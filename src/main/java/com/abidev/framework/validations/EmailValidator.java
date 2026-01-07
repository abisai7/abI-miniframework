package com.abidev.framework.validations;


import com.abidev.annotations.validation.Constraint;
import com.abidev.annotations.validation.Email;

import java.lang.reflect.Field;
import java.util.regex.Pattern;

@Constraint
public class EmailValidator implements ConstraintValidator<Email> {

    private static final Pattern EMAIL =
            Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");

    @Override
    public Class<Email> supports() {
        return Email.class;
    }

    @Override
    public String validate(Email ann, Object value, Field field) {

        if (value == null) return null;

        if (!EMAIL.matcher(value.toString()).matches()) {
            return field.getName() + " " + ann.message();
        }

        return null;
    }
}