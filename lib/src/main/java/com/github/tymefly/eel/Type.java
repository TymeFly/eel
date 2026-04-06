package com.github.tymefly.eel;

import javax.annotation.Nonnull;
/**
 * Supported EEL data types.
 * Each type represents a possible result of an evaluated EEL expression.
 */
public enum Type {
    /** Textual values. */
    TEXT("Text"),

    /** Numeric values. */
    NUMBER("Number"),

    /** Logical ({@code boolean}) values. */
    LOGIC("Logic"),

    /** Date and time values. */
    DATE("Date");

    private final String name;

    Type(@Nonnull String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }
}