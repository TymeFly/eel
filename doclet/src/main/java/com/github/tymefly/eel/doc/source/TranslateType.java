package com.github.tymefly.eel.doc.source;

import java.io.File;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.ZonedDateTime;
import java.util.Map;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;

import com.github.tymefly.eel.Value;
import com.github.tymefly.eel.doc.utils.EelType;

import static java.util.Map.entry;

/**
 * Type Utility functions.
 */
class TranslateType {
    private static final Map<String, EelType> TYPE_MAPPER = Map.ofEntries(
        entry("char", EelType.TEXT),                                    // Primitives
        entry("boolean", EelType.LOGIC),
        entry("byte", EelType.NUMBER),
        entry("short", EelType.NUMBER),
        entry("int", EelType.NUMBER),
        entry("long", EelType.NUMBER),
        entry("float", EelType.NUMBER),
        entry("double", EelType.NUMBER),

        entry(Character.class.getName(), EelType.TEXT),                 // Wrapped primitives
        entry(Boolean.class.getName(), EelType.LOGIC),
        entry(Byte.class.getName(), EelType.NUMBER),
        entry(Short.class.getName(), EelType.NUMBER),
        entry(Integer.class.getName(), EelType.NUMBER),
        entry(Long.class.getName(), EelType.NUMBER),
        entry(Float.class.getName(), EelType.NUMBER),
        entry(Double.class.getName(), EelType.NUMBER),

        entry(BigInteger.class.getName(), EelType.NUMBER),              // Other Numbers
        entry(BigDecimal.class.getName(), EelType.NUMBER),

        entry(String.class.getName(), EelType.TEXT),                    // Other Text
        entry(File.class.getName(), EelType.TEXT),

        entry(ZonedDateTime.class.getName(), EelType.DATE),             // Dates

        entry(Value.class.getName(), EelType.VALUE));                   // Misc


    private TranslateType() {
    }


    /**
     * Returns the EelType associated with the {@code typeMirror}, or {@literal null} if there is
     * no associated EEL type
     * @param typeMirror        Javadoc mirror type
     * @return the EelType associated with the {@code typeMirror}, or {@literal null} if there is
     * no associated EEL type
     */
    @Nullable
    static EelType toEel(@Nonnull TypeMirror typeMirror) {
        EelType type;

        switch (typeMirror.getKind()) {
            case BOOLEAN -> type = EelType.LOGIC;
            case CHAR -> type = EelType.TEXT;
            case INT, BYTE, SHORT, LONG, FLOAT, DOUBLE -> type = EelType.NUMBER;
            case DECLARED -> {
                DeclaredType declaredType = (DeclaredType) typeMirror;
                TypeElement typeElement = (TypeElement) declaredType.asElement();
                type = TYPE_MAPPER.get(typeElement.getQualifiedName().toString());
            }
            case ARRAY -> {
                ArrayType arrayType = (ArrayType) typeMirror;
                TypeMirror componentType = arrayType.getComponentType();
                type = TYPE_MAPPER.get(componentType.toString());
            }
            default -> type = null;
        }

        return type;
    }
}
