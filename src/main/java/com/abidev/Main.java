package com.abidev;

import com.abidev.framework.AbiFramework;

public class Main {
    public static void main(String[] args) throws Exception {

        AbiFramework framework = new AbiFramework();

        framework.scan("com.abidev");

        System.out.println(framework.callRoute("/hello"));    // Output: Hello, World!
        System.out.println(framework.callRoute("/goodbye"));  // Output: Goodbye, World!
        System.out.println(framework.callRoute("/unknown"));   // Output: 404 Not Found
    }
}