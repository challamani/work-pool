package com.workpool.common.exception;

public class WorkPoolException extends RuntimeException {
    private final String errorCode;

    public WorkPoolException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public WorkPoolException(String errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }

    public String getErrorCode() {
        return errorCode;
    }
}
