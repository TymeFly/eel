package com.github.tymefly.eel.udf;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * EEL functions must denote their entry point by annotating a public method with this annotation.
 * The requirements for the parameters are defined in the {@literal User Defined Functions} documentation
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface EelFunction {
    /**
     * Returns the EEL name for this function.
     * @return the EEL name for this function
     */
    String value();
}