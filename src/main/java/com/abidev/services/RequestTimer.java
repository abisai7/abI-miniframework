package com.abidev.services;

import com.abidev.annotations.Component;
import com.abidev.annotations.Scope;

@Component
@Scope(Scope.PROTOTYPE)
public class RequestTimer {

    private long timestamp = System.currentTimeMillis();

    public long getTimestamp() {
        return timestamp;
    }
}
