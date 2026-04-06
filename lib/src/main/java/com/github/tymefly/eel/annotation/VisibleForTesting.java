package com.github.tymefly.eel.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


/**
 * Indicates that a method, constructor, class, or field has been given more permissive access
 * than necessary (default/package-private instead of private) so that it can be unit tested.
 * <p>
 * <b>Clients should NOT reference the annotated element.</b>
 * <p>
 * This annotation is a more limited alternative to the Guava {@code @VisibleForTesting} annotation.
 * Guava is not used here to avoid including a large dependency for a single annotation.
 */
@Retention(RetentionPolicy.SOURCE)
@Target({ElementType.METHOD, ElementType.CONSTRUCTOR, ElementType.TYPE, ElementType.FIELD})
@Documented
public @interface VisibleForTesting {
}