package com.abidev.framework.validations;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;

public interface ConstraintValidator<A extends Annotation> {

    /**
     * Returns the annotation class that this validator supports.
     *
     * @return the supported annotation class
     */
    Class<A> supports();

    /**
     * Validates the given value against the specified annotation.
     *
     * @param annotation the annotation instance
     * @param value      the value to be validated
     * @param field      the field being validated
     * @return an error message if validation fails, or null if validation passes
     */
    String validate(A annotation, Object value, Field field);

}
