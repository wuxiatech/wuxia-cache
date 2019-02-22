package cn.wuxia.common.cached.session;

/**
 * SessionListener
 * 
 * @author songlin.li
 * @version 1.0.0
 */
public interface SessionListener {
    public void onAttributeChanged(MemcachedHttpSession session);

    public void onInvalidated(MemcachedHttpSession session);
}
