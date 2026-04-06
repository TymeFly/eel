package com.github.tymefly.eel.doc.source;

import java.util.Arrays;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.TypeVariable;
import javax.lang.model.util.Elements;

import com.github.tymefly.eel.doc.context.EelDocContext;

/**
 * Helper class used to map a Javadoc signature into an element.
 */
class Resolver {
    private static final String VAR_ARG_SUFFIX = "...";
    private static final String ARRAY_SUFFIX = "[]";

    private final EelDocContext context;
    private final Element element;
    private final Map<String, Element> elementCache;
    private final Map<ExecutableElement, String> methodCache;


    Resolver(@Nonnull EelDocContext context, @Nonnull Element element) {
        this.context = context;
        this.element = element;
        this.elementCache = new WeakHashMap<>();
        this.methodCache = new WeakHashMap<>();
    }


    /**
     * Resolve the signature into a Java element. Return {@literal null} if the signature doesn't map to an element
     * @param signature     signature of an element as seen in a Javadoc.
     * @return the element associated with the {@code signature}, or {@literal null} if there is no such element
     */
    @Nullable
    Element resolve(@Nonnull String signature) {
        // Cache the resolved elements to save recalculating them
        return elementCache.computeIfAbsent(signature, this::resolveHelper);
    }

    @Nullable
    private Element resolveHelper(@Nonnull String signature) {
        Element found;
        TypeElement typeElement;

        int hashIndex = signature.indexOf('#');
        String classPart;
        String memberPart;

        if (hashIndex >= 0) {
            classPart = signature.substring(0, hashIndex);
            memberPart = signature.substring(hashIndex + 1);
        } else {
            classPart = signature;
            memberPart = null;
        }

        if (classPart.isEmpty()) {      // Only a member is specified — resolve in current type
            typeElement = findEnclosingType();
        } else {
            typeElement = resolveTypeElement(classPart);
        }

        if (typeElement == null) {
            found = null;
        } else if (memberPart == null || memberPart.isEmpty()) {
            found = typeElement;
        } else {
            String normalised = normaliseSignature(memberPart);

            found = findMember(typeElement, normalised);
        }

        return found;
    }

    @Nullable
    private Element findMember(@Nonnull TypeElement typeElement, @Nonnull String signature) {
        Element found = null;

        for (var test : typeElement.getEnclosedElements()) {
            String candidate;

            if (test instanceof ExecutableElement executable) {
                candidate = executableSignature(executable);
            } else {
                candidate = test.getSimpleName().toString();
            }

            if (signature.equals(candidate)) {
                found = test;
                break;
            }
        }

        return found;
    }


    @Nonnull
    private String executableSignature(@Nonnull ExecutableElement method) {
        // Cache each method signature we generate. We should do this independently of the element cache
        // as multiple elements may be able to refer to the same method

        return methodCache.computeIfAbsent(method, m -> {
            String params = m.getParameters()
                .stream()
                .map(this::formatParameter)
                .collect(Collectors.joining(","));

            return m.getSimpleName().toString() + "(" + params + ")";
        });
    }

    @Nonnull
    private String formatParameter(@Nonnull VariableElement param) {
        TypeMirror type = param.asType();
        StringBuilder arraySuffix = new StringBuilder();

        while (type instanceof ArrayType arrayType) {
            type = arrayType.getComponentType();
            arraySuffix.append(ARRAY_SUFFIX);
        }

        return formatSimpleParameter(type) + arraySuffix;
    }

    @Nonnull
    private String formatSimpleParameter(@Nonnull TypeMirror type) {
        String name;

        if (type instanceof DeclaredType declared) {
            name = declared.asElement().getSimpleName().toString();
        } else if (type instanceof TypeVariable typeVar) {
            name = typeVar.asElement().getSimpleName().toString();
        } else {                            // Primitive type
            name = type.toString();
        }

        return name;
    }


    /** Normalise input signature string (handles fully qualified, simple names, and varargs ...) */
    @Nonnull
    private String normaliseSignature(@Nonnull String signature) {
        String normalised;
        int index = signature.indexOf('(');

        if (index < 0) {                            // field or type
            normalised = signature;
        } else {
            String name = signature.substring(0, index);
            String[] params = signature.substring(index + 1, signature.length() - 1)
                .split(",");
            String normalisedParams = Arrays.stream(params)
                .map(String::trim)
                .map(this::normaliseTypeName)
                .collect(Collectors.joining(","));

            normalised = name + "(" + normalisedParams + ")";
        }

        return normalised;
    }

    @Nonnull
    private String normaliseTypeName(@Nonnull String name) {
        StringBuilder arraySuffix = new StringBuilder();
        name = name.trim();

        if (name.endsWith(VAR_ARG_SUFFIX)) {
            int end = name.length() - VAR_ARG_SUFFIX.length();

            name = name.substring(0, end);
            arraySuffix.append(ARRAY_SUFFIX);
        }

        while (name.endsWith(ARRAY_SUFFIX)) {
            int end = name.length() - ARRAY_SUFFIX.length();

            name = name.substring(0, end).trim();
            arraySuffix.append(ARRAY_SUFFIX);
        }

        return simpleNameFromString(name) + arraySuffix;
    }

    /** Strip package prefix, keep nested types */
    @Nonnull
    private String simpleNameFromString(@Nonnull String typeStr) {
        int lastDot = typeStr.lastIndexOf('.');

        return (lastDot >= 0 ? typeStr.substring(lastDot + 1) : typeStr);
    }

    @Nullable
    private TypeElement resolveTypeElement(@Nonnull String name) {
        Elements elementUtils = context.elementUtils();
        TypeElement typeElement;

        // Check if type name is specified directly
        typeElement = elementUtils.getTypeElement(name);

        if (typeElement == null) {                                              // Check java.lang
            typeElement = elementUtils.getTypeElement("java.lang." + name);
        }

        if (typeElement == null) {                                              // Current package
            PackageElement packageElement = elementUtils.getPackageOf(element);

            if (!packageElement.isUnnamed()) {
                String packageName = packageElement.getQualifiedName().toString();

                typeElement = elementUtils.getTypeElement(packageName  + "." + name);
            }
        }

        // Enclosing type
        if (typeElement == null) {
            TypeElement enclosing = findEnclosingType();

            if (enclosing != null) {                                            // Check nested types
                for (var enclosed : enclosing.getEnclosedElements()) {
                    if (enclosed.getSimpleName().contentEquals(name) && enclosed instanceof TypeElement) {
                        typeElement = (TypeElement) enclosed;

                        break;
                    }
                }

                if (typeElement == null) {                                      // Check qualified inner class name
                    String fullName = enclosing.getQualifiedName().toString() + "$" + name;
                    typeElement = elementUtils.getTypeElement(fullName);
                }
            }
        }

        return typeElement;
    }


    @Nullable
    private TypeElement findEnclosingType() {
        Element test = element;

        while ((test != null) && !(test instanceof TypeElement)) {
            test = test.getEnclosingElement();
        }

        return (TypeElement) test;
    }
}
