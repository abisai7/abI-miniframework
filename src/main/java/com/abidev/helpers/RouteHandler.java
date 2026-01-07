package com.abidev.helpers;
import com.abidev.annotations.*;
import com.abidev.annotations.validation.Valid;
import com.abidev.framework.validations.ValidatorEngine;
import com.abidev.http.HandlerResult;
import com.abidev.http.QueryParamsUtils;
import com.abidev.http.ResponseEntity;
import com.abidev.middleware.RequestContext;
import com.sun.net.httpserver.HttpExchange;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

/**
 * A helper class to encapsulate a route handler method and its instance.
 */
public class RouteHandler {

    private final Supplier<Object> instanceSupplier;
    private final Method method;
    private final String routePattern;

    public RouteHandler(Supplier<Object> instanceSupplier, Method method, String routePattern) {
        this.instanceSupplier = instanceSupplier;
        this.method = method;
        this.routePattern = routePattern;
        this.method.setAccessible(true);
    }

    public boolean matches(String path) {
        String[] pathParts = normalize(path).split("/");
        String[] patternParts = normalize(routePattern).split("/");

        if (pathParts.length != patternParts.length) {
            return false;
        }

        for (int i = 0; i < patternParts.length; i++) {
            if (patternParts[i].startsWith("{")) continue;
            if (!patternParts[i].equals(pathParts[i])) {
                return false;
            }
        }

        return true;
    }

    /**
     * Invokes the method with parameters extracted from the given path.
     *
     * @param path the URL path containing parameters
     * @return the result of the method invocation
     * @throws Exception if an error occurs during invocation
     */
    public Object invokeWithPath(String path) throws Exception {
        String[] pathParts = path.split("/");
        String[] patternParts = routePattern.split("/");

        Class<?>[] paramTypes = method.getParameterTypes();
        Object[] args = new Object[paramTypes.length];

        int argIndex = 0;
        for (int i = 0; i < patternParts.length; i++) {
            if (patternParts[i].startsWith("{")) {
                String value = pathParts[i];
                Class<?> expectedType = paramTypes[argIndex];
                args[argIndex] = convert(value, expectedType);
                argIndex++;
            }
        }

        Object instance = instanceSupplier.get();
        return method.invoke(instance, args);
    }

    private String normalize(String s) {
        if (s == null || s.isBlank()) return "";
        if (s.startsWith("/")) s = s.substring(1);
        if (s.endsWith("/")) s = s.substring(0, s.length() - 1);
        return s;
    }

    public String getPattern() {
        return routePattern;
    }

    /**
     * Creates RequestContext extracting path variables.
     */
    public RequestContext createContext(String path, HttpExchange exchange) {

        String[] patternParts = routePattern.split("/");
        String[] pathParts = path.split("/");

        Map<String, String> variables = new HashMap<>();


        for (int i = 0; i < patternParts.length; i++) {
            String p = patternParts[i];

            if (p.startsWith("{") && p.endsWith("}")) {
                String varName = p.substring(1, p.length() - 1);
                variables.put(varName, pathParts[i]);
            }
        }

        Map<String, String> queryParams = QueryParamsUtils.parse(exchange);

        String body = null;
        try {
            body = new String(exchange.getRequestBody().readAllBytes());
        } catch (Exception e) {
            e.printStackTrace();
        }

        Map<String, String> headers = new HashMap<>();
        exchange.getRequestHeaders().forEach((key, values) -> {
            if (!values.isEmpty()) {
                headers.put(key.toLowerCase(), values.get(0));
            }
        });

        return new RequestContext(body, path, variables, queryParams, headers, exchange);
    }

    /**
     * Invokes the controller method and normalizes the result into a HandlerResult.
     */
    public HandlerResult invoke(RequestContext ctx) throws Exception {

        Object controller = instanceSupplier.get();

        Class<?>[] paramTypes = method.getParameterTypes();
        Annotation[][] paramAnnotations = method.getParameterAnnotations();

        Object[] args = new Object[paramTypes.length];

        // =============================
        // 1️⃣ Resolve method parameters
        // =============================
        for (int i = 0; i < paramTypes.length; i++) {

            Class<?> paramType = paramTypes[i];
            Annotation[] annotations = paramAnnotations[i];

            boolean resolved = false;

            // ---- RequestContext ----
            if (paramType == RequestContext.class) {
                args[i] = ctx;
                resolved = true;
                continue;
            }

            for (Annotation a : annotations) {

                // ---- @PathVariable ----
                if (a instanceof PathVariable pv) {

                    String name = pv.value();
                    String raw = ctx.getPathVariables().get(name);

                    if (raw == null) {
                        throw new IllegalArgumentException(
                                "Missing path variable: " + name
                        );
                    }

                    args[i] = convert(raw, paramType);
                    resolved = true;
                    break;
                }

                // ---- @RequestParam ----
                if (a instanceof RequestParam rp) {

                    String name = !rp.value().isEmpty() ? rp.value() : null;
                    String raw = name != null
                            ? ctx.getQueryParams().get(name)
                            : null;

                    if (raw == null || raw.isEmpty()) {

                        if (!rp.defaultValue().isEmpty()) {
                            raw = rp.defaultValue();
                        } else if (rp.required()) {
                            throw new IllegalArgumentException(
                                    "Missing required query param: " + name
                            );
                        } else {
                            args[i] = null;
                            resolved = true;
                            break;
                        }
                    }

                    args[i] = convert(raw, paramType);
                    resolved = true;
                    break;
                }

                // ---- @RequestBody ----
                if (a instanceof RequestBody rb) {

                    String body = ctx.getBody();

                    if ((body == null || body.isBlank()) && rb.required()) {
                        throw new IllegalArgumentException(
                                "Missing required request body for parameter " + i
                        );
                    }

                    Object obj = BodyConverter.convert(body, paramType);

                    if (hasAnnotation(paramAnnotations[i], Valid.class)) {
                        ValidatorEngine.validate(obj);
                    }

                    args[i] = obj;
                    resolved = true;
                    break;
                }

                // ---- @RequestHeader ----
                if (a instanceof RequestHeader rh) {

                    String name = rh.value().toLowerCase();
                    String raw = ctx.getHeaders().get(name);

                    if (raw == null || raw.isEmpty()) {

                        if (!rh.defaultValue().isEmpty()) {
                            raw = rh.defaultValue();
                        } else if (rh.required()) {
                            throw new IllegalArgumentException(
                                    "Missing required header: " + rh.value()
                            );
                        } else {
                            args[i] = null;
                            resolved = true;
                            break;
                        }
                    }

                    args[i] = convert(raw, paramType);
                    resolved = true;
                    break;
                }
            }

            if (!resolved) {
                throw new IllegalArgumentException(
                        "Cannot resolve parameter at index " + i +
                                " of type " + paramType.getName() +
                                " in method " +
                                method.getDeclaringClass().getSimpleName() +
                                "." + method.getName()
                );
            }
        }

        // ============================
        // 2️⃣ Invoke controller method
        // ============================
        method.setAccessible(true);
        Object result = method.invoke(controller, args);

        // =========================
        // 3️⃣ Normalize the response
        // =========================

        // ---- ResponseEntity ----
        if (result instanceof ResponseEntity<?> re) {
            return new HandlerResult(
                    re.getStatus(),
                    re.getHeaders(),
                    re.getBody()
            );
        }

        // ---- @ResponseStatus ----
        if (method.isAnnotationPresent(ResponseStatus.class)) {
            int status = method.getAnnotation(ResponseStatus.class).value();
            return new HandlerResult(status, Map.of(), result);
        }

        // ---- Default 200 OK ----
        return new HandlerResult(200, Map.of(), result);
    }


    private Object convert(String value, Class<?> targetType) {

        if (value == null) {
            return null;
        }

        if (targetType == String.class) {
            return value;
        }
        if (targetType == int.class || targetType == Integer.class) {
            return Integer.parseInt(value);
        }
        if (targetType == long.class || targetType == Long.class) {
            return Long.parseLong(value);
        }
        if (targetType == boolean.class || targetType == Boolean.class) {
            return Boolean.parseBoolean(value);
        }

        throw new RuntimeException("Unsupported parameter type: " + targetType.getName());
    }

    private boolean hasAnnotation(Annotation[] annotations, Class<?> type) {
        for (Annotation a : annotations) {
            if (a.annotationType() == type) return true;
        }
        return false;
    }

}