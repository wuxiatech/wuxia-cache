package cn.wuxia.common.cached.memcached;

import cn.wuxia.common.exception.ServiceException;

/**
 * 
 * [ticket id]
 * Description of the class 
 * @author songlin.li
 * @ Version : V<Ver.No> <2013年6月30日>
 */
public class MemcachedException extends ServiceException {

    /**
     * Comment for <code>serialVersionUID</code>
     */
    private static final long serialVersionUID = 3934351634325210052L;

    public MemcachedException(String msg) {
        super(msg);
    }

    public MemcachedException(String message, String... args) {
        super(message, args);
    }

    public MemcachedException(String message, Throwable cause, String... args) {
        super(message, cause, args);
    }

}
