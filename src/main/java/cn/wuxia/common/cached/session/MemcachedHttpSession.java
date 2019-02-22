/**
 * 
 */
package cn.wuxia.common.cached.session;

import java.io.Serializable;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionContext;

import org.apache.commons.lang3.builder.ToStringBuilder;

import com.google.common.collect.Maps;

import cn.wuxia.common.util.StringUtil;

/**
 * HttpSession Object in memcached
 * 
 * @author songlin.li
 * @version 1.0.0
 */
public class MemcachedHttpSession implements HttpSession, Serializable {

    private static final long serialVersionUID = 1L;

    protected long creationTime = 0L;

    protected String id;

    protected int maxInactiveInterval;

    protected long lastAccessedTime = 0;

    protected transient boolean expired = false;

    protected transient boolean isNew = false;

    protected transient boolean isDirty = false;

    private transient SessionListener listener;

    private Map<String, Object> data = Maps.newHashMap();

    public void setListener(SessionListener listener) {
        this.listener = listener;
    }

    @Override
    public long getCreationTime() {
        return creationTime;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public long getLastAccessedTime() {
        return lastAccessedTime;
    }

    public void setLastAccessedTime(long lastAccessedTime) {
        this.lastAccessedTime = lastAccessedTime;
        listener.onAttributeChanged(this);
    }

    @Override
    public ServletContext getServletContext() {
        return null;
    }

    @Override
    public void setMaxInactiveInterval(int i) {
        this.maxInactiveInterval = i;
        lastAccessedTime = System.currentTimeMillis();
        listener.onAttributeChanged(this);
    }

    @Override
    public int getMaxInactiveInterval() {
        return maxInactiveInterval;
    }

    @Override
    public HttpSessionContext getSessionContext() {
        return null;
    }

    @Override
    public Object getAttribute(String key) {
        if (null != data.get(key)) {
            lastAccessedTime = System.currentTimeMillis();
            // listener.onAttributeChanged(this);
        }
        return data.get(key);
    }

    @Override
    public Object getValue(String key) {
        return getAttribute(key);
    }

    @Override
    public Enumeration getAttributeNames() {
        final Iterator iterator = data.keySet().iterator();
        lastAccessedTime = System.currentTimeMillis();
        // listener.onAttributeChanged(this);
        return new Enumeration() {
            public boolean hasMoreElements() {
                return iterator.hasNext();
            }

            public Object nextElement() {
                return iterator.next();
            }
        };
    }

    @Override
    public String[] getValueNames() {
        String[] names = new String[data.size()];
        lastAccessedTime = System.currentTimeMillis();
        // listener.onAttributeChanged(this);
        return data.keySet().toArray(names);
    }

    @Override
    public void setAttribute(String s, Object o) {
        Object obj = data.get(s);
        if (o instanceof String) {
            if (StringUtil.equalsIgnoreCase((String) obj, (String) o)) {
                return;
            }
        } else if (o != null) {
            // if(o.equals(obj))
            // return;
        }
        data.put(s, o);
        lastAccessedTime = System.currentTimeMillis();
        // isDirty = true;
        listener.onAttributeChanged(this);
    }

    @Override
    public void putValue(String s, Object o) {
        setAttribute(s, o);
    }

    @Override
    public void removeAttribute(String s) {
        if (data.get(s) != null) {
            data.remove(s);
            // isDirty = true;
            lastAccessedTime = System.currentTimeMillis();
            listener.onAttributeChanged(this);
        }
    }

    @Override
    public void removeValue(String s) {
        removeAttribute(s);
    }

    @Override
    public void invalidate() {
        expired = true;
        isDirty = true;
        if (listener != null)
            listener.onInvalidated(this);
    }

    @Override
    public boolean isNew() {
        return isNew;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }
}
