package com.abidev.framework;

import com.abidev.annotations.Component;
import com.abidev.annotations.Inject;
import com.abidev.annotations.Route;
import com.abidev.helpers.RouteHandler;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class AbiFramework {

    private Map<Class<?>, Object> components = new HashMap<>();
    private Map<String, RouteHandler> routes = new HashMap<>();

    public void scan(String packageName) throws URISyntaxException, ClassNotFoundException, NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {

        String path = packageName.replace('.', '/');
        URL packageURL = Thread.currentThread().getContextClassLoader().getResource(path);

        if (packageURL == null) {
            throw new RuntimeException("Package not found: " + packageName);
        }

        File folder = new File(packageURL.toURI());

        // Recursively scan the directory for classes
        scanDirectory(folder, packageName);

        // After scanning, perform dependency injection
        injectDependencies();

        // Register routes after dependencies have been injected
        registerRoutes();
    }

    private void scanDirectory(File dir, String currentPackage) throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        File[] files = dir.listFiles();
        if (files == null) return;

        for (File file : files) {
            if (file.isDirectory()) {
                String subPackage = currentPackage + "." + file.getName();
                scanDirectory(file, subPackage);
            } else if (file.getName().endsWith(".class")) {
                String className = currentPackage + "." + file.getName().replace(".class", "");
                Class<?> clazz = Class.forName(className);

                if (clazz.isAnnotationPresent(Component.class)) {
                    Object instance = clazz.getDeclaredConstructor().newInstance();
                    components.put(clazz, instance);
                }
            }
        }
    }



    /**
     * Registers routes by scanning methods of registered components for the @Route annotation.
     */
    private void registerRoutes() {
        for (var entry : components.entrySet()) {
            Object instance = entry.getValue();
            for (var method : entry.getKey().getDeclaredMethods()) {

                if (method.isAnnotationPresent(Route.class)) {
                    Route r = method.getAnnotation(Route.class);
                    System.out.println("Registered route: " + r.value() + " -> " + method.getName());
                    routes.put(r.value(), new RouteHandler(instance, method));
                }
            }
        }
    }

    /**
     * Injects dependencies into fields annotated with @Inject.
     * @throws IllegalAccessException if a field cannot be accessed
     */
    private void injectDependencies() throws IllegalAccessException {
        for (var entry : components.entrySet()) {
            Object instance = entry.getValue();
            Class<?> clazz = entry.getKey();
            for (Field field : clazz.getDeclaredFields()) {

                if (field.isAnnotationPresent(Inject.class)) {
                    Class<?> dependencyType = field.getType();
                    Object dependency = components.get(dependencyType);
                    if (dependency == null) {
                        throw  new RuntimeException("Unsatisfied dependency: " + dependencyType.getName());
                    }

                    field.setAccessible(true);
                    field.set(instance, dependency);
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
