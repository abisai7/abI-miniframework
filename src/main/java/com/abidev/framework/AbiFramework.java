package com.abidev.framework;

import com.abidev.annotations.Component;
import com.abidev.annotations.Route;
import com.abidev.helpers.RouteHandler;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class AbiFramework {

    private Map<String, Object> components = new HashMap<>();
    private Map<String, RouteHandler> routes = new HashMap<>();

    public void scan(String packageName) throws URISyntaxException, ClassNotFoundException, NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        String path = packageName.replace('.', '/');
        URL packageURL = Thread.currentThread().getContextClassLoader().getResource(path);

        if (packageURL == null) {
            throw new RuntimeException("Package not found: " + packageName);
        }

        File folder = new File(packageURL.toURI());

        for (File file : folder.listFiles()) {
            if (!file.getName().endsWith(".class")) {
                continue;
            }

            String className = packageName + '.' + file.getName().replace(".class", "");
            Class<?> clazz = Class.forName(className);

            // Register components
            if (clazz.isAnnotationPresent(Component.class)) {
                Object instance = clazz.getDeclaredConstructor().newInstance();
                components.put(className, instance);

                // Register routes
                for (var method : clazz.getDeclaredMethods()) {
                    if(method.isAnnotationPresent(Route.class)) {
                        Route route = method.getAnnotation(Route.class);
                        routes.put(route.value(), new RouteHandler(instance, method));
                    }
                }
            }
        }
    }

    public String callRoute(String path) throws  Exception {
        RouteHandler handler = routes.get(path);

        if (handler == null) {
            return "404 Not Found";
        }

        return handler.invoke().toString();
    }

}
