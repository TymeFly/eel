package com.github.tymefly.eel;

import javax.annotation.Nonnull;

/**
 * Types handled by the Expression parser
 */
public enum Type {
    TEXT("Text"),
    NUMBER("Number"),
    LOGIC("Logic"),
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
