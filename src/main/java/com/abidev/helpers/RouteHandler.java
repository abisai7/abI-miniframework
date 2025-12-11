package com.abidev.helpers;
import java.lang.reflect.Method;

/**
 * A helper class to encapsulate a route handler method and its instance.
 */
public class RouteHandler {
    private final Object instance;
    private final Method method;

    public RouteHandler(Object instance, Method method) {
        this.instance = instance;
        this.method = method;
    }

    public Object invoke() throws Exception {
        return method.invoke(instance);
    }
}