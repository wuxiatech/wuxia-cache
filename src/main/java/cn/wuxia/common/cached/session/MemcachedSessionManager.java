package cn.wuxia.common.cached.session;

import java.util.UUID;
import java.util.concurrent.TimeoutException;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;

import net.rubyeye.xmemcached.MemcachedClient;
import net.rubyeye.xmemcached.exception.MemcachedException;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.wuxia.common.spring.SpringContextHolder;

/**
 * MemcachedSessionManager
 * 
 * @author songlin.li
 * @version 1.0.0
 */

public class MemcachedSessionManager {
    private String sessionIdPrefix = "M_JSID_";

    private String sessionCookieName = "M_JSESSIONID";

    private String sessionCookieDomain = "";

    /* 如果session没有变化，则5分钟更新一次memcached */
    private int expirationUpdateInterval = 5 * 60;

    private int maxInactiveInterval = 30 * 60;

    private Logger logger = LoggerFactory.getLogger(getClass());

    // 必须配置了memcachedClient 的注入方可使用
    public MemcachedClient getMemcachedClient() {
        return SpringContextHolder.getBean("memcachedClient");
    }

    public void setExpirationUpdateInterval(int expirationUpdateInterval) {
        this.expirationUpdateInterval = expirationUpdateInterval;
    }

    public void setMaxInactiveInterval(int maxInactiveInterval) {
        this.maxInactiveInterval = maxInactiveInterval;
    }

    public void setSessionIdPrefix(String sessionIdPrefix) {
        this.sessionIdPrefix = sessionIdPrefix;
    }

    public void setSessionCookieName(String sessionCookieName) {
        this.sessionCookieName = sessionCookieName;
    }

    public void setSessionCookieDomain(String sessionCookieDomain) {
        this.sessionCookieDomain = sessionCookieDomain;
    }

    /**
     * Description of the method
     * 
     * @author songlin.li
     * @param request
     * @param response
     * @param requestEventSubject
     * @param create
     * @return
     */
    public MemcachedHttpSession createSession(SessionHttpServletRequestWrapper request, HttpServletResponse response,
            RequestEventSubject requestEventSubject, boolean create) {
        String sessionId = getRequestedSessionId(request);
        MemcachedHttpSession session = null;
        if (StringUtils.isEmpty(sessionId) && create == false)
            return null;
        if (StringUtils.isNotEmpty(sessionId)) {
            session = loadSession(sessionId);
        }
        if (session == null && create) {
            session = createEmptySession(request, response);
        }
        if (session != null)
            attachEvent(session, request, response, requestEventSubject);
        return session;
    }

    /**
     * not use method <code>request.getRequestedSessionId()</code> ,because some
     * problem with websphere implements
     * 
     * @param request
     * @return
     */
    private String getRequestedSessionId(HttpServletRequestWrapper request) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null || cookies.length == 0)
            return null;
        for (Cookie cookie : cookies) {
            if (sessionCookieName.equals(cookie.getName()))
                return cookie.getValue();
        }
        return null;
    }

    /**
     * save or delete memcached session
     * 
     * @author songlin.li
     * @param session
     */
    private void saveSession(MemcachedHttpSession session) {
        if (session.isNew && session.expired) {
            return;
        }
        session.lastAccessedTime = System.currentTimeMillis();
        if (logger.isDebugEnabled()) {
            logger.debug("MemcachedHttpSession saveSession [ID=" + session.id + ",isNew=" + session.isNew + ",isDiry=" + session.isDirty
                    + ",isExpired=" + session.expired + "]");
            logger.debug("save session to memcached {}", session);
        }
        if (session.expired)
            deleteMemcachedHttpSession(session.id);
        else
            setMemcachedHttpSession(session);
    }

    /**
     * delete session from memcached
     * 
     * @author songlin.li
     * @param sessionId
     */
    private void deleteMemcachedHttpSession(String sessionId) {
        try {
            getMemcachedClient().delete(generatorSessionKey(sessionId));
        } catch (TimeoutException | InterruptedException | MemcachedException e) {
            throw new SessionException(e);
        }
    }

    /**
     * get session from memecached
     * 
     * @author songlin.li
     * @param sessionId
     * @return
     */
    private MemcachedHttpSession getMemcachedHttpSession(String sessionId) {
        try {
            return getMemcachedClient().get(generatorSessionKey(sessionId));
        } catch (TimeoutException | InterruptedException | MemcachedException e) {
            throw new SessionException(e);
        }
    }

    /**
     * set session to memcached
     * 
     * @author songlin.li
     * @param session
     */
    private void setMemcachedHttpSession(MemcachedHttpSession session) {
        try {
            getMemcachedClient().set(generatorSessionKey(session.id), session.maxInactiveInterval + expirationUpdateInterval, session);
        } catch (TimeoutException | InterruptedException | MemcachedException e) {
            throw new SessionException(e);
        }
    }

    private MemcachedHttpSession createEmptySession(SessionHttpServletRequestWrapper request, HttpServletResponse response) {
        MemcachedHttpSession session = new MemcachedHttpSession();
        session.id = createSessionId(request);
        session.creationTime = System.currentTimeMillis();
        session.maxInactiveInterval = maxInactiveInterval;
        session.isNew = true;
        if (logger.isDebugEnabled())
            logger.debug("MemcachedHttpSession Create [ID=" + session.id + "]");
        saveCookie(session, request, response);
        return session;
    }

    private String createSessionId(SessionHttpServletRequestWrapper request) {
        // String sessionId = request.getSession().getId();
        // if (StringUtils.isNotBlank(sessionId))
        // return sessionId;
        return UUID.randomUUID().toString().replace("-", "").toUpperCase();
    }

    /**
     * when request is completed,write session into memcached and write cookie
     * into response
     * 
     * @param session MemcachedHttpSession
     * @param request HttpServletRequestWrapper
     * @param response HttpServletResponse
     * @param requestEventSubject RequestEventSubject
     */
    private void attachEvent(final MemcachedHttpSession session, final HttpServletRequestWrapper request, final HttpServletResponse response,
            RequestEventSubject requestEventSubject) {
        session.setListener(new SessionListenerAdaptor() {
            public void onInvalidated(MemcachedHttpSession session) {
                saveCookie(session, request, response);
            }

            public void onAttributeChanged(MemcachedHttpSession session) {
                saveSession(session);
            }
        });

        requestEventSubject.attach(new RequestEventObserver() {
            public void completed(HttpServletRequest servletRequest, HttpServletResponse response) {
                int updateInterval = (int) ((System.currentTimeMillis() - session.lastAccessedTime) / 1000);
                if (logger.isDebugEnabled())
                    logger.debug("MemcachedHttpSession Request completed [ID=" + session.id + ",lastAccessedTime=" + session.lastAccessedTime
                            + ",updateInterval=" + updateInterval + "]");
                // 非新创建session，数据未更改且未到更新间隔，则不更新memcached
                if (session.isNew == false && session.isDirty == false && updateInterval < expirationUpdateInterval) {
                    return;
                }
                saveSession(session);
            }
        });
    }

    private void saveCookie(MemcachedHttpSession session, HttpServletRequestWrapper request, HttpServletResponse response) {
        if (session.isNew == false && session.expired == false)
            return;

        Cookie cookie = new Cookie(sessionCookieName, null);
        String contextPath = request.getContextPath();
        cookie.setPath(contextPath.length() > 0 ? contextPath : "/");
        if (StringUtils.isNotBlank(sessionCookieDomain)) {
            cookie.setDomain(sessionCookieDomain);
        }
        if (session.expired) {
            cookie.setMaxAge(0);
        } else if (session.isNew) {
            cookie.setValue(session.getId());
        }
        response.addCookie(cookie);
        if (logger.isDebugEnabled())
            logger.debug("MemcachedHttpSession saveCookie [ID=" + session.id + "]");
    }

    private MemcachedHttpSession loadSession(String sessionId) {
        try {
            MemcachedHttpSession session = getMemcachedHttpSession(sessionId);
            if (logger.isDebugEnabled()) {
                logger.debug("MemcachedHttpSession Load [ID=" + sessionId + ",exist=" + (session != null) + "]");
                logger.debug("get session from memcached {}", session);
            }
            if (session != null) {
                session.isNew = false;
                session.isDirty = false;
            }
            return session;
        } catch (Exception e) {
            logger.warn("exception loadSession [Id=" + sessionId + "]", e);
            return null;
        }

    }

    private String generatorSessionKey(String sessionId) {
        return sessionIdPrefix.concat(sessionId);
    }

}
