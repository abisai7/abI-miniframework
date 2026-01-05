package com.abidev.http;

import com.abidev.framework.AbiFramework;
import com.sun.net.httpserver.HttpServer;

import java.net.InetSocketAddress;
import java.util.Arrays;

public class AbiHttpServer {

    private final AbiFramework framework;
    private HttpServer server;

    public AbiHttpServer(AbiFramework framework) {
        this.framework = framework;
    }

    public void start(int port) throws Exception {
        server = HttpServer.create(new InetSocketAddress(port), 0);
        server.createContext("/", exchange -> {
           String path = exchange.getRequestURI().getPath();
           String method = exchange.getRequestMethod();

           if (!Arrays.asList("GET", "POST").contains(method)) {
               exchange.sendResponseHeaders(405, -1); // Method Not Allowed
               return;
           }

           String response;
           try {
               response = framework.callRoute(path, exchange);
           } catch (Exception e) {
               e.printStackTrace();
               response = "500 Internal Server Error";
           }

           byte[] bytes = response.getBytes();
           exchange.sendResponseHeaders(200, bytes.length);
           exchange.getResponseBody().write(bytes);
           exchange.close();
        });
        
        server.start();
        System.out.println("🚀 AbiFramework running on http://localhost:" + port);
    }
}
