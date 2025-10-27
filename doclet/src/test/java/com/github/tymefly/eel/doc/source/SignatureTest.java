package com.github.tymefly.eel.doc.source;

import java.util.List;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.ModuleElement;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;

import org.junit.Assert;
import org.junit.Test;

import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Unit test for {@link Signature}
 */
public class SignatureTest {

    /**
     * Unit test {@link Signature#of(Element)}
     */
    @Test
    public void test_of_package() {
        PackageElement source = mock(PackageElement.class, RETURNS_DEEP_STUBS);

        when(source.getQualifiedName().toString())
            .thenReturn("com.github.tymefly.eel");

        Assert.assertEquals("Unexpected signature", "com.github.tymefly.eel", Signature.of(source));
    }

    /**
     * Unit test {@link Signature#of(Element)}
     */
    @Test
    public void test_of_type() {
        TypeElement source = mock(TypeElement.class, RETURNS_DEEP_STUBS);

        when(source.getQualifiedName().toString())
            .thenReturn("com.github.tymefly.eel.MyClass");

        Assert.assertEquals("Unexpected signature", "com.github.tymefly.eel.MyClass", Signature.of(source));
    }

    /**
     * Unit test {@link Signature#of(Element)}
     */
    @Test
    public void test_of_method() {
        ExecutableElement source = mock(ExecutableElement.class, RETURNS_DEEP_STUBS);
        TypeElement type = mock(TypeElement.class, RETURNS_DEEP_STUBS);
        VariableElement arg1 = mock(VariableElement.class, RETURNS_DEEP_STUBS);
        VariableElement arg2 = mock(VariableElement.class, RETURNS_DEEP_STUBS);

        when(source.getEnclosingElement())
            .thenReturn(type);
        when(type.toString())
            .thenReturn("com.github.tymefly.eel.MyClass");
        when(source.getSimpleName().toString())
            .thenReturn("myMethod");
        when(source.getParameters())
            .thenAnswer(i -> List.of(arg1, arg2));
        when(arg1.asType().toString())
            .thenReturn("java.lang.String");
        when(arg2.asType().toString())
            .thenReturn("int");

        Assert.assertEquals("Unexpected signature",
            "com.github.tymefly.eel.MyClass.myMethod(java.lang.String, int)",
            Signature.of(source));
    }

    /**
     * Unit test {@link Signature#of(Element)}
     */
    @Test
    public void test_of_field() {
        Element source = mock(VariableElement.class, RETURNS_DEEP_STUBS);

        when(source.getKind().isField())
            .thenReturn(true);
        when(source.getKind())
            .thenReturn(ElementKind.FIELD);
        when(source.getEnclosingElement().toString())
            .thenReturn("com.github.tymefly.eel.MyClass");
        when(source.getSimpleName().toString())
            .thenReturn("myField");

        Assert.assertEquals("Unexpected signature", "com.github.tymefly.eel.MyClass.myField", Signature.of(source));
    }

    /**
     * Unit test {@link Signature#of(Element)}
     */
    @Test
    public void test_of_enum() {
        Element source = mock(VariableElement.class, RETURNS_DEEP_STUBS);

        when(source.getKind().isField())
            .thenReturn(true);
        when(source.getKind())
            .thenReturn(ElementKind.ENUM_CONSTANT);
        when(source.getEnclosingElement().toString())
            .thenReturn("com.github.tymefly.eel.MyClass");
        when(source.getSimpleName().toString())
            .thenReturn("MY_ENUM");

        Assert.assertEquals("Unexpected signature", "com.github.tymefly.eel.MyClass.MY_ENUM", Signature.of(source));
    }

    /**
     * Unit test {@link Signature#of(Element)}
     */
    @Test
    public void test_of_unexpected() {
        Element source = mock(ModuleElement.class, RETURNS_DEEP_STUBS);

        when(source.toString())
            .thenReturn("myModule");

        Assert.assertEquals("Unexpected signature", "myModule", Signature.of(source));
    }
}