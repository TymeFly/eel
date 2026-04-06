package com.github.tymefly.eel.doc.source;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.PrimitiveType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeVariable;
import javax.lang.model.util.Elements;

import com.github.tymefly.eel.doc.context.EelDocContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Unit test for {@link Resolver}
 */
public class ResolverTest {
    private Elements elementUtils;

    private ExecutableElement localFunc_0;
    private ExecutableElement localFunc_1;
    private ExecutableElement localFunc_4;
    private VariableElement localField;
    private VariableElement nestedField;
    private VariableElement innerField;
    private ExecutableElement eelEvaluate;
    private ExecutableElement stringLength;
    private ExecutableElement packageMethod;
    private TypeElement testClass;

    private Resolver resolver;


    @BeforeEach
    public void setUp() {
        elementUtils = mock(Elements.class);

        EelDocContext context = mock(EelDocContext.class, RETURNS_DEEP_STUBS);
        PackageElement packageElement = mockPackage("com.github.tymefly.eel.test");

        localFunc_0 = mockMethod("myMethod");
        localFunc_1 = mockMethod("methodVarArgs", mockVarArgs("int", true));
        localFunc_4 = mockMethod("myMethod",
            mockArg("String", true),
            mockArg("boolean", true),
            mockGenericArg("T"),
            mockVarArgs("int", true));
        localField = mockField("MY_FIELD");
        nestedField = mockField("NESTED_FIELD");
        innerField = mockField("INNER_FIELD");
        eelEvaluate = mockMethod("evaluate");
        stringLength = mockMethod("length");
        packageMethod = mockMethod("otherMethod");

        TypeElement eelClass = mockClass(packageElement, null, "Eel", eelEvaluate);
        TypeElement stringClass = mockClass(packageElement, null, "String", stringLength);
        TypeElement PackageClass = mockClass(packageElement, null, "Package", packageMethod);

        testClass = mockClass(packageElement, null, "testClass", localField, localFunc_0, localFunc_1, localFunc_4);

        mockClass(packageElement, testClass, "Nested", nestedField);
        mockClass(packageElement, testClass, "$Inner", innerField);

        when(context.elementUtils())
            .thenReturn(elementUtils);

        when(elementUtils.getTypeElement("com.github.tymefly.eel.Eel"))
            .thenReturn(eelClass);
        when(elementUtils.getTypeElement("java.lang.String"))
            .thenReturn(stringClass);
        when(elementUtils.getTypeElement("com.github.tymefly.eel.test.Package"))
            .thenReturn(PackageClass);

        resolver = new Resolver(context, localFunc_0);
    }


    @Nonnull
    private PackageElement mockPackage(@Nonnull String name) {
        PackageElement packageElement = mock(PackageElement.class, RETURNS_DEEP_STUBS);

        when(packageElement.getQualifiedName().toString())
            .thenReturn(name);

        return packageElement;
    }

    @Nonnull
    private TypeElement mockClass(@Nonnull PackageElement packageElement,
                                  @Nullable TypeElement parentClass,
                                  @Nonnull String name,
                                  Element... members) {
        TypeElement type = mock(TypeElement.class, RETURNS_DEEP_STUBS);
        String fullName;

        if (parentClass == null) {
            fullName = packageElement.getQualifiedName().toString() + "." + name;
        } else if (name.startsWith("$")) {
            fullName = parentClass.getQualifiedName().toString() + name;
        } else {
            fullName = parentClass.getQualifiedName().toString() + "." + name;
        }

        when(type.getQualifiedName().toString())
            .thenReturn(fullName);
        when(type.getSimpleName().contentEquals(name))
            .thenReturn(true);
        when(type.getEnclosedElements())
            .thenAnswer(i -> Arrays.asList(members));
        when(type.toString())
            .thenReturn("class: " + name);

        when(elementUtils.getTypeElement(fullName))
            .thenReturn(type);

        if (parentClass != null) {
            List<Element> children = new ArrayList<>(parentClass.getEnclosedElements());
            children.add(type);

            when(parentClass.getEnclosedElements())
                .thenAnswer(i -> children);

            when(type.getEnclosingElement())
                .thenReturn(parentClass);
        }

        for (var member : members) {
            when(member.getEnclosingElement())
                .thenReturn(type);

            when(elementUtils.getPackageOf(member))
                .thenReturn(packageElement);
        }

        return type;
    }

    @Nonnull
    private VariableElement mockField(@Nonnull String name) {
        VariableElement field = mock(VariableElement.class, RETURNS_DEEP_STUBS);

        when(field.getSimpleName().contentEquals(name))
            .thenReturn(true);
        when(field.getSimpleName().toString())
            .thenReturn(name);
        when(field.toString())
            .thenReturn("field: " + name);

        return field;
    }

    @Nonnull
    private ExecutableElement mockMethod(@Nonnull String name,
                                         @Nonnull VariableElement... parameters) {
        ExecutableElement method = mock(ExecutableElement.class, RETURNS_DEEP_STUBS);

        when(method.getSimpleName().contentEquals(name))
            .thenReturn(true);
        when(method.getSimpleName().toString())
            .thenReturn(name);
        when(method.getParameters())
            .thenAnswer(i -> Arrays.asList(parameters));
        when(method.toString())
            .thenReturn("method: " + name);

        return method;
    }

    @Nonnull
    private VariableElement mockArg(@Nonnull String typeName, boolean isPrimitive) {
        VariableElement param = mock(VariableElement.class);
        TypeKind kind = mock(TypeKind.class);
        DeclaredType type = mock(DeclaredType.class, RETURNS_DEEP_STUBS);

        when(param.asType())
            .thenReturn(type);
        when(type.getKind())
            .thenReturn(kind);
        when(kind.isPrimitive())
            .thenReturn(isPrimitive);
        when(type.asElement().getSimpleName().toString())
            .thenReturn(typeName);
        when(type.toString())
            .thenReturn(typeName);

        return param;
    }

    @Nonnull
    private VariableElement mockGenericArg(@Nonnull String typeName) {
        VariableElement param = mock(VariableElement.class);
        TypeVariable type = mock(TypeVariable.class, RETURNS_DEEP_STUBS);
        TypeKind kind = mock(TypeKind.class);

        when(param.asType())
            .thenReturn(type);
        when(type.getKind())
            .thenReturn(kind);
        when(type.asElement().getSimpleName().toString())
            .thenReturn(typeName);

        return param;
    }

    @Nonnull
    private VariableElement mockVarArgs(@Nonnull String typeName, boolean isPrimitive) {
        VariableElement param = mock(VariableElement.class);
        ArrayType arrayType = mock(ArrayType.class, RETURNS_DEEP_STUBS);
        PrimitiveType componentType = mock(PrimitiveType.class, RETURNS_DEEP_STUBS);
        TypeKind kind = mock(TypeKind.class);

        when(param.asType())
            .thenReturn(arrayType);
        when(arrayType.getComponentType())
            .thenReturn(componentType);
        when(componentType.getKind())
            .thenReturn(kind);
        when(kind.isPrimitive())
            .thenReturn(isPrimitive);
        when(componentType.toString())
            .thenReturn(typeName);

        return param;
    }



    /**
     * Unit test {@link Resolver#resolve(String)}
     */
    @Test
    public void resolve_local_field() {
        Element actual = resolver.resolve("#MY_FIELD");

        assertSame(localField, actual, "Failed to find method");
    }

    /**
     * Unit test {@link Resolver#resolve(String)}
     */
    @Test
    public void resolve_local_method_noArgs() {
        Element actual = resolver.resolve("#myMethod()");

        assertSame(localFunc_0, actual, "Failed to find method");
    }

    /**
     * Unit test {@link Resolver#resolve(String)}
     */
    @Test
    public void resolve_local_method_1arg() {
        Element actual = resolver.resolve("#methodVarArgs(int...)");

        assertSame(localFunc_1, actual, "Failed to find method");
    }

    /**
     * Unit test {@link Resolver#resolve(String)}
     */
    @Test
    public void resolve_local_method_4args() {
        Element actual = resolver.resolve("#myMethod(java.lang.String, boolean, T, int[])");

        assertSame(localFunc_4, actual, "Failed to find method");
    }

    /**
     * Unit test {@link Resolver#resolve(String)}
     */
    @Test
    public void resolve_local_method_unknown() {
        Element actual = resolver.resolve("#unknown()");

        assertNull(actual, "Found unexpected method");
    }

    /**
     * Unit test {@link Resolver#resolve(String)} with a fully qualified function name
     */
    @Test
    public void resolve_qualified_method() {
        Element actual = resolver.resolve("com.github.tymefly.eel.Eel#evaluate()");

        assertSame(eelEvaluate, actual, "Found unexpected method");
    }

    /**
     * Unit test {@link Resolver#resolve(String)} by looking up {@link String#length()}
     */
    @Test
    public void resolve_java_method() {
        Element actual = resolver.resolve("String#length()");

        assertSame(stringLength, actual, "Found unexpected method");
    }

    /**
     * Unit test {@link Resolver#resolve(String)} in the current package
     */
    @Test
    public void resolve_packaged_method() {
        Element actual = resolver.resolve("Package#otherMethod()");

        assertSame(packageMethod, actual, "Found unexpected method");
    }

    /**
     * Unit test {@link Resolver#resolve(String)}
     */
    @Test
    public void resolve_local_NestedClass_field() {
        Element actual = resolver.resolve("Nested#NESTED_FIELD");

        assertSame(nestedField, actual, "Found unexpected method");
    }

    /**
     * Unit test {@link Resolver#resolve(String)}
     */
    @Test
    public void resolve_local_InnerClass_field() {
        Element actual = resolver.resolve("Inner#INNER_FIELD");

        assertSame(innerField, actual, "Found unexpected method");
    }

    /**
     * Unit test {@link Resolver#resolve(String)}
     */
    @Test
    public void resolve_class() {
        Element actual = resolver.resolve("com.github.tymefly.eel.test.testClass");

        assertSame(testClass, actual, "Found unexpected class");
    }

    /**
     * Unit test {@link Resolver#resolve(String)}
     */
    @Test
    public void resolve_unknown() {
        Element actual = resolver.resolve("x.y.z");

        assertNull(actual, "Found unexpected element");
    }
}