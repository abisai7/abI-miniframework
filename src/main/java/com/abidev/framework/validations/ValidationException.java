package com.abidev.framework.validations;

import com.abidev.annotations.ResponseStatus;

import java.util.List;

@ResponseStatus(400)
public class ValidationException extends RuntimeException {

    private final List<String> errors;

    public ValidationException(List<String> errors) {
        super(String.join("\n", errors));
        this.errors = errors;
    }

    public List<String> getErrors() {
        return errors;
    }
}
