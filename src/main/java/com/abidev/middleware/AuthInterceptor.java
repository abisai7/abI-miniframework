package com.abidev.middleware;

import com.abidev.annotations.Component;

@Component
public class AuthInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(RequestContext ctx) {
        return !ctx.getPath().startsWith("/admin");
    }
}