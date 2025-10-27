package com.github.tymefly.eel.doc.source;

import javax.annotation.Nonnull;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;

import com.github.tymefly.eel.doc.utils.EelType;
import org.junit.Assert;
import org.junit.Test;

import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Unit test for {@link TranslateType}
 */
public class TranslateTypeTest {
    /**
     * Unit test {@link TranslateType#toEel(TypeMirror)}
     */
    @Test
    public void test_toEel_primitive() {
        Assert.assertEquals("boolean", EelType.LOGIC, TranslateType.toEel(mockPrimitive(TypeKind.BOOLEAN)));
        Assert.assertEquals("char", EelType.TEXT, TranslateType.toEel(mockPrimitive(TypeKind.CHAR)));
        Assert.assertEquals("byte", EelType.NUMBER, TranslateType.toEel(mockPrimitive(TypeKind.BYTE)));
        Assert.assertEquals("short", EelType.NUMBER, TranslateType.toEel(mockPrimitive(TypeKind.SHORT)));
        Assert.assertEquals("int", EelType.NUMBER, TranslateType.toEel(mockPrimitive(TypeKind.INT)));
        Assert.assertEquals("long", EelType.NUMBER, TranslateType.toEel(mockPrimitive(TypeKind.LONG)));
        Assert.assertEquals("float", EelType.NUMBER, TranslateType.toEel(mockPrimitive(TypeKind.FLOAT)));
        Assert.assertEquals("double", EelType.NUMBER, TranslateType.toEel(mockPrimitive(TypeKind.DOUBLE)));
    }

    @Nonnull
    private TypeMirror mockPrimitive(@Nonnull TypeKind type) {
        TypeMirror mock = mock();

        when(mock.getKind())
            .thenReturn(type);

        return mock;
    }


    /**
     * Unit test {@link TranslateType#toEel(TypeMirror)}
     */
    @Test
    public void test_toEel_declared() {
        Assert.assertEquals("Boolean", EelType.LOGIC, TranslateType.toEel(mockDeclared("java.lang.Boolean")));
        Assert.assertEquals("Char", EelType.TEXT, TranslateType.toEel(mockDeclared("java.lang.Character")));
        Assert.assertEquals("Byte", EelType.NUMBER, TranslateType.toEel(mockDeclared("java.lang.Byte")));
        Assert.assertEquals("Short", EelType.NUMBER, TranslateType.toEel(mockDeclared("java.lang.Short")));
        Assert.assertEquals("Int", EelType.NUMBER, TranslateType.toEel(mockDeclared("java.lang.Integer")));
        Assert.assertEquals("Long", EelType.NUMBER, TranslateType.toEel(mockDeclared("java.lang.Long")));
        Assert.assertEquals("Float", EelType.NUMBER, TranslateType.toEel(mockDeclared("java.lang.Float")));
        Assert.assertEquals("Double", EelType.NUMBER, TranslateType.toEel(mockDeclared("java.lang.Double")));

        Assert.assertEquals("BigInteger", EelType.NUMBER, TranslateType.toEel(mockDeclared("java.math.BigInteger")));
        Assert.assertEquals("BigDecimal", EelType.NUMBER, TranslateType.toEel(mockDeclared("java.math.BigDecimal")));
        Assert.assertEquals("String", EelType.TEXT, TranslateType.toEel(mockDeclared("java.lang.String")));
        Assert.assertEquals("File", EelType.TEXT, TranslateType.toEel(mockDeclared("java.io.File")));
        Assert.assertEquals("ZonedDateTime", EelType.DATE, TranslateType.toEel(mockDeclared("java.time.ZonedDateTime")));
        Assert.assertEquals("Value", EelType.VALUE, TranslateType.toEel(mockDeclared("com.github.tymefly.eel.Value")));

        Assert.assertNull("Unknown", TranslateType.toEel(mockDeclared("x.y.Z")));
    }

    @Nonnull
    private TypeMirror mockDeclared(@Nonnull String name) {
        DeclaredType mock = mock(DeclaredType.class, RETURNS_DEEP_STUBS);
        TypeElement typeElement = mock(TypeElement.class, RETURNS_DEEP_STUBS);

        when(mock.getKind())
            .thenReturn(TypeKind.DECLARED);
        when(mock.asElement())
            .thenReturn(typeElement);
        when(typeElement.getQualifiedName().toString())
            .thenReturn(name);

        return mock;
    }



    /**
     * Unit test {@link TranslateType#toEel(TypeMirror)}
     */
    @Test
    public void test_toEel_array() {
        Assert.assertEquals("boolean", EelType.LOGIC, TranslateType.toEel(mockArray("boolean")));
        Assert.assertEquals("char", EelType.TEXT, TranslateType.toEel(mockArray("char")));
        Assert.assertEquals("byte", EelType.NUMBER, TranslateType.toEel(mockArray("byte")));
        Assert.assertEquals("short", EelType.NUMBER, TranslateType.toEel(mockArray("short")));
        Assert.assertEquals("int", EelType.NUMBER, TranslateType.toEel(mockArray("int")));
        Assert.assertEquals("long", EelType.NUMBER, TranslateType.toEel(mockArray("long")));
        Assert.assertEquals("float", EelType.NUMBER, TranslateType.toEel(mockArray("float")));
        Assert.assertEquals("double", EelType.NUMBER, TranslateType.toEel(mockArray("double")));

        Assert.assertEquals("Boolean", EelType.LOGIC, TranslateType.toEel(mockArray("java.lang.Boolean")));
        Assert.assertEquals("Char", EelType.TEXT, TranslateType.toEel(mockArray("java.lang.Character")));
        Assert.assertEquals("Byte", EelType.NUMBER, TranslateType.toEel(mockArray("java.lang.Byte")));
        Assert.assertEquals("Short", EelType.NUMBER, TranslateType.toEel(mockArray("java.lang.Short")));
        Assert.assertEquals("Int", EelType.NUMBER, TranslateType.toEel(mockArray("java.lang.Integer")));
        Assert.assertEquals("Long", EelType.NUMBER, TranslateType.toEel(mockArray("java.lang.Long")));
        Assert.assertEquals("Float", EelType.NUMBER, TranslateType.toEel(mockArray("java.lang.Float")));
        Assert.assertEquals("Double", EelType.NUMBER, TranslateType.toEel(mockArray("java.lang.Double")));

        Assert.assertEquals("BigInteger", EelType.NUMBER, TranslateType.toEel(mockArray("java.math.BigInteger")));
        Assert.assertEquals("BigDecimal", EelType.NUMBER, TranslateType.toEel(mockArray("java.math.BigDecimal")));
        Assert.assertEquals("String", EelType.TEXT, TranslateType.toEel(mockArray("java.lang.String")));
        Assert.assertEquals("File", EelType.TEXT, TranslateType.toEel(mockArray("java.io.File")));
        Assert.assertEquals("ZonedDateTime", EelType.DATE, TranslateType.toEel(mockArray("java.time.ZonedDateTime")));
        Assert.assertEquals("Value", EelType.VALUE, TranslateType.toEel(mockArray("com.github.tymefly.eel.Value")));

        Assert.assertNull("Unknown", TranslateType.toEel(mockArray("x.y.Z")));
    }

    @Nonnull
    private TypeMirror mockArray(@Nonnull String name) {
        ArrayType mock = mock(ArrayType.class, RETURNS_DEEP_STUBS);

        when(mock.getKind())
            .thenReturn(TypeKind.ARRAY);
        when(mock.getComponentType().toString())
            .thenReturn(name);

        return mock;
    }



    /**
     * Unit test {@link TranslateType#toEel(TypeMirror)}
     */
    @Test
    public void test_toEel_unknown() {
        TypeMirror mock = mock(TypeMirror.class, RETURNS_DEEP_STUBS);

        when(mock.getKind())
            .thenReturn(TypeKind.PACKAGE);

        Assert.assertNull("Unknown", TranslateType.toEel(mock));
    }
}