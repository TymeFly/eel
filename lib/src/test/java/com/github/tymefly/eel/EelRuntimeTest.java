package com.github.tymefly.eel;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;

import javax.annotation.Nonnull;

import com.github.tymefly.eel.exception.EelException;
import com.github.tymefly.eel.exception.EelInterruptedException;
import com.github.tymefly.eel.exception.EelRuntimeException;
import com.github.tymefly.eel.exception.EelTimeoutException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Unit test for {@link EelRuntime}
 */
public class EelRuntimeTest {
    private EelContextImpl context;


    @BeforeEach
    public void setUp() {
        context = mock();

        when(context.getTimeout())
            .thenReturn(Duration.of(2, ChronoUnit.SECONDS));
    }

                //*** No timeout ***//

    /**
     * Unit test {@link EelRuntime#wrap(Term)}
     */
    @Test
    public void test_noTimeout_happyPath() {
        when(context.getTimeout())
            .thenReturn(Duration.of(0, ChronoUnit.SECONDS));

        Term backing = s -> Constant.of(1);
        Expression wrapped = new EelRuntime(context).wrap(backing);
        SymbolsTable table = mock();

        Result actual = wrapped.evaluate(table);

        assertEquals(Type.NUMBER, actual.getType(), "Unexpected Type");
        assertEquals(BigDecimal.ONE, actual.asNumber(), "Unexpected Value");
    }

    /**
     * Unit test {@link EelRuntime#wrap(Term)}
     */
    @Test
    public void test_noTimeoutRequired_happyPath() {
        when(context.getTimeout())
            .thenReturn(Duration.of(1, ChronoUnit.MICROS));

        Term backing = Constant.of(1);                  // Constants don't need timeouts
        Expression wrapped = new EelRuntime(context).wrap(backing);
        SymbolsTable table = mock();

        Result actual = wrapped.evaluate(table);

        assertEquals(Type.NUMBER, actual.getType(), "Unexpected Type");
        assertEquals(BigDecimal.ONE, actual.asNumber(), "Unexpected Value");
    }


    /**
     * Unit test {@link EelRuntime#wrap(Term)}
     */
    @Test
    public void test_noTimeout_eelException() {
        when(context.getTimeout())
            .thenReturn(Duration.of(0, ChronoUnit.SECONDS));

        EelException cause = new EelRuntimeException("expected");
        Term backing = s -> { throw cause; };
        Expression wrapped = new EelRuntime(context).wrap(backing);
        SymbolsTable table = mock();

        EelRuntimeException actual = assertThrows(EelRuntimeException.class, () -> wrapped.evaluate(table));

        assertSame(cause, actual, "Unexpected cause");
        assertStack(actual, "test_noTimeout_eelException", false);
    }


    /**
     * Unit test {@link EelRuntime#wrap(Term)}
     */
    @Test
    public void test_noTimeout_otherException() {
        when(context.getTimeout())
            .thenReturn(Duration.of(0, ChronoUnit.SECONDS));

        RuntimeException cause = new ArithmeticException("expected");
        Term backing = s -> { throw cause; };
        Expression wrapped = new EelRuntime(context).wrap(backing);
        SymbolsTable table = mock();

        EelRuntimeException actual = assertThrows(EelRuntimeException.class, () -> wrapped.evaluate(table));

        assertEquals("EEL execution failed", actual.getMessage(), "Unexpected message");
        assertEquals(cause, actual.getCause(), "Unexpected cause");
        assertStack(actual, "test_noTimeout_otherException", false);
    }

                //*** With timeout ***//

    /**
     * Unit test {@link EelRuntime#wrap(Term)}
     */
    @Test
    public void test_withTimeout_happyPath() {
        Term backing = s -> Constant.of(1);
        Expression wrapped = new EelRuntime(context).wrap(backing);
        SymbolsTable table = mock();

        Result actual = wrapped.evaluate(table);

        assertEquals(Type.NUMBER, actual.getType(), "Unexpected Type");
        assertEquals(BigDecimal.ONE, actual.asNumber(), "Unexpected Value");
    }


    /**
     * Unit test {@link EelRuntime#wrap(Term)}
     */
    @Test
    public void test_withTimeout_timedOut() {
        Term backing = s -> {
            try {
                Thread.sleep(5_000);
            } catch (Exception e) {
            }

            return Value.of("");
        };
        Expression wrapped = new EelRuntime(context).wrap(backing);
        SymbolsTable table = mock();

        EelRuntimeException actual = assertThrows(EelTimeoutException.class, () -> wrapped.evaluate(table));

        assertEquals("EEL Timeout after 2 second(s)", actual.getMessage(), "Unexpected message");
        assertStack(actual, "test_withTimeout_timedOut", false);
    }


    /**
     * Unit test {@link EelRuntime#wrap(Term)}
     */
    @Test
    public void test_withTimeout_eelException() {
        EelException cause = new EelRuntimeException("expected");
        Term backing = s -> { throw cause; };
        Expression wrapped = new EelRuntime(context).wrap(backing);
        SymbolsTable table = mock();

        EelRuntimeException actual = assertThrows(EelRuntimeException.class, () -> wrapped.evaluate(table));

        assertSame(cause, actual, "Unexpected cause");
        assertStack(actual, "test_withTimeout_eelException", false);
    }


    /**
     * Unit test {@link EelRuntime#wrap(Term)}
     */
    @Test
    public void test_withTimeout_otherException() {
        RuntimeException cause = new ArithmeticException("expected");
        Term backing = s -> { throw cause; };
        Expression wrapped = new EelRuntime(context).wrap(backing);
        SymbolsTable table = mock();

        EelRuntimeException actual = assertThrows(EelRuntimeException.class, () -> wrapped.evaluate(table));

        assertEquals("EEL execution failed", actual.getMessage(), "Unexpected message");
        assertEquals(cause, actual.getCause(), "Unexpected cause");
        assertStack(actual, "test_withTimeout_otherException", false);
    }


    private Exception exception;

    /**
     * Unit test {@link EelRuntime#wrap(Term)}
     */
    @Test
    public void test_withTimeout_interruptThread() throws InterruptedException {
        Term backing = s -> {
            try {
                Thread.sleep(5_000);
            } catch (Exception e) {
            }

            return Value.of(true);
        };

        Expression wrapped = new EelRuntime(context).wrap(backing);
        SymbolsTable table = mock();

        Runnable runner = () -> {
            try {
                wrapped.evaluate(table);
            } catch (Exception e) {
                exception = e;
            }
        };
        Thread thread = new Thread(runner);

        thread.start();
        Thread.sleep(250);

        thread.interrupt();
        Thread.sleep(250);

        assertNotNull(exception, "Exception was not thrown");
        assertEquals(EelInterruptedException.class, exception.getClass(), "Exception has unexpected type");
        assertEquals("EEL execution was interrupted", exception.getMessage(), "Unexpected message");
        assertStack(exception, "test_withTimeout_interruptThread", true);
    }


    private void assertStack(@Nonnull Exception actual, @Nonnull String callingMethod, boolean substring) {
        boolean found = Arrays.stream(actual.getStackTrace())
            .map(StackTraceElement::getMethodName)
            .anyMatch(m -> substring ? m.contains(callingMethod) : m.equals(callingMethod));

        if (!found) {
            StringWriter trace = new StringWriter();
            actual.printStackTrace(new PrintWriter(trace));
            fail("Calling method '" + callingMethod + "' not in stack trace: " + trace);
        }
    }
}