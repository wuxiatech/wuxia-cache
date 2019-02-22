package cn.wuxia.common.cached.session;

/**
 * function description
 * 
 * @author songlin.li
 * @version 1.0.0
 */

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

interface RequestEventObserver {
    public void completed(HttpServletRequest servletRequest, HttpServletResponse response);
}
