package com.github.tymefly.eel;

import java.time.Duration;
import java.util.Map;

import javax.annotation.Nonnull;

import com.github.tymefly.eel.exception.EelUnknownSymbolException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import uk.org.webcompere.systemstubs.jupiter.SystemStub;
import uk.org.webcompere.systemstubs.jupiter.SystemStubsExtension;
import uk.org.webcompere.systemstubs.stream.SystemErr;
import uk.org.webcompere.systemstubs.stream.SystemOut;

import static java.util.Map.entry;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;


/**
 * Integration Test for variable expansions
 */
@ExtendWith(SystemStubsExtension.class)
public class VariableExpansionIntegrationTest {
    @SystemStub
    private SystemOut stdOut;

    @SystemStub
    private SystemErr stdErr;

    private EelContext context;


    @BeforeEach
    public void setUp() {
        context = EelContext.factory()
            .withTimeout(Duration.ofSeconds(0))
            .build();
    }
    
    /**
     * Integration test {@link Eel}
     */
    @Test
    public void test_withDefault() {
        SymbolsTable symbols = SymbolsTable.from(Map.ofEntries(
            Map.entry("defined", "value"),
            Map.entry("blank", "")));

        assertEquals("???",
            Eel.compile(context, "${undefined-???}").evaluate(symbols).asText(),
            "undefined");
        assertEquals("",
            Eel.compile(context, "${blank-???}").evaluate(symbols).asText(),
            "blank");
        assertEquals("value",
            Eel.compile(context, "${defined-???}").evaluate(symbols).asText(),
            "defined");
    }
    /**
     * Integration test {@link Eel}
     */
    @Test
    public void test_withBlankDefault() {
        SymbolsTable symbols = SymbolsTable.from(Map.ofEntries(
            Map.entry("defined", "value"),
            Map.entry("blank", "")));

        assertEquals("???",
            Eel.compile(context, "${undefined:-???}").evaluate(symbols).asText(),
            "undefined");
        assertEquals("???",
            Eel.compile(context, "${blank:-???}").evaluate(symbols).asText(),
            "blank");
        assertEquals("value",
            Eel.compile(context, "${defined:-???}").evaluate(symbols).asText(),
            "defined");
    }

    /**
     * Integration test {@link Eel}
     */
    @Test
    public void test_notFound() {
        Eel expression = Eel.compile(context, "${fred}");

        EelUnknownSymbolException actual = assertThrows(EelUnknownSymbolException.class, expression::evaluate);

        assertEquals("Unknown variable 'fred'", actual.getMessage(), "Unexpected message");
    }

    /**
     * Integration test {@link Eel}
     */
    @Test
    public void test_use_default() {
        Result actual = Eel.compile(context, "${key-default value}")
            .evaluate();

        assertEquals(Type.TEXT, actual.getType(), "Unexpected type");
        assertEquals("default value", actual.asText(), "Unexpected value");
    }

    /**
     * Integration test {@link Eel} unrequired
     */
    @Test
    public void test_default_unrequired() {
        Result actual = Eel.compile(context, "${key-default value}")
            .evaluate(SymbolsTable.from(Map.of("key", "Value")));

        assertEquals(Type.TEXT, actual.getType(), "Unexpected type");
        assertEquals("Value", actual.asText(), "Unexpected value");
    }

    /**
     * Integration test {@link Eel}
     */
    @Test
    public void test_use_default_withEmbeddedExpression() {
        Result actual = Eel.compile(context, "${key-~$( 1 + number(${two}) )~${postfix}~}!")
            .evaluate(SymbolsTable.from(Map.of("two", "2", "postfix", "@@")));

        assertEquals(Type.TEXT, actual.getType(), "Unexpected type");
        assertEquals("~3~@@~!", actual.asText(), "Unexpected value");
    }

    /**
     * Integration test {@link Eel}
     */
    @Test
    public void test_FirstUpper() {
        Result actual = Eel.compile(context, "${var^}")
            .evaluate(SymbolsTable.from(Map.of("var", "lower")));

        assertEquals(Type.TEXT, actual.getType(), "Unexpected type");
        assertEquals("Lower", actual.asText(), "Unexpected value");
    }

    /**
     * Integration test {@link Eel}
     */
    @Test
    public void test_AllUpper() {
        Result actual = Eel.compile(context, "${var^^}")
            .evaluate(SymbolsTable.from(Map.of("var", "lower")));

        assertEquals(Type.TEXT, actual.getType(), "Unexpected type");
        assertEquals("LOWER", actual.asText(), "Unexpected value");
    }

    /**
     * Integration test {@link Eel}
     */
    @Test
    public void test_FirstLower() {
        Result actual = Eel.compile(context, "${var,}")
            .evaluate(SymbolsTable.from(Map.of("var", "UPPER")));

        assertEquals(Type.TEXT, actual.getType(), "Unexpected type");
        assertEquals("uPPER", actual.asText(), "Unexpected value");
    }


    /**
     * Integration test {@link Eel}
     */
    @Test
    public void test_AllLower() {
        Result actual = Eel.compile(context, "${var,,}")
            .evaluate(SymbolsTable.from(Map.of("var", "UPPER")));

        assertEquals(Type.TEXT, actual.getType(), "Unexpected type");
        assertEquals("upper", actual.asText(), "Unexpected value");
    }

    /**
     * Integration test {@link Eel}
     */
    @Test
    public void test_FirstToggle() {
        Result actual = Eel.compile(context, "${var1~} ${var2~}")
            .evaluate(SymbolsTable.from(Map.of("var1", "UPPER", "var2", "lower")));

        assertEquals(Type.TEXT, actual.getType(), "Unexpected type");
        assertEquals("uPPER Lower", actual.asText(), "Unexpected value");
    }

    /**
     * Integration test {@link Eel}
     */
    @Test
    public void test_AllToggle() {
        Result actual = Eel.compile(context, "${var1~~} ${var2~~}")
            .evaluate(SymbolsTable.from(Map.of("var1", "UPPER", "var2", "lower")));

        assertEquals(Type.TEXT, actual.getType(), "Unexpected type");
        assertEquals("upper LOWER", actual.asText(), "Unexpected value");
    }

    /**
     * Integration test {@link Eel}
     */
    @Test
    public void test_MultipleCases() {
        Result actual = Eel.compile(context, "${var1,,^} ${var2,,^}")
            .evaluate(SymbolsTable.from(Map.of("var1", "UPPER", "var2", "lower")));

        assertEquals(Type.TEXT, actual.getType(), "Unexpected type");
        assertEquals("Upper Lower", actual.asText(), "Unexpected value");
    }

    /**
     * Integration test {@link Eel}
     */
    @Test
    public void test_caseChange_and_default() {
        Result actual = Eel.compile(context, "${key^^-default value}")
            .evaluate(SymbolsTable.from(Map.of("key", "Value")));

        assertEquals(Type.TEXT, actual.getType(), "Unexpected type");
        assertEquals("VALUE", actual.asText(), "Unexpected value");
    }

    /**
     * Integration test {@link Eel}
     */
    @Test
    public void test_caseChange_and_default2() {
        Result actual = Eel.compile(context, "${key^^-default value}")
            .evaluate();

        assertEquals(Type.TEXT, actual.getType(), "Unexpected type");
        assertEquals("default value", actual.asText(), "Unexpected value");
    }

    /**
     * Integration test {@link Eel}
     */
    @Test
    public void test_length() {
        Result actual = Eel.compile(context, "${#key}")
            .evaluate(SymbolsTable.from(Map.of("key", "123456")));

        assertEquals(Type.TEXT, actual.getType(), "Unexpected type");
        assertEquals("6", actual.asText(), "Unexpected value");
    }

    /**
     * Integration test {@link Eel}
     */
    @Test
    public void test_defaultAndLength() {
        Result actual = Eel.compile(context, "${#key-12}")
            .evaluate();

        assertEquals(Type.TEXT, actual.getType(), "Unexpected type");
        assertEquals("12", actual.asText(), "Unexpected value");
    }

    /**
     * Integration test {@link Eel}
     */
    @Test
    public void test_changeCaseAndLength() {
        Result actual = Eel.compile(context, "${#key^^}")             // changes of case should be ignored
            .evaluate(SymbolsTable.from(Map.of("key", "123456")));

        assertEquals(Type.TEXT, actual.getType(), "Unexpected type");
        assertEquals("6", actual.asText(), "Unexpected value");
    }


    /**
     * Integration test {@link Eel}
     */
    @Test
    public void test_substring() {
        substringHelper("just index", "${key:3}", "3456789");
        substringHelper("index and length", "${key:3:2}", "34");

        substringHelper("positive index", "${key:+3:2}", "34");
        substringHelper("positive index with space", "${key: +3:2}", "34");
        substringHelper("negative index", "${key: -3:2}", "78");
        substringHelper("positive count", "${key:3:+2}", "34");
        substringHelper("positive count with space", "${key:3: +2}", "34");
        substringHelper("negative count", "${key:3: -2}", "34567");
        substringHelper("negative index and count with space", "${key: -3: -2}", "7");

        substringHelper("index is value", "${text:${index}:4}", "DEFG");
        substringHelper("index is expression", "${text:$( 1 + 2 ):4}", "DEFG");
        substringHelper("index is term", "${text:(1+2):4}", "DEFG");
        substringHelper("index is function", "${text:$indexOf('123456789', '4'):4}", "DEFG");

        substringHelper("count is value", "${text:3:${count}}", "DEFG");
        substringHelper("count is expression", "${text:3:$(5 - 1)}", "DEFG");
        substringHelper("count is term", "${text:3:(5 - 1)}", "DEFG");
        substringHelper("count is function", "${text:3:$indexOf('123456789', '5')}", "DEFG");

        substringHelper("with default (unused)", "${key:3:2-undefined}", "34");
        substringHelper("with default (used)", "${unknown:3:2-undefined}", "undefined");
        substringHelper("with default (used) + negativeIndex", "${unknown:3: -2^^-undefined}", "undefined");
        substringHelper("case change + default", "${text:2:4,,~-undefined}", "Cdef");

        substringHelper("just index and default (unused)", "${key:3:-default}", "3456789");
        substringHelper("just index and default (used)", "${unknown:3-default}", "default");
        substringHelper("just index and empty/default (empty)", "${empty:3:-default}", "default");
        substringHelper("just index and empty/default (default)", "${unknown:3:-default}", "default");
    }

    private void substringHelper(@Nonnull String message, @Nonnull String expression, @Nonnull String expected) {
        Result actual = Eel.compile(context, expression)
            .evaluate(SymbolsTable.from(Map.ofEntries(
                entry("empty", ""),
                entry("key", "0123456789"),
                entry("text", "ABCDEFGHI"),
                entry("index", "3"),
                entry("count", "4"))
            ));

        assertEquals(Type.TEXT, actual.getType(), message + ": Unexpected type");
        assertEquals(expected, actual.asText(), message + ": Unexpected value");
    }


    /**
     * Integration test {@link Eel}
     */
    @Test
    public void test_count_value_caseChange() {
        Result actual = Eel.compile(context, "${key:3:${count},,}")
            .evaluate(SymbolsTable.from(Map.ofEntries(
                entry("key", "ABCDEFGHI"),
                entry("count", "4"))));

        assertEquals(Type.TEXT, actual.getType(), "Unexpected type");
        assertEquals("defg", actual.asText(), "Unexpected value");
    }
}
