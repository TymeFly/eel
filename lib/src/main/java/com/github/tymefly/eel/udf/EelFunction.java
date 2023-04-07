package com.github.tymefly.eel.udf;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * EEL functions are required to denote their entry point by annotating a public method with this annotation
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface EelFunction {
    /**
     * Returns the EEL name for this function.
     * @return the EEL name for this function.
     */
    String name();
}
