package com.abidev.framework.validations;

import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class ValidatorRegistry {

    private static final Map<Class<?>, ConstraintValidator<?>> validators = new HashMap<>();

//    static {
//        register(new NotNullValidator());
//        register(new EmailValidator());
//        register(new SizeValidator());
//    }

    public static void register(ConstraintValidator<?> validator) {
        validators.put(validator.supports(), validator);
    }

    @SuppressWarnings("unchecked")
    public static <A extends Annotation> ConstraintValidator<A> get(Class<A> annotation) {
        return (ConstraintValidator<A>) validators.get(annotation);
    }

    public static Collection<ConstraintValidator<?>> all() {
        return validators.values();
    }
}
