package com.abidev.middleware;

import com.abidev.annotations.Component;

@Component
public class LoggingInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(RequestContext ctx) {
        System.out.println("Incoming request: " + ctx.getPath());
        return true;
    }

    @Override
    public void postHandle(RequestContext ctx, Object result) {
        System.out.println("Response: " + result);
    }
}