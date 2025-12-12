package com.abidev.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This annotation is used to mark fields for dependency injection.
 *
 * @deprecated This annotation is deprecated and will be removed in future versions.
 * Is recommended to use constructor injection or other dependency injection frameworks.
 */
@Deprecated()
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Inject {
}
