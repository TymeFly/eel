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
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Unit test for {@link EelRuntime}
 */
public class EelRuntimeTest {
    private EelContextImpl context;


    @Before
    public void setUp() {
        context = mock(EelContextImpl.class);

        when(context.getTimeout())
            .thenReturn(Duration.of(2, ChronoUnit.SECONDS));
    }

                //*** No timeout ***//

    /**
     * Unit test {@link EelRuntime#apply(Executor)}
     */
    @Test
    public void test_noTimeout_happyPath() {
        when(context.getTimeout())
            .thenReturn(Duration.of(0, ChronoUnit.SECONDS));

        Executor backing = s -> Value.of(1);
        Executor wrapped = new EelRuntime(context).apply(backing);
        SymbolsTable table = mock(SymbolsTable.class);

        Value actual = wrapped.execute(table);

        Assert.assertEquals("Unexpected Type", Type.NUMBER, actual.getType());
        Assert.assertEquals("Unexpected Value", BigDecimal.ONE, actual.asNumber());
    }


    /**
     * Unit test {@link EelRuntime#apply(Executor)}
     */
    @Test
    public void test_noTimeout_eelException() {
        when(context.getTimeout())
            .thenReturn(Duration.of(0, ChronoUnit.SECONDS));

        EelException cause = new EelRuntimeException("expected");
        Executor backing = s -> { throw cause; };
        Executor wrapped = new EelRuntime(context).apply(backing);
        SymbolsTable table = mock(SymbolsTable.class);

        EelRuntimeException actual = Assert.assertThrows(EelRuntimeException.class, () -> wrapped.execute(table));

        Assert.assertSame("Unexpected cause", cause, actual);
        assertStack(actual, "test_noTimeout_eelException", false);
    }


    /**
     * Unit test {@link EelRuntime#apply(Executor)}
     */
    @Test
    public void test_noTimeout_otherException() {
        when(context.getTimeout())
            .thenReturn(Duration.of(0, ChronoUnit.SECONDS));

        RuntimeException cause = new ArithmeticException("expected");
        Executor backing = s -> { throw cause; };
        Executor wrapped = new EelRuntime(context).apply(backing);
        SymbolsTable table = mock(SymbolsTable.class);

        EelRuntimeException actual = Assert.assertThrows(EelRuntimeException.class, () -> wrapped.execute(table));

        Assert.assertEquals("Unexpected message", "EEL execution failed", actual.getMessage());
        Assert.assertEquals("Unexpected cause", cause, actual.getCause());
        assertStack(actual, "test_noTimeout_otherException", false);
    }

                //*** With timeout ***//

    /**
     * Unit test {@link EelRuntime#apply(Executor)}
     */
    @Test
    public void test_withTimeout_happyPath() {
        Executor backing = s -> Value.of(1);
        Executor wrapped = new EelRuntime(context).apply(backing);
        SymbolsTable table = mock(SymbolsTable.class);

        Value actual = wrapped.execute(table);

        Assert.assertEquals("Unexpected Type", Type.NUMBER, actual.getType());
        Assert.assertEquals("Unexpected Value", BigDecimal.ONE, actual.asNumber());
    }


    /**
     * Unit test {@link EelRuntime#apply(Executor)}
     */
    @Test
    public void test_withTimeout_timedOut() {
        Executor backing = s -> {
            try {
                Thread.sleep(5_000);
            } catch (Exception e) {
            }

            return Value.of("");
        };
        Executor wrapped = new EelRuntime(context).apply(backing);
        SymbolsTable table = mock(SymbolsTable.class);

        EelRuntimeException actual = Assert.assertThrows(EelTimeoutException.class, () -> wrapped.execute(table));

        Assert.assertEquals("Unexpected message", "EEL Timeout after 2 second(s)", actual.getMessage());
        assertStack(actual, "test_withTimeout_timedOut", false);
    }


    /**
     * Unit test {@link EelRuntime#apply(Executor)}
     */
    @Test
    public void test_withTimeout_eelException() {
        EelException cause = new EelRuntimeException("expected");
        Executor backing = s -> { throw cause; };
        Executor wrapped = new EelRuntime(context).apply(backing);
        SymbolsTable table = mock(SymbolsTable.class);

        EelRuntimeException actual = Assert.assertThrows(EelRuntimeException.class, () -> wrapped.execute(table));

        Assert.assertSame("Unexpected cause", cause, actual);
        assertStack(actual, "test_withTimeout_eelException", false);
    }


    /**
     * Unit test {@link EelRuntime#apply(Executor)}
     */
    @Test
    public void test_withTimeout_otherException() {
        RuntimeException cause = new ArithmeticException("expected");
        Executor backing = s -> { throw cause; };
        Executor wrapped = new EelRuntime(context).apply(backing);
        SymbolsTable table = mock(SymbolsTable.class);

        EelRuntimeException actual = Assert.assertThrows(EelRuntimeException.class, () -> wrapped.execute(table));

        Assert.assertEquals("Unexpected message", "EEL execution failed", actual.getMessage());
        Assert.assertEquals("Unexpected cause", cause, actual.getCause());
        assertStack(actual, "test_withTimeout_otherException", false);
    }


    private Exception exception;

    /**
     * Unit test {@link EelRuntime#apply(Executor)}
     */
    @Test
    public void test_withTimeout_interruptThread() throws InterruptedException {
        Executor backing = s -> {
            try {
                Thread.sleep(5_000);
            } catch (Exception e) {
            }

            return Value.of(true);
        };

        Executor wrapped = new EelRuntime(context).apply(backing);
        SymbolsTable table = mock(SymbolsTable.class);

        Runnable runner = () -> {
            try {
                wrapped.execute(table);
            } catch (Exception e) {
                exception = e;
            }
        };
        Thread thread = new Thread(runner);

        thread.start();
        Thread.sleep(250);

        thread.interrupt();
        Thread.sleep(250);

        Assert.assertNotNull("Exception was not thrown", exception);
        Assert.assertEquals("Exception has unexpected type", EelInterruptedException.class, exception.getClass());
        Assert.assertEquals("Unexpected message", "EEL execution was interrupted", exception.getMessage());
        assertStack(exception, "test_withTimeout_interruptThread", true);
    }


    private void assertStack(@Nonnull Exception actual, @Nonnull String callingMethod, boolean substring) {
        boolean found = Arrays.stream(actual.getStackTrace())
            .map(StackTraceElement::getMethodName)
            .anyMatch(m -> substring ? m.contains(callingMethod) : m.equals(callingMethod));

        if (!found) {
            StringWriter trace = new StringWriter();
            actual.printStackTrace(new PrintWriter(trace));
            Assert.fail("Calling method '" + callingMethod + "' not in stack trace: " + trace);
        }
    }
}