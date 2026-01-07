package com.abidev.framework.validations;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public class ValidatorEngine {


    @SuppressWarnings("unchecked")
    public static void validate(Object target) {
        List<String> errors = new ArrayList<>();

        if (target == null) {
            throw new ValidationException(errors);
        }

        Class<?> clazz = target.getClass();

        for (Field field : clazz.getDeclaredFields()) {
            field.setAccessible(true);
            Object value;

            try {
                value = field.get(target);
            } catch (IllegalAccessException e) {
                continue; // Skip inaccessible fields
            }

            for (Annotation ann : field.getAnnotations()) {
                ConstraintValidator<Annotation> validator = (ConstraintValidator<Annotation>) ValidatorRegistry.get(ann.annotationType());

                if (validator != null) {
                    String error = validator.validate(ann, value, field);
                    if (error != null) {
                        errors.add(error);
                    }
                }

                if (!errors.isEmpty()) {
                    throw new ValidationException(errors);
                }
            }
        }
    }

}
