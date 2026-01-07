package com.abidev.framework.validations;

import com.abidev.annotations.validation.Constraint;
import com.abidev.annotations.validation.NotNull;

import java.lang.reflect.Field;

@Constraint
public class NotNullValidator implements ConstraintValidator<NotNull> {

    @Override
    public Class<NotNull> supports() {
        return NotNull.class;
    }

    @Override
    public String validate(NotNull ann, Object value, Field field) {
        if (value == null) {
            return field.getName() + " " + ann.message();
        }

        return null;
    }

}
