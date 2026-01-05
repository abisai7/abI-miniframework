package com.abidev.middleware;

import com.sun.net.httpserver.HttpExchange;

import java.util.HashMap;
import java.util.Map;

public class RequestContext {

    private final String body;
    private final String path;
    private final Map<String, String> pathVariables;
    private final Map<String, String> queryParams;
    private final Map<String, Object> attributes = new HashMap<>();
    private final HttpExchange exchange;
    private final Map<String, String> headers;

    public RequestContext(String body, String path,
                          Map<String, String> pathVariables,
                          Map<String, String> queryParams,
                          Map<String, String> headers,
                          HttpExchange exchange) {
        this.body = body;
        this.path = path;
        this.pathVariables = pathVariables;
        this.queryParams = queryParams;
        this.headers = headers;
        this.exchange = exchange;
    }

    public String getBody() {
        return body;
    }

    public String getPath() {
        return path;
    }

    public Map<String, String> getPathVariables() {
        return pathVariables;
    }

    public Map<String, String> getQueryParams() {
        return queryParams;
    }

    public HttpExchange getExchange() {
        return exchange;
    }

    public void setAttribute(String key, Object value) {
        attributes.put(key, value);
    }

    public Object getAttribute(String key) {
        return attributes.get(key);
    }

    public Map<String, String> getHeaders() {
        return headers;
    }
}
