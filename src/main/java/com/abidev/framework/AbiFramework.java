package com.abidev.framework;

import com.abidev.annotations.Component;
import com.abidev.annotations.Inject;
import com.abidev.annotations.Route;
import com.abidev.helpers.RouteHandler;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class AbiFramework {

    private Map<Class<?>, Object> components = new HashMap<>();
    private Map<String, RouteHandler> routes = new HashMap<>();

    public void scan(String packageName) throws Exception {

        String path = packageName.replace('.', '/');
        URL packageURL = Thread.currentThread().getContextClassLoader().getResource(path);

        if (packageURL == null) {
            throw new RuntimeException("Package not found: " + packageName);
        }

        File folder = new File(packageURL.toURI());

        // Recursively scan the directory for classes
        scanDirectory(folder, packageName);

        // After scanning, perform dependency injection
        // This is deprecated and will be removed in future versions.
        injectDependencies();

        // Register routes after dependencies have been injected
        registerRoutes();
    }

    private void scanDirectory(File dir, String currentPackage) throws Exception {
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
                    Object instance = createInstance(clazz);
                    components.put(clazz, instance);
                }
            }
        }
    }

    private Object createInstance(Class<?> clazz) throws Exception {
        if (components.containsKey(clazz)) {
            return components.get(clazz);
        }

        Constructor<?> constructor = clazz.getDeclaredConstructors()[0];
        Class<?>[] paramTypes = constructor.getParameterTypes();

        Object[] args = new Object[paramTypes.length];
        for (int i = 0; i < paramTypes.length; i++) {
            args[i] = createInstance(paramTypes[i]);
        }

        Object instance = constructor.newInstance(args);
        components.put(clazz, instance);
        return instance;
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
                    routes.put(r.value(), new RouteHandler(instance, method, r.value()));
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

    public String callRoute(String path) throws Exception {
        for (var entry : routes.entrySet()) {
            String pattern = entry.getKey();
            RouteHandler handler = entry.getValue();

            if (!handler.matches(path)) {
                continue;
            }

            String[] pathParts = path.split("/");
            String[] patternParts = pattern.split("/");

            boolean fits = true;
            for (int i = 0; i < patternParts.length; i++) {
                if (!patternParts[i].startsWith("{") && !patternParts[i].equals(pathParts[i])) {
                    fits = false;
                    break;
                }
            }

            if (fits) {
                return (String) handler.invokeWithPath(path);
            }
        }

        return "404 Not Found";
    }

}
