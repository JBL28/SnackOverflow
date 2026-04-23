package com.snackoverflow.common.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.regex.Pattern;

public class SafePasswordValidator implements ConstraintValidator<SafePassword, String> {

    private static final int MIN_LENGTH = 6;
    private static final Pattern ALLOWED = Pattern.compile(
            "^[A-Za-z0-9!@#$%^&*()_+\\-=\\[\\]{};:,.?]+$"
    );
    private static final Pattern HAS_LETTER = Pattern.compile("[A-Za-z]");
    private static final Pattern HAS_DIGIT = Pattern.compile("[0-9]");

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null) return false;
        if (value.length() < MIN_LENGTH) return false;
        if (!HAS_LETTER.matcher(value).find()) return false;
        if (!HAS_DIGIT.matcher(value).find()) return false;
        return ALLOWED.matcher(value).matches();
    }
}
