package com.abidev.framework.validations;

import com.abidev.annotations.validation.Constraint;
import com.abidev.annotations.validation.Size;

import java.lang.reflect.Field;

@Constraint
public class SizeValidator implements ConstraintValidator<Size> {

    @Override
    public Class<Size> supports() {
        return Size.class;
    }

    @Override
    public String validate(Size ann, Object value, Field field) {
        System.out.println("Validating field: " + field.getName() + " with Size constraints: min=" + ann.min() + ", max=" + ann.max());
        if (value == null) return null;

        int len = value.toString().length();

        if (len < ann.min() || len > ann.max()) {
            return field.getName() + " " + ann.message();
        }

        return null;
    }

}
