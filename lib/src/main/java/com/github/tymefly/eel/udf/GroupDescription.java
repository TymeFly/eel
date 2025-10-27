package com.github.tymefly.eel.udf;


import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This is an annotation used by the EelDoc Doclet to help generate end-user documentation for EEL functions.
 * A {@literal package-info.java} file annotated with GroupDescription will be used to generate an overview
 * for all the functions in a particular group. This is analogous to a package description in Java
 * @since 3.1
 */
@Target(ElementType.PACKAGE)
@Retention(RetentionPolicy.RUNTIME)
public @interface GroupDescription {
    /**
     * Returns the EEL group name
     * @return the EEL group name
     */
    String value();
}
