package com.abidev.middleware;

import java.util.HashMap;
import java.util.Map;

public class RequestContext {

    private final String path;
    private final Map<String, String> pathVariables;
    private final Map<String, Object> attributes = new HashMap<>();

    public RequestContext(String path, Map<String, String> pathVariables) {
        this.path = path;
        this.pathVariables = pathVariables;
    }

    public String getPath() {
        return path;
    }

    public Map<String, String> getPathVariables() {
        return pathVariables;
    }

    public void setAttribute(String key, Object value) {
        attributes.put(key, value);
    }

    public Object getAttribute(String key) {
        return attributes.get(key);
    }
}
