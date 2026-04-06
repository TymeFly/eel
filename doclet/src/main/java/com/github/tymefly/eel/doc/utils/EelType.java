package com.github.tymefly.eel.doc.utils;

import javax.annotation.Nonnull;

/**
 * Enumeration of all possible types that can be passed to an EEL functions.
 * This includes the four basic types and the "{@literal value}" for Thunking
 */
public enum EelType {
    TEXT("Text"),
    NUMBER("Number"),
    LOGIC("Logic"),
    DATE("Date"),
    VALUE("Value");

    private final String name;

    EelType(@Nonnull String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }
}
