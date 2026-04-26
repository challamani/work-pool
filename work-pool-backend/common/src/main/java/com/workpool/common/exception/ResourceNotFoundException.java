package com.workpool.common.exception;

public class ResourceNotFoundException extends WorkPoolException {
    public ResourceNotFoundException(String resource, String id) {
        super("NOT_FOUND", resource + " not found with id: " + id);
    }
}
