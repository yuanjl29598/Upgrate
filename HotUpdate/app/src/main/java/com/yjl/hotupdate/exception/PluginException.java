package com.yjl.hotupdate.exception;

/**
 * @author yjl
 * @date
 * @note
 */
public class PluginException extends Exception {

    private static final long serialVersionUID = 1L;

    public PluginException() {
        super();
    }

    public PluginException(String msg) {
        super(msg);
    }

    public PluginException(String msg, Throwable cause) {
        super(msg, cause);
    }

    public PluginException(Throwable cause) {
        super(cause);
    }
}
