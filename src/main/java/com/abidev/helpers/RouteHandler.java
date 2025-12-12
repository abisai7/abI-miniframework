package com.abidev.helpers;
import java.lang.reflect.Method;

/**
 * A helper class to encapsulate a route handler method and its instance.
 */
public class RouteHandler {
    private final Object instance;
    private final Method method;
    private final String routePattern;

    public RouteHandler(Object instance, Method method, String routePattern) {
        this.instance = instance;
        this.method = method;
        this.routePattern = routePattern;
    }

    public boolean matches(String path) {
        return path.split("/").length == routePattern.split("/").length;
    }

    public Object invoke() throws Exception {
        return method.invoke(instance);
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
                args[argIndex] = convertToType(value, expectedType);
            }
        }

        return method.invoke(instance, args);
    }

    /**
     * Converts a string value to the specified type.
     *
     * @param value the string value to convert
     * @param type  the target type class
     * @return the converted object
     */
    private Object convertToType(String value, Class<?> type) {
        if (type == String.class) {
            return value;
        }

        if (type == int.class || type == Integer.class) {
            return Integer.parseInt(value);
        }

        if (type == long.class || type == Long.class) {
            return Long.parseLong(value);
        }

        if (type == boolean.class || type == Boolean.class) {
            return Boolean.parseBoolean(value);
        }

        throw new IllegalArgumentException("Unsupported parameter type: " + type.getName());
    }


}