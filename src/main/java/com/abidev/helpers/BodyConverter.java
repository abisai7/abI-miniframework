package com.abidev.helpers;

import tools.jackson.databind.ObjectMapper;

public class BodyConverter {

    private static final ObjectMapper mapper = new ObjectMapper();

    public static <T> T convert(String body, Class<T> clazz) throws Exception {
        try {
            return mapper.readValue(body, clazz);
        } catch (Exception e) {
            throw new Exception("Failed to convert request body to " + clazz.getName(), e);
        }
    }
}
