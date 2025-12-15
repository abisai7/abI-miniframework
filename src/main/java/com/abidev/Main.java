package com.abidev;

import com.abidev.framework.AbiFramework;

public class Main {
    public static void main(String[] args) throws Exception {

        AbiFramework framework = new AbiFramework();

        framework.scan("com.abidev");

        System.out.println(framework.callRoute("/hello"));    // Output: Hello, World!
        System.out.println(framework.callRoute("/goodbye"));  // Output: Goodbye, World!
        System.out.println(framework.callRoute("/hello/Alice")); // Output: Hello, Alice!
        System.out.println(framework.callRoute("/user/42"));  // Output: User ID: 42
        System.out.println(framework.callRoute("/user/42/profile")); // Output: Profile for user ID: 42
        System.out.println(framework.callRoute("/unknown"));   // Output: 404 Not Found

        System.out.println(framework.callRoute("/time")); // Output: Request timestamp: <timestamp>
        Thread.sleep(1000);
        System.out.println(framework.callRoute("/time")); // Output: Request timestamp: <different timestamp>

        System.out.println(framework.callRoute("/admin")); // Output: 403 Forbidden!
    }
}