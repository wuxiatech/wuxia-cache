package cn.wuxia.common.cached.session;

/**
 * function description
 * 
 * @author songlin.li
 * @version 1.0.0
 */
public class SessionException extends RuntimeException {
    /**
     * Comment for <code>serialVersionUID</code>
     */
    private static final long serialVersionUID = 9011485126758454188L;

    public SessionException() {
        super();
    }

    public SessionException(String message) {
        super(message);
    }

    public SessionException(String message, Throwable cause) {
        super(message, cause);
    }

    public SessionException(Throwable cause) {
        super(cause);
    }
}
