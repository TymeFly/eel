package com.github.tymefly.eel.doc.source;

import javax.annotation.Nonnull;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;

import com.github.tymefly.eel.doc.utils.EelType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
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
        assertEquals(EelType.LOGIC, TranslateType.toEel(mockPrimitive(TypeKind.BOOLEAN)), "boolean");
        assertEquals(EelType.TEXT, TranslateType.toEel(mockPrimitive(TypeKind.CHAR)), "char");
        assertEquals(EelType.NUMBER, TranslateType.toEel(mockPrimitive(TypeKind.BYTE)), "byte");
        assertEquals(EelType.NUMBER, TranslateType.toEel(mockPrimitive(TypeKind.SHORT)), "short");
        assertEquals(EelType.NUMBER, TranslateType.toEel(mockPrimitive(TypeKind.INT)), "int");
        assertEquals(EelType.NUMBER, TranslateType.toEel(mockPrimitive(TypeKind.LONG)), "long");
        assertEquals(EelType.NUMBER, TranslateType.toEel(mockPrimitive(TypeKind.FLOAT)), "float");
        assertEquals(EelType.NUMBER, TranslateType.toEel(mockPrimitive(TypeKind.DOUBLE)), "double");
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
        assertEquals(EelType.LOGIC, TranslateType.toEel(mockDeclared("java.lang.Boolean")), "Boolean");
        assertEquals(EelType.TEXT, TranslateType.toEel(mockDeclared("java.lang.Character")), "Char");
        assertEquals(EelType.NUMBER, TranslateType.toEel(mockDeclared("java.lang.Byte")), "Byte");
        assertEquals(EelType.NUMBER, TranslateType.toEel(mockDeclared("java.lang.Short")), "Short");
        assertEquals(EelType.NUMBER, TranslateType.toEel(mockDeclared("java.lang.Integer")), "Int");
        assertEquals(EelType.NUMBER, TranslateType.toEel(mockDeclared("java.lang.Long")), "Long");
        assertEquals(EelType.NUMBER, TranslateType.toEel(mockDeclared("java.lang.Float")), "Float");
        assertEquals(EelType.NUMBER, TranslateType.toEel(mockDeclared("java.lang.Double")), "Double");

        assertEquals(EelType.NUMBER, TranslateType.toEel(mockDeclared("java.math.BigInteger")), "BigInteger");
        assertEquals(EelType.NUMBER, TranslateType.toEel(mockDeclared("java.math.BigDecimal")), "BigDecimal");
        assertEquals(EelType.TEXT, TranslateType.toEel(mockDeclared("java.lang.String")), "String");
        assertEquals(EelType.TEXT, TranslateType.toEel(mockDeclared("java.io.File")), "File");
        assertEquals(EelType.DATE, TranslateType.toEel(mockDeclared("java.time.ZonedDateTime")), "ZonedDateTime");
        assertEquals(EelType.VALUE, TranslateType.toEel(mockDeclared("com.github.tymefly.eel.Value")), "Value");

        assertNull(TranslateType.toEel(mockDeclared("x.y.Z")), "Unknown");
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
        assertEquals(EelType.LOGIC, TranslateType.toEel(mockArray("boolean")), "boolean");
        assertEquals(EelType.TEXT, TranslateType.toEel(mockArray("char")), "char");
        assertEquals(EelType.NUMBER, TranslateType.toEel(mockArray("byte")), "byte");
        assertEquals(EelType.NUMBER, TranslateType.toEel(mockArray("short")), "short");
        assertEquals(EelType.NUMBER, TranslateType.toEel(mockArray("int")), "int");
        assertEquals(EelType.NUMBER, TranslateType.toEel(mockArray("long")), "long");
        assertEquals(EelType.NUMBER, TranslateType.toEel(mockArray("float")), "float");
        assertEquals(EelType.NUMBER, TranslateType.toEel(mockArray("double")), "double");

        assertEquals(EelType.LOGIC, TranslateType.toEel(mockArray("java.lang.Boolean")), "Boolean");
        assertEquals(EelType.TEXT, TranslateType.toEel(mockArray("java.lang.Character")), "Char");
        assertEquals(EelType.NUMBER, TranslateType.toEel(mockArray("java.lang.Byte")), "Byte");
        assertEquals(EelType.NUMBER, TranslateType.toEel(mockArray("java.lang.Short")), "Short");
        assertEquals(EelType.NUMBER, TranslateType.toEel(mockArray("java.lang.Integer")), "Int");
        assertEquals(EelType.NUMBER, TranslateType.toEel(mockArray("java.lang.Long")), "Long");
        assertEquals(EelType.NUMBER, TranslateType.toEel(mockArray("java.lang.Float")), "Float");
        assertEquals(EelType.NUMBER, TranslateType.toEel(mockArray("java.lang.Double")), "Double");

        assertEquals(EelType.NUMBER, TranslateType.toEel(mockArray("java.math.BigInteger")), "BigInteger");
        assertEquals(EelType.NUMBER, TranslateType.toEel(mockArray("java.math.BigDecimal")), "BigDecimal");
        assertEquals(EelType.TEXT, TranslateType.toEel(mockArray("java.lang.String")), "String");
        assertEquals(EelType.TEXT, TranslateType.toEel(mockArray("java.io.File")), "File");
        assertEquals(EelType.DATE, TranslateType.toEel(mockArray("java.time.ZonedDateTime")), "ZonedDateTime");
        assertEquals(EelType.VALUE, TranslateType.toEel(mockArray("com.github.tymefly.eel.Value")), "Value");

        assertNull(TranslateType.toEel(mockArray("x.y.Z")), "Unknown");
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

        assertNull(TranslateType.toEel(mock), "Unknown");
    }
}