package com.abidev.controllers;

import com.abidev.annotations.Component;
import com.abidev.annotations.Route;

@Component
public class HelloController {

    @Route("/hello")
    public String sayHello() {
        return "Hello, World!";
    }

    @Route("/goodbye")
    public String sayGoodbye() {
        return "Goodbye, World!";
    }
}
