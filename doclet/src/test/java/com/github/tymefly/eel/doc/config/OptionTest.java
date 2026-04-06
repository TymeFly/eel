package com.github.tymefly.eel.doc.config;

import java.util.List;
import java.util.Optional;

import jdk.javadoc.doclet.Doclet;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit test for {@link Option}
 */
class OptionTest {
    private final Option flag = Option.builder("-flag")
        .withDescription("my Flag")
        .asExtended()
        .build();
    private final Option option = Option.builder("-option")
        .withDescription("an option")
        .asExtended()
        .withParameter("myParameter")
        .build();
    private final Option defaulted = Option.builder("-defaulted")
        .withDescription("defaulted option")
        .withParameter("anotherParameter")
        .withDefault("myDefault")
        .build();
    private final Option expression = Option.builder("-e" ,"-expression")
        .withDescription("eel expression")
        .withParameter("myExpression")
        .asExpression()
        .build();


    /**
     * Unit test {@link Option#getArgumentCount()}
     */
    @Test
    void test_getArgumentCount() {
        assertEquals(0, flag.getArgumentCount(), "flag");
        assertEquals(1, option.getArgumentCount(), "option");
        assertEquals(1, defaulted.getArgumentCount(), "defaulted");
        assertEquals(1, expression.getArgumentCount(), "expression");
    }

    /**
     * Unit test {@link Option#getDescription()}
     */
    @Test
    void test_getDescription() {
        assertEquals("my Flag", flag.getDescription(), "flag");
        assertEquals("an option", option.getDescription(), "option");
        assertEquals("defaulted option", defaulted.getDescription(), "defaulted");
        assertEquals("eel expression", expression.getDescription(), "expression");
    }

    /**
     * Unit test {@link Option#getKind()}
     */
    @Test
    void test_getKind() {
        assertEquals(Doclet.Option.Kind.EXTENDED, flag.getKind(), "flag");
        assertEquals(Doclet.Option.Kind.EXTENDED, option.getKind(), "option");
        assertEquals(Doclet.Option.Kind.STANDARD, defaulted.getKind(), "defaulted");
        assertEquals(Doclet.Option.Kind.STANDARD, expression.getKind(), "expression");
    }

    /**
     * Unit test {@link Option#getNames()}
     */
    @Test
    void test_getNames() {
        assertEquals(List.of("-flag"), flag.getNames(), "flag");
        assertEquals(List.of("-option"), option.getNames(), "option");
        assertEquals(List.of("-defaulted"), defaulted.getNames(), "defaulted");
        assertEquals(List.of("-e" ,"-expression"), expression.getNames(), "expression");
    }

    /**
     * Unit test {@link Option#getParameters()}
     */
    @Test
    void test_getParameters() {
        assertEquals("", flag.getParameters(), "flag");
        assertEquals("myParameter", option.getParameters(), "option");
        assertEquals("anotherParameter", defaulted.getParameters(), "defaulted");
        assertEquals("myExpression", expression.getParameters(), "expression");
    }


    /**
     * Unit test {@link Option} for a flag
     */
    @Test
    void test_flag() {
        assertFalse(flag.isSet(), "Before: Flag");
        assertThrows(IllegalStateException.class, flag::value, "Before: Value");
        assertThrows(IllegalStateException.class, flag::optionalValue, "Before: Optional");

        flag.process("", List.of());

        assertTrue(flag.isSet(), "After: Flag");
        assertThrows(IllegalStateException.class, flag::value, "After: Value");
        assertThrows(IllegalStateException.class, flag::optionalValue, "After: Optional");
    }

    /**
     * Unit test {@link Option} for an option without a default
     */
    @Test
    void test_option() {
        assertFalse(option.isSet(), "Before: Flag");
        assertThrows(IllegalStateException.class, option::value, "Before: Value");
        assertEquals(Optional.empty(), option.optionalValue(), "Before: Optional");

        option.process("myParameter", List.of("setting"));

        assertTrue(option.isSet(), "After: Flag");
        assertEquals("setting", option.value(), "After: Value");
        assertEquals(Optional.of("setting"), option.optionalValue(), "After: Optional");
    }

    /**
     * Unit test {@link Option} for an option with a default
     */
    @Test
    void test_defaulted() {
        assertFalse(defaulted.isSet(), "Before: Flag");
        assertEquals("myDefault", defaulted.value(), "Before: Value");
        assertEquals(Optional.empty(), defaulted.optionalValue(), "Before: Optional");

        defaulted.process("anotherParameter", List.of("setting"));

        assertTrue(defaulted.isSet(), "After: Flag");
        assertEquals("setting", defaulted.value(), "After: Value");
        assertEquals(Optional.of("setting"), defaulted.optionalValue(), "After: Optional");
    }

    /**
     * Unit test {@link Option} for an option that can take an expression
     */
    @Test
    void test_expression() {
        assertFalse(expression.isSet(), "Before: Flag");
        assertThrows(IllegalStateException.class, expression::value, "Before: Value");
        assertEquals(Optional.empty(), expression.optionalValue(), "Before: Optional");

        expression.process("anotherParameter", List.of("add: $( 1 + 2 )"));

        assertTrue(expression.isSet(), "After: Flag");
        assertEquals("add: 3", expression.value(), "After: Value");
        assertEquals(Optional.of("add: 3"), expression.optionalValue(), "After: Optional");
    }
}
