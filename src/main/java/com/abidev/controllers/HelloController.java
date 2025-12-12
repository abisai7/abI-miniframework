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

    @Route("/hello/{name}")
    public String greetByName(String name) {
        return "Hello, " + name + "!";
    }

    @Route("/user/{id}")
    public String getUserById(int id) {
        return "User ID: " + id;
    }

    @Route("/user/{id}/profile")
    public String getUserProfile(int id) {
        return "Profile for user ID: " + id;
    }

    @Route("/goodbye")
    public String sayGoodbye() {
        return "Goodbye, World!";
    }
}
