package com.github.tymefly.eel;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.stream.IntStream;

import javax.annotation.Nonnull;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import uk.org.webcompere.systemstubs.rules.SystemErrRule;
import uk.org.webcompere.systemstubs.rules.SystemOutRule;

/**
 * Test EEL can evaluate expressions concurrently
 */
public class ConcurrencyIntegrationTest {
    @Rule
    public SystemOutRule stdOut = new SystemOutRule();

    @Rule
    public SystemErrRule stdErr = new SystemErrRule();

    // Extend timeout because the scheduler might starve one of the threads
    private static final Duration TIMEOUT = Duration.ofSeconds(60);


    private final SymbolsTable symbols = SymbolsTable.factory()
        .withValues(Map.ofEntries(
            Map.entry("a", "1"),
            Map.entry("b", "2"),
            Map.entry("c", "WORLD!!!!"),
            Map.entry("d", "20")))
        .build();
    private final List<Consumer<Integer>> tests = List.of(
            this::test1,
            this::test2,
            this::test3,
            this::test4,
            this::test5
        );
    private final String[][] functions = {
        { "$lower(${c})", "world!!!!" },
        { "$upper(${c})", "WORLD!!!!" },
        { "$title(${c})", "World!!!!" },
        { "$len(${c})", "9" },
        { "$isEmpty(${c})", "false" },
        { "$isBlank(${c})", "false" },
        { "$indexOf(${c}, 'O')", "1" },
        { "$lastIndexOf(${c}, 'O')", "1" },
        { "$contains(${c}, '!')", "4" },
        { "$left(${c}, 2)", "WO" },
        { "$mid(${c}, 2, 2)", "RL" },
        { "$right(${c}, 5)", "D!!!!" },
        { "$before(${c}, '!', 1)", "WORLD" },
        { "$after(${c}, '!', 2)", "!!" },
        { "$beforeFirst(${c}, '!')", "WORLD" },
        { "$afterFirst(${c}, '!')", "!!!" },
        { "$beforeLast(${c}, '!')", "WORLD!!!" },
        { "$afterLast(${c}, '!')", "" },
        { "$max(1, 2, 3)", "3" },
        { "$min(1, 2, 3)", "1" },
        { "$avg(1, 2, 3)", "2" },
        { "$format.binary(${d})", "10100" },
        { "$format.octal(${d})", "24" },
        { "$format.hex(${d})", "14" },
        { "$format.number(${d}, 3)", "202" },
        { "$('<<'; '>>'; $[1] ~> '$random(99, 99)' ~> $[2])", "<<99>>" }
    };
    private final AtomicInteger functionsIndex = new AtomicInteger(0);


    /**
     * Integration test {@link Eel}
     */
    @Test
    public void test_Concurrency() {
        IntStream.rangeClosed(0, 1024)
            .parallel()
            .forEach(i -> tests.get(i % tests.size()).accept(i));
    }


    // logical expression
    private void test1(@Nonnull Integer index) {
        boolean result = Eel.factory()
            .withTimeout(TIMEOUT)
            .compile("$( true and ( ${a} = 1 ) )")
            .evaluate(symbols)
            .asLogic();

        Assert.assertTrue("Unexpected result", result);
    }

    // Numeric expression
    private void test2(@Nonnull Integer index) {
        long result = Eel.factory()
            .withTimeout(TIMEOUT)
            .compile("$( (number.e() ** number.pi()) << ${b} )")
            .evaluate(symbols)
            .asLong();

        Assert.assertEquals("Unexpected result", 92, result);
    }

    // String manipulation
    private void test3(@Nonnull Integer index) {
        String result = Eel.factory()
            .withTimeout(TIMEOUT)
            .compile("Hello: ${c:0:6,,^}")
            .evaluate(symbols)
            .asText();

        Assert.assertEquals("Unexpected result", "Hello: World!", result);
    }

    // create lots of different values
    private void test4(@Nonnull Integer index) {
        String expression = "$random(" + index + "," + index + ")";

        Integer result = Eel.factory()
            .withTimeout(TIMEOUT)
            .compile(expression)
            .evaluate(symbols)
            .asInt();

        Assert.assertEquals("Unexpected result", index, result);
    }

    // Invoke many different functions
    private void test5(@Nonnull Integer index) {
        int functionIndex = functionsIndex.getAndIncrement() % functions.length;
        String expression = functions[functionIndex][0];
        String expected = functions[functionIndex][1];
        String result = Eel.factory()
            .withTimeout(TIMEOUT)
            .compile(expression)
            .evaluate(symbols)
            .asText();

        Assert.assertEquals("Unexpected result for " + expression, expected, result);
    }
}