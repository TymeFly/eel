package com.github.tymefly.eel.udf;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * An optional annotation that can be applied to a parameter of a method annotated as {@link EelFunction}.
 * If the compiled expression does not have a value for this parameter then the value in this annotation is used.
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface DefaultArgument {
    /**
     * Returns the optional default value for this argument
     * @return the optional default value for this argument
     */
    String value();
}
