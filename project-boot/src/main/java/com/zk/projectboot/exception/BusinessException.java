package com.zk.projectboot.exception;

import org.springframework.http.HttpStatus;

/**
 * Expected business failure that can be mapped to a stable HTTP status.
 */
public class BusinessException extends RuntimeException {

    private final HttpStatus status;

    public BusinessException(HttpStatus status, String message) {
        super(message);
        this.status = status;
    }

    public HttpStatus getStatus() {
        return status;
    }
}
