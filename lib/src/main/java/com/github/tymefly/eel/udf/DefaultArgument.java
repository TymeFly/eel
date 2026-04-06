package com.github.tymefly.eel.udf;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * An optional annotation applied to a parameter of a method annotated with {@link EelFunction}.
 * If the compiled expression does not provide a value for this parameter, the value specified
 * in this annotation is used instead.
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface DefaultArgument {
    /**
     * Returns the optional default value for this argument.
     * @return the default value to use if the parameter is not supplied
     */
    String value();

    /**
     * An optional description for documentation purposes.
     * {@code EelDoc} can use this to describe the default value.
     * @return a description of the default value
     */
    String description() default "";
}