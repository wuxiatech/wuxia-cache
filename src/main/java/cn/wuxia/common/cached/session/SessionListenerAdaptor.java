package cn.wuxia.common.cached.session;

/**
 * SessionListener interface adaptor
 * 
 * @author songlin.li
 * @version 1.0.0
 */
public class SessionListenerAdaptor implements SessionListener {

    public void onAttributeChanged(MemcachedHttpSession session) {
    }

    public void onInvalidated(MemcachedHttpSession session) {
    }
}
