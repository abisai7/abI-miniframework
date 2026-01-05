package com.abidev.helpers;
import com.abidev.annotations.PathVariable;
import com.abidev.annotations.RequestParam;
import com.abidev.http.QueryParamsUtils;
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

        return new RequestContext(path, variables, queryParams, exchange);
    }

    /**
     * Invokes the controller method.
     */
    public Object invoke(RequestContext ctx) throws Exception {

        Object controller = instanceSupplier.get();

        Class<?>[] paramTypes = method.getParameterTypes();
        Annotation[][] paramAnnotations = method.getParameterAnnotations();

        Object[] args = new Object[paramTypes.length];

        for (int i = 0; i < paramTypes.length; i++) {

            Class<?> paramType = paramTypes[i];
            Annotation[] annotations = paramAnnotations[i];

            boolean resolved = false;

            // 1️⃣ RequestContext
            if (paramType == RequestContext.class) {
                args[i] = ctx;
                continue;
            }

            // 2️⃣ @PathVariable y @RequestParam
            for (Annotation a : annotations) {

                // ---- PATH VARIABLE ----
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

                // ---- QUERY PARAM ----
                if (a instanceof RequestParam rp) {

                    String name = !rp.value().isEmpty()
                            ? rp.value()
                            : null;

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
            }

            // 3️⃣ Not resolved -> error
            if (!resolved) {
                throw new IllegalArgumentException(
                        "Cannot resolve parameter at index " + i +
                                " of type " + paramType.getName() +
                                " in method " + method.getName()
                );
            }
        }

        return method.invoke(controller, args);
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

}