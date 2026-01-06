package com.abidev.http;

import com.abidev.framework.AbiFramework;
import com.sun.net.httpserver.HttpServer;
import tools.jackson.databind.ObjectMapper;

import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Map;

public class AbiHttpServer {

    private final AbiFramework framework;
    private HttpServer server;

    private static final ObjectMapper mapper = new ObjectMapper();

    public AbiHttpServer(AbiFramework framework) {
        this.framework = framework;
    }

    public void start(int port) throws Exception {

        server = HttpServer.create(new InetSocketAddress(port), 0);

        server.createContext("/", exchange -> {

            String path = exchange.getRequestURI().getPath();
            String method = exchange.getRequestMethod();

            if (!Arrays.asList("GET", "POST").contains(method)) {
                exchange.sendResponseHeaders(405, -1);
                exchange.close();
                return;
            }

            HandlerResult result;

            try {
                result = framework.callRoute(path, exchange);

            } catch (Exception e) {
                e.printStackTrace();
                result = new HandlerResult(
                        500,
                        Map.of("Content-Type", "text/plain"),
                        "Internal Server Error"
                );
            }

            if (result.headers() != null) {
                result.headers().forEach(
                        (k, v) -> exchange.getResponseHeaders().add(k, v)
                );
            }

            byte[] bodyBytes = serializeBody(result.body(), exchange);
            exchange.sendResponseHeaders(
                    result.status(),
                    bodyBytes.length
            );

            try (OutputStream os = exchange.getResponseBody()) {
                os.write(bodyBytes);
            }

            exchange.close();
        });

        server.start();
        System.out.println("🚀 AbiFramework running on http://localhost:" + port);
    }

    private byte[] serializeBody(Object body, com.sun.net.httpserver.HttpExchange exchange) {

        if (body == null) {
            exchange.getResponseHeaders().add("Content-Type", "text/plain");
            return new byte[0];
        }

        try {
            // String → text/plain
            if (body instanceof String s) {
                exchange.getResponseHeaders().add("Content-Type", "text/plain; charset=UTF-8");
                return s.getBytes(StandardCharsets.UTF_8);
            }

            // byte[] → binary
            if (body instanceof byte[] bytes) {
                exchange.getResponseHeaders().add("Content-Type", "application/octet-stream");
                return bytes;
            }

            // Object → JSON
            exchange.getResponseHeaders().add("Content-Type", "application/json; charset=UTF-8");
            return mapper.writeValueAsBytes(body);

        } catch (Exception e) {
            e.printStackTrace();
            exchange.getResponseHeaders().add("Content-Type", "text/plain");
            return "Serialization error".getBytes(StandardCharsets.UTF_8);
        }
    }
}
