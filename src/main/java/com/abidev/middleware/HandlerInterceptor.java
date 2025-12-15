package com.abidev.middleware;

public interface HandlerInterceptor {

    /**
     * Pre-handle method to be executed before the main request handling.
     *
     * @param ctx the request context
     * @return true to continue processing, false to abort
     */
    default boolean preHandle(RequestContext ctx) {
        return true;
    }

    /**
     * Post-handle method to be executed after the main request handling.
     * @param ctx the request context
     * @param result the result of the main request handling
     */
    default void postHandle(RequestContext ctx, Object result) {
        // Default implementation does nothing
    }

    /**
     * After-completion method to be executed after the request has been fully processed.
     *
     * @param ctx the request context
     * @param ex any exception that occurred during processing, or null if none
     */
    default void afterCompletion(RequestContext ctx, Exception ex) {
        // Default implementation does nothing
    }
}
