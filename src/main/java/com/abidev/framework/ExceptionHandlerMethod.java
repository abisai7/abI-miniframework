package com.abidev.framework;

import java.lang.reflect.Method;
import java.util.List;

public class ExceptionHandlerMethod {

    private final Object instance;
    private final Method method;
    private final List<Class<? extends Throwable>> handledExceptions;

    public ExceptionHandlerMethod(Object instance, Method method, List<Class<? extends Throwable>> handledExceptions) {
        this.instance = instance;
        this.method = method;
        this.handledExceptions = handledExceptions;
    }

    public boolean supports(Throwable ex) {
        return handledExceptions.stream().anyMatch(c -> c.isAssignableFrom(ex.getClass()));
    }

    public Object invoke(Throwable ex) throws Exception {
        method.setAccessible(true);
        return method.invoke(instance, ex);
    }

    public List<Class<? extends Throwable>> getHandledExceptions() {
        return handledExceptions;
    }
}
