package com.abidev.framework;

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
        for (var entry : mappings.entrySet()) {
            if (entry.getKey().isAssignableFrom(ex.getClass())) {
                return entry.getValue();
            }
        }

        return 500;
    }

}
