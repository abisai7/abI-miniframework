package com.abidev.services;

import com.abidev.annotations.Component;

@Component
public class MessageService {

    public String getMessage() {
        return "This is a message from MessageService.";
    }
}
