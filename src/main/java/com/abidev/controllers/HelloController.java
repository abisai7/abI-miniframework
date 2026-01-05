package com.abidev.controllers;

import com.abidev.annotations.*;
import com.abidev.services.MessageService;
import com.abidev.services.RequestTimer;

@Component
public class HelloController {

    private final MessageService messageService;
    private final RequestTimer timer;

    public HelloController(MessageService messageService, RequestTimer requestTimer) {
        this.messageService = messageService;
        this.timer = requestTimer;
    }

    @Route("/hello")
    public String sayHello() {
        return messageService.getMessage();
    }

    @Route("/hello/{name}")
    public String greetByName(@PathVariable("name") String name) {
        return "Hello, " + name + "!";
    }

    @Route("/user/{id}")
    public String getUserById(@PathVariable("id") int id) {
        return "User ID: " + id;
    }

    @Route("/user/{id}/profile")
    public String getUserProfile(@PathVariable("id") int id) {
        return "Profile for user ID: " + id;
    }

    @Route("/user/create")
    public String create(@RequestBody UserRequest body){
        return "Created user: " + body.name() + ", age " + body.age();
    }

    @Route("/goodbye")
    public String sayGoodbye() {
        return "Goodbye, World!";
    }

    @Route("/time")
    public String getRequestTime() {
        return "Request timestamp: " + timer.getTimestamp();
    }

    @Route("/admin")
    public String adminArea() {
        return "Welcome to the admin area!";
    }

    @Route("/search")
    public String search(
            @RequestParam("q") String query,
            @RequestParam(defaultValue = "1") int page
    ) {
        return "Searching '" + query + "' page " + page;
    }

    public record UserRequest(String name, int age) {}
}
