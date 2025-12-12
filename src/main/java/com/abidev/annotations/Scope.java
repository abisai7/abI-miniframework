package com.abidev.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Scope {

    public static final String SINGLETON = "singleton";
    public static final String PROTOTYPE = "prototype";

    String value() default SINGLETON;
}
