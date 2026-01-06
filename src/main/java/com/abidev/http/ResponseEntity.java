package com.abidev.http;

import java.util.HashMap;
import java.util.Map;

public class ResponseEntity<T> {

    private final int status;
    private final Map<String, String> headers;
    private final T body;

    private ResponseEntity(int status, Map<String, String> headers, T body) {
        this.status = status;
        this.headers = headers;
        this.body = body;
    }

    public int getStatus() {
        return status;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public T getBody() {
        return body;
    }

    // ===== Builders =====

    public static <T> ResponseEntity<T> ok(T body) {
        return ResponseEntity.<T>status(200).body(body);
    }

    public static <T> Builder<T> status(int status) {
        return new Builder<>(status);
    }

    public static class Builder<T> {
        private final int status;
        private final Map<String, String> headers = new HashMap<>();

        private Builder(int status) {
            this.status = status;
        }

        public Builder<T> header(String name, String value) {
            headers.put(name, value);
            return this;
        }

        public ResponseEntity<T> body(T body) {
            return new ResponseEntity<>(status, headers, body);
        }
    }
}
