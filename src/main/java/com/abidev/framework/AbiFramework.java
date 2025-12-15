package com.abidev.framework;

import com.abidev.annotations.Component;
import com.abidev.annotations.Inject;
import com.abidev.annotations.Route;
import com.abidev.annotations.Scope;
import com.abidev.helpers.RouteHandler;
import com.abidev.middleware.HandlerInterceptor;
import com.abidev.middleware.RequestContext;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.util.*;
import java.util.function.Supplier;

public class AbiFramework {

    private Map<Class<?>, Object> singletons = new HashMap<>();
    private Set<Class<?>> prototypeBeans = new HashSet<>();
    private Set<Class<?>> componentClasses = new HashSet<>();
    private final Map<Class<?>, Boolean> prototypeGraphCache = new HashMap<>();

    private Map<String, RouteHandler> routes = new HashMap<>();
    private final List<HandlerInterceptor> interceptors = new ArrayList<>();




    public void scan(String packageName) throws Exception {

        String path = packageName.replace('.', '/');
        URL packageURL = Thread.currentThread().getContextClassLoader().getResource(path);

        if (packageURL == null) {
            throw new RuntimeException("Package not found: " + packageName);
        }

        File folder = new File(packageURL.toURI());

        // Recursively scan the directory for classes
        scanDirectory(folder, packageName);

        // Create singleton instances
        for (Class<?> clazz : new HashSet<>(componentClasses)) {
            if (!prototypeBeans.contains(clazz) && !singletons.containsKey(clazz)) {
                Object instance = createInstance(clazz);
                singletons.put(clazz, instance);
            }
        }

        // Register routes after dependencies have been injected
        registerRoutes();

        System.out.println("\n\n================= AbiFramework Scan Report ================");
        System.out.println("\tScanned package: " + packageName);
        System.out.println("\tFound components: " + componentClasses.size());
        System.out.println("\tSingleton instances: " + singletons.size());
        System.out.println("\tPrototype beans: " + prototypeBeans.size());
        System.out.println("\tRegistered routes: " + routes.size());
        System.out.println("\tRegistered interceptors: " + interceptors.size());
        System.out.println("============================================================\n\n");
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
                    componentClasses.add(clazz);
                    Scope scope = clazz.getAnnotation(Scope.class);

                    if (scope == null || scope.value().equals(Scope.SINGLETON)) {
                        // Only singleton by default
                        // Handled later in the scan method
                    } else if (scope.value().equals(Scope.PROTOTYPE)) {
                        prototypeBeans.add(clazz);
                    }
                }

                if (HandlerInterceptor.class.isAssignableFrom(clazz)
                        && !clazz.isInterface()
                        && !Modifier.isAbstract(clazz.getModifiers())) {

                    HandlerInterceptor interceptor =
                            (HandlerInterceptor) createInstance(clazz);

                    interceptors.add(interceptor);
                }
            }
        }
    }

    private Object createInstance(Class<?> clazz) throws Exception {
        return createInstance(clazz, false);
    }

    private Object createInstance(Class<?> clazz, boolean forceNew) throws Exception {

        // Prevent instantiation of interfaces and abstract classes, we don't manage them
        if (clazz.isInterface() || java.lang.reflect.Modifier.isAbstract(clazz.getModifiers())) {
            throw new RuntimeException("Cannot instantiate interface or abstract class: " + clazz.getName());
        }

        // Check if the class is a singleton and already instantiated
        if (!prototypeBeans.contains(clazz) && singletons.containsKey(clazz) && !forceNew) {
            return singletons.get(clazz);
        }

        // Resolve constructor
        Constructor<?> constructor = chooseConstructor(clazz);
        Class<?>[] paramTypes = constructor.getParameterTypes();
        Object[] args = new Object[paramTypes.length];
        for (int i = 0; i < paramTypes.length; i++) {
            Class<?> paramType = paramTypes[i];
            args[i] = createInstance(paramType);
        }

        Object instance = constructor.newInstance(args);

        performFieldInjection(instance, clazz);

        // If singleton, store the instance
        if (!prototypeBeans.contains(clazz) && !forceNew) {
            singletons.put(clazz, instance);
        }

        return instance;
    }

    private Constructor<?> chooseConstructor(Class<?> clazz) {
        Constructor<?>[] constructors = clazz.getDeclaredConstructors();

        for (Constructor<?> c : constructors) {
            if (c.getParameterCount() == 0) {
                c.setAccessible(true);
                return c;
            }
        }

        // If no default constructor, return the first one
        Constructor<?> constructor = constructors[0];
        constructor.setAccessible(true);
        return constructor;
    }

    /**
     * Performs field injection for the given instance and class.
     * @param instance the object instance to inject dependencies into
     * @param clazz the class of the object
     * @throws IllegalAccessException if a field cannot be accessed
     */
    private void performFieldInjection(Object instance, Class<?> clazz) throws IllegalAccessException {
        for (Field field : clazz.getDeclaredFields()) {
            if (field.isAnnotationPresent(Inject.class)) {
                field.setAccessible(true);
                Class<?> dependencyType = field.getType();
                try {
                    Object dependency = createInstance(dependencyType);
                    field.set(instance, dependency);
                } catch (Exception e) {
                    throw new RuntimeException("Failed to inject field " + field.getName()
                            + " on " + clazz.getName() + " -> " + e.getMessage(), e);
                }
            }
        }
    }

    private boolean hasPrototypeDependency(Class<?> clazz) {
        return prototypeGraphCache.computeIfAbsent(clazz, this::scanPrototypeDeps);
    }

    private boolean scanPrototypeDeps(Class<?> clazz) {
        try {
            Constructor<?> constructor = chooseConstructor(clazz);
            for (Class<?> p : constructor.getParameterTypes()) {
                if (prototypeBeans.contains(p)) {
                    return true;
                }
            }
            for (Field field : clazz.getDeclaredFields()) {
                if (field.isAnnotationPresent(Inject.class) && prototypeBeans.contains(field.getType())) {
                    return true;
                }
            }
        } catch (Exception ignored) {
            return false;
        }
        return false;
    }

    /**
     * Registers routes by scanning methods of registered components for the @Route annotation.
     */
    private void registerRoutes() {
        for (Class<?> clazz : componentClasses) {
            for (var method : clazz.getDeclaredMethods()) {
                if (method.isAnnotationPresent(Route.class)) {
                    Route route = method.getAnnotation(Route.class);
                    String pattern = route.value();

                    final Supplier<Object> supplier = () -> {
                        try {
                            if (prototypeBeans.contains(clazz) || hasPrototypeDependency(clazz)) {
                                return createInstance(clazz, true);
                            } else {
                                if (!singletons.containsKey(clazz)) {
                                    Object instance = createInstance(clazz);
                                    singletons.put(clazz, instance);
                                }

                                return singletons.get(clazz);
                            }
                        } catch (Exception e) {
                            throw new RuntimeException("Failed to create instance for route: " + pattern);
                        }
                    };

                    System.out.println("Registered route: " + pattern + " -> " + clazz.getSimpleName() + "." + method.getName());
                    routes.put(pattern, new RouteHandler(supplier, method, pattern));
                }
            }
        }
    }

    public String callRoute(String path) throws Exception {

        for (var entry : routes.entrySet()) {

            RouteHandler handler = entry.getValue();

            if (!handler.matches(path)) {
                continue;
            }

            RequestContext ctx = handler.createContext(path);

            // ===== PRE HANDLE =====
            for (HandlerInterceptor interceptor : interceptors) {
                if (!interceptor.preHandle(ctx)) {
                    return "403 Forbidden";
                }
            }

            Object result = null;
            Exception error = null;

            try {
                // ===== CONTROLLER =====
                result = handler.invoke(ctx);

            } catch (Exception ex) {
                error = ex;
            }

            // ===== POST HANDLE =====
            if (error == null) {
                for (HandlerInterceptor interceptor : interceptors) {
                    interceptor.postHandle(ctx, result);
                }
            }

            // ===== AFTER COMPLETION =====
            for (HandlerInterceptor interceptor : interceptors) {
                interceptor.afterCompletion(ctx, error);
            }

            if (error != null) {
                throw error;
            }

            return (String) result;
        }

        return "404 Not Found";
    }

    private Object instantiatePlainObject(Class<?> clazz) throws Exception {
        Constructor<?> constructor = chooseConstructor(clazz);
        constructor.setAccessible(true);
        return constructor.newInstance();
    }

    /**
     * Retrieves a singleton instance of the specified class.
     *
     * @param clazz the class of the singleton to retrieve
     * @return an Optional containing the singleton instance if it exists, or empty if not found
     */
    public Optional<Object> getSingleton(Class<?> clazz) {
        return Optional.ofNullable(singletons.get(clazz));
    }

    /**
     * Retrieves an unmodifiable set of all registered route patterns.
     *
     * @return a set of registered route patterns
     */
    public Set<String> getRoutes() {
        return Collections.unmodifiableSet(routes.keySet());
    }

    public void addInterceptor(HandlerInterceptor interceptor) {
        interceptors.add(interceptor);
    }
}
