package com.abidev.services;

import com.abidev.annotations.Component;
import com.abidev.annotations.Scope;

@Component
@Scope(Scope.SINGLETON)
public class MessageService {

    public String getMessage() {
        return "This is a message from MessageService.";
    }
}
