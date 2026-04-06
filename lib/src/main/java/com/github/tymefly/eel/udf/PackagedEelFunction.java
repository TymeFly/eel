package com.github.tymefly.eel.udf;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.github.tymefly.eel.builder.EelBuilder;

/**
 * This annotation is used by {@link EelBuilder#withUdfPackage(Package)} to locate classes of EEL functions.
 * EEL functions in classes without this annotation can still be made available by calling
 * {@link EelBuilder#withUdfClass(Class)}.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface PackagedEelFunction {
}