package com.github.tymefly.eel.udf;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.github.tymefly.eel.builder.EelBuilder;

/**
 * This annotation is used EEL Functions that are located by a call to
 * {@link EelBuilder#withUdfPackage(Package)}.
 * Individual EEL Functions that do not have this annotation can still be made available by calling
 * {@link EelBuilder#withUdfClass(Class)}
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface PackagedEelFunction {
}
