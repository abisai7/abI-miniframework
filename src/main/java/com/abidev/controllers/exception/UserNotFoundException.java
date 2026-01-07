package com.abidev.controllers.exception;

import com.abidev.annotations.ResponseStatus;

@ResponseStatus(404)
public class UserNotFoundException extends RuntimeException {
    public UserNotFoundException(String msg) {
        super(msg);
    }
}
