package com.github.tymefly.eel.doc.config;

import java.util.List;
import java.util.Optional;

import jdk.javadoc.doclet.Doclet;
import org.junit.Assert;
import org.junit.Test;

/**
 * Unit test for {@link Option}
 */
public class OptionTest {
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
    public void test_getArgumentCount() {
        Assert.assertEquals("flag", 0, flag.getArgumentCount());
        Assert.assertEquals("option", 1, option.getArgumentCount());
        Assert.assertEquals("defaulted", 1, defaulted.getArgumentCount());
        Assert.assertEquals("expression", 1, expression.getArgumentCount());
    }

    /**
     * Unit test {@link Option#getDescription()}
     */
    @Test
    public void test_getDescription() {
        Assert.assertEquals("flag", "my Flag", flag.getDescription());
        Assert.assertEquals("option", "an option", option.getDescription());
        Assert.assertEquals("defaulted", "defaulted option", defaulted.getDescription());
        Assert.assertEquals("expression", "eel expression", expression.getDescription());
    }

    /**
     * Unit test {@link Option#getKind()}
     */
    @Test
    public void test_getKind() {
        Assert.assertEquals("flag", Doclet.Option.Kind.EXTENDED, flag.getKind());
        Assert.assertEquals("option", Doclet.Option.Kind.EXTENDED, option.getKind());
        Assert.assertEquals("defaulted", Doclet.Option.Kind.STANDARD, defaulted.getKind());
        Assert.assertEquals("expression", Doclet.Option.Kind.STANDARD, expression.getKind());
    }

    /**
     * Unit test {@link Option#getNames()}
     */
    @Test
    public void test_getNames() {
        Assert.assertEquals("flag", List.of("-flag"), flag.getNames());
        Assert.assertEquals("option", List.of("-option"), option.getNames());
        Assert.assertEquals("defaulted", List.of("-defaulted"), defaulted.getNames());
        Assert.assertEquals("expression", List.of("-e" ,"-expression"), expression.getNames());
    }

    /**
     * Unit test {@link Option#getParameters()}
     */
    @Test
    public void test_getParameters() {
        Assert.assertEquals("flag", "", flag.getParameters());
        Assert.assertEquals("option", "myParameter", option.getParameters());
        Assert.assertEquals("defaulted", "anotherParameter", defaulted.getParameters());
        Assert.assertEquals("expression", "myExpression", expression.getParameters());
    }


    /**
     * Unit test {@link Option} for a flag
     */
    @Test
    public void test_flag() {
        Assert.assertFalse("Before: Flag", flag.isSet());
        Assert.assertThrows("Before: Value", IllegalStateException.class, flag::value);
        Assert.assertThrows("Before: Optional", IllegalStateException.class, flag::optionalValue);

        flag.process("", List.of());

        Assert.assertTrue("After: Flag", flag.isSet());
        Assert.assertThrows("After: Value", IllegalStateException.class, flag::value);
        Assert.assertThrows("After: Optional", IllegalStateException.class, flag::optionalValue);
    }

    /**
     * Unit test {@link Option} for an option without a default
     */
    @Test
    public void test_option() {
        Assert.assertFalse("Before: Flag", option.isSet());
        Assert.assertThrows("Before: Value", IllegalStateException.class, option::value);
        Assert.assertEquals("Before: Optional", Optional.empty(), option.optionalValue());

        option.process("myParameter", List.of("setting"));

        Assert.assertTrue("After: Flag", option.isSet());
        Assert.assertEquals("After: Value", "setting", option.value());
        Assert.assertEquals("After: Optional", Optional.of("setting"), option.optionalValue());
    }

    /**
     * Unit test {@link Option} for an option with a default
     */
    @Test
    public void test_defaulted() {
        Assert.assertFalse("Before: Flag", defaulted.isSet());
        Assert.assertEquals("Before: Value", "myDefault", defaulted.value());
        Assert.assertEquals("Before: Optional", Optional.empty(), defaulted.optionalValue());

        defaulted.process("anotherParameter", List.of("setting"));

        Assert.assertTrue("After: Flag", defaulted.isSet());
        Assert.assertEquals("After: Value", "setting", defaulted.value());
        Assert.assertEquals("After: Optional", Optional.of("setting"), defaulted.optionalValue());
    }

    /**
     * Unit test {@link Option} for an option that can take an expression
     */
    @Test
    public void test_expression() {
        Assert.assertFalse("Before: Flag", expression.isSet());
        Assert.assertThrows("Before: Value", IllegalStateException.class, expression::value);
        Assert.assertEquals("Before: Optional", Optional.empty(), expression.optionalValue());

        expression.process("anotherParameter", List.of("add: $( 1 + 2 )"));

        Assert.assertTrue("After: Flag", expression.isSet());
        Assert.assertEquals("After: Value", "add: 3", expression.value());
        Assert.assertEquals("After: Optional", Optional.of("add: 3"), expression.optionalValue());
    }
}