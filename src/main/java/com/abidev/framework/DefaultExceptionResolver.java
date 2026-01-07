package com.abidev.framework;

import com.abidev.annotations.ResponseStatus;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.NoSuchElementException;

public class DefaultExceptionResolver {

    private static final Map<Class<? extends Exception>, Integer> mappings = new LinkedHashMap<>();

    static {
        mappings.put(IllegalArgumentException.class, 400);
        mappings.put(SecurityException.class, 403);
        mappings.put(NoSuchElementException.class, 404);
        mappings.put(UnsupportedOperationException.class, 405);
        mappings.put(Exception.class, 500); // fallback
    }

    public static int resolveStatus(Exception ex) {

        // 1️⃣ @ResponseStatus en la excepción o superclases
        Class<?> clazz = ex.getClass();
        while (clazz != null && clazz != Object.class) {

            ResponseStatus rs = clazz.getAnnotation(ResponseStatus.class);
            if (rs != null) {
                return rs.value();
            }

            clazz = clazz.getSuperclass();
        }

        // 2️⃣ Mapeo por tipo (ordenado)
        for (var entry : mappings.entrySet()) {
            if (entry.getKey().isAssignableFrom(ex.getClass())) {
                return entry.getValue();
            }
        }

        // 3️⃣ Fallback final
        return 500;
    }
}
