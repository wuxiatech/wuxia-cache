package cn.wuxia.common.cached.session;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * rewrite HttpServletRequest,change getSession method,use memcached session
 * implement
 * 
 * @author songlin.li
 * @version 1.0.0
 */

public class MemcachedSessionFilter implements Filter {
    private Logger logger = LoggerFactory.getLogger(getClass());

    public static final String[] IGNORE_SUFFIX = new String[] { ".png", ".jpg", ".jpeg", ".gif", ".css", ".js",
            ".html", ".htm", ".shtml" };

    private MemcachedSessionManager sessionManager;

    public void init(FilterConfig filterConfig) throws ServletException {

    }

    public void setSessionManager(MemcachedSessionManager sessionManager) {
        this.sessionManager = sessionManager;
    }

    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain)
            throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        // igonre image,css or javascript file request
        if (!shouldFilter(request)) {
            filterChain.doFilter(servletRequest, servletResponse);
            return;
        }
        long begineTime = System.currentTimeMillis();
        String uri = request.getRequestURI();
        logger.debug("$$$[" + request.getRemoteAddr() + "] Begine memcached session. URI:" + uri);

        RequestEventSubject eventSubject = new RequestEventSubject();
        HttpServletResponse response = (HttpServletResponse) servletResponse;
        SessionHttpServletRequestWrapper requestWrapper = new SessionHttpServletRequestWrapper(request, response,
                sessionManager, eventSubject);

        try {
            filterChain.doFilter(requestWrapper, servletResponse);
        } finally {
            // when request is completed,refresh session event,write cookie or
            // save into memcached
            eventSubject.completed(request, response);
        }
        long totalTime = System.currentTimeMillis() - begineTime;
        if (totalTime <= 5 * 1000) {
            logger.debug("$$$[" + request.getRemoteAddr()
                    + "] End memcached session. Current request cost time(Level-1): " + (totalTime) + " ms. URI:" + uri);
        } else if (totalTime > 5 * 1000 && totalTime <= 20 * 1000) {
            logger.debug("$$$[" + request.getRemoteAddr()
                    + "] End memcached session. Current request cost time(Level-2): " + (totalTime) + " ms. URI:" + uri);
        } else if (totalTime > 20 * 1000) {
            logger.warn("$$$[" + request.getRemoteAddr()
                    + "] End memcached session. Current request cost time(Level-3): " + (totalTime) + " ms. URI:" + uri);
        }
    }

    /**
     * igonre image,css or javascript file request
     * 
     * @param request HttpServletRequest
     * @return
     */
    private boolean shouldFilter(HttpServletRequest request) {
        String uri = request.getRequestURI().toLowerCase();
        for (String suffix : IGNORE_SUFFIX) {
            if (uri.endsWith(suffix))
                return false;
        }
        return true;
    }

    public void destroy() {

    }
}
