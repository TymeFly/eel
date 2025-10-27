package com.github.tymefly.eel.doc.model;

/**
 * Supported text styles.
 */
public enum TextStyle {
    NONE(true, false),
    RAW(true, false),
    LITERAL(false, false),
    CODE(false, false),
    LINK(false, true),
    PLAIN_LINK(false, true),
    ERROR(false, false);

    private final boolean clean;
    private final boolean escape;

    TextStyle(boolean clean, boolean escape) {
        this.clean = clean;
        this.escape = escape;
    }


    boolean clean() {
        return clean;
    }

    boolean escape() {
        return escape;
    }
}
