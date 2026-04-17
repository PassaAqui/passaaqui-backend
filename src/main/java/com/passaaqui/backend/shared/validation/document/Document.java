package com.passaaqui.backend.shared.validation.document;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = DocumentValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface Document {
    String message() default "Invalid document";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
