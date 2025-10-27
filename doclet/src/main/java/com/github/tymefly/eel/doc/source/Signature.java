package com.github.tymefly.eel.doc.source;

import javax.annotation.Nonnull;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;

/**
 * Utility class used to generate the Java signature for source code elements.
 */
class Signature {
    private Signature() {
    }


    @Nonnull
    static String of(@Nonnull Element element) {
        String result;

        if (element instanceof PackageElement packageElement) {
            result = packageElement.getQualifiedName().toString();
        } else if (element instanceof TypeElement typeElement) {
            result = typeElement.getQualifiedName().toString();
        } else if (element instanceof ExecutableElement executableElement) {
            result = methodSignature(executableElement);
        } else if (element.getKind().isField()) {
            result = element.getEnclosingElement().toString() + "." + element.getSimpleName().toString();
        } else {
            result = element.toString(); // fallback
        }

        return result;
    }


    @Nonnull
    private static String methodSignature(@Nonnull ExecutableElement method) {
        Element enclosing = method.getEnclosingElement();
        StringBuilder builder = new StringBuilder(enclosing.toString())
            .append('.')
            .append(method.getSimpleName().toString())
            .append('(');
        String separator = "";

        for (var variable : method.getParameters()) {
            builder.append(separator)
                .append(variable.asType().toString());

            separator = ", ";
        }

        builder.append(')');

        return builder.toString();
    }
}
