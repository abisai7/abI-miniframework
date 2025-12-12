package com.abidev.helpers;
import java.lang.reflect.Method;
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
                args[argIndex] = convertToType(value, expectedType);
                argIndex++;
            }
        }

        Object instance = instanceSupplier.get();
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

    private String normalize(String s) {
        if (s == null || s.isBlank()) return "";
        if (s.startsWith("/")) s = s.substring(1);
        if (s.endsWith("/")) s = s.substring(0, s.length() - 1);
        return s;
    }

    public String getPattern() {
        return routePattern;
    }

}