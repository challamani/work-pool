package com.workpool.common.exception;

public class UnauthorizedException extends WorkPoolException {
    public UnauthorizedException(String message) {
        super("UNAUTHORIZED", message);
    }
}
