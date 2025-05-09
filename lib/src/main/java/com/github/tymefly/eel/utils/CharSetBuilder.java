package com.github.tymefly.eel.utils;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.annotation.Nonnull;

import com.github.tymefly.eel.validate.Preconditions;

/**
 * Helper class that builds sets of characters.
 */
public class CharSetBuilder {
    private final Set<Character> backing;

    /** Constructor */
    public CharSetBuilder() {
        backing = new HashSet<>();
    }

    /**
     * Constructor. This is the equivalent of {@code new CharSetBuilder().withAll(content);}
     * @param content    initial characters in the set
     */
    public CharSetBuilder(@Nonnull Set<Character> content) {
        backing = new HashSet<>(content);
    }


    /**
     * Adds a single character to the set defined by this builder
     * @param entry     a character that will be in the set generated by this builder.
     * @return          a fluent interface
     */
    public CharSetBuilder with(char entry) {
        backing.add(entry);

        return this;
    }


    /**
     * Adds all the characters in the specified range to the set defined by this builder
     * @param from      the first character in the range that will be in the set generated by this builder.
     * @param to        the last character in the range that will be in the set generated by this builder.
     * @return          a fluent interface
     * @throws IllegalArgumentException if the value of {@code from} is greater than the value of {@code to}
     */
    @Nonnull
    public CharSetBuilder range(char from, char to) throws IllegalArgumentException {
        Preconditions.checkArgument((from <= to), "Invalid character range (%d -> %d)", (int) from, (int) to);

        while (from <= to) {
            backing.add(from++);
        }

        return this;
    }


    /**
     * Adds all the characters in {@code subset} to the set defined by this builder
     * @param subset    a collection of characters that will be in the set generated by this builder.
     * @return          a fluent interface
     */
    @Nonnull
    public CharSetBuilder withAll(@Nonnull Set<Character> subset) {
        backing.addAll(subset);

        return this;
    }


    /**
     * Builder method that returns a modifiable set of characters as defined by this builder
     * @return a modifiable set of characters as defined by this builder
     */
    @Nonnull
    Set<Character> mutable() {
        return new HashSet<>(backing);
    }

    /**
     * Builder method that returns an unmodifiable set of characters as defined by this builder
     * @return an unmodifiable set of characters as defined by this builder
     */
    @Nonnull
    public Set<Character> immutable() {
        return Collections.unmodifiableSet(mutable());
    }

    /**
     * Builder method that returns a String containing all the characters defined by this builder.
     * The order of the characters is undefined
     * @return a string containing all the characters all the characters defined by this builder.
     */
    @Nonnull
    public String asString() {
        StringBuilder result = new StringBuilder(backing.size());

        backing.forEach(result::append);

        return result.toString();
    }
}
