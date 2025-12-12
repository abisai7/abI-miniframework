package com.abidev.controllers;

import com.abidev.annotations.Component;
import com.abidev.annotations.Inject;
import com.abidev.annotations.Route;
import com.abidev.services.MessageService;

@Component
public class HelloController {

    @Inject
    private MessageService messageService;

    @Route("/hello")
    public String sayHello() {
        return messageService.getMessage();
    }

    @Route("/goodbye")
    public String sayGoodbye() {
        return "Goodbye, World!";
    }
}
