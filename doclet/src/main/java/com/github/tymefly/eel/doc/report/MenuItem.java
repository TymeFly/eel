package com.github.tymefly.eel.doc.report;

import javax.annotation.Nonnull;

/**
 * Enumeration of links that appear in the menu at the top of each page.
 * The order the constants are declared in this class matches the order they links are displayed
 */
enum MenuItem {
    /** Menu item for a link to the root of this site */
    OVERVIEW("Overview", "index.html"),

    /** Index of all functions by group */
    INDEX("Index", "_index.html"),

    /** Menu item for a link to the EEL website */
    EEL("EEL", "https://github.com/TymeFly/eel?tab=readme-ov-file#eel");


    private final String name;
    private final String href;


    MenuItem(@Nonnull String name, @Nonnull String href) {
        this.name = name;
        this.href = href;
    }


    /**
     * Returns the destination link
     * @return the destination link
     */
    @Nonnull
    public String getHref() {
        return href;
    }


    @Override
    public String toString() {
        return name;
    }
}
