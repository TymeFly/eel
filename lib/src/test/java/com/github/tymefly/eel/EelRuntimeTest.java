package com.github.tymefly.eel;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.temporal.ChronoUnit;

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

    /**
     * Unit test {@link EelRuntime#apply(Executor)}
     */
    @Test
    public void test_wrap_happyPath() {
        Executor backing = s -> Value.ONE;
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
    public void test_wrap_timeout() {
        Executor backing = s -> {
            try {
                Thread.sleep(5_000);
            } catch (Exception e) {
            }

            return Value.BLANK;
        };
        Executor wrapped = new EelRuntime(context).apply(backing);
        SymbolsTable table = mock(SymbolsTable.class);

        EelRuntimeException actual = Assert.assertThrows(EelTimeoutException.class, () -> wrapped.execute(table));

        Assert.assertEquals("Unexpected message", "EEL Timeout after 2 second(s)", actual.getMessage());
    }


    /**
     * Unit test {@link EelRuntime#apply(Executor)}
     */
    @Test
    public void test_wrap_eelException() {
        EelException cause = new EelRuntimeException("expected");
        Executor backing = s -> { throw cause; };
        Executor wrapped = new EelRuntime(context).apply(backing);
        SymbolsTable table = mock(SymbolsTable.class);

        EelRuntimeException actual = Assert.assertThrows(EelRuntimeException.class, () -> wrapped.execute(table));

        Assert.assertSame("Unexpected cause", cause, actual);
    }


    /**
     * Unit test {@link EelRuntime#apply(Executor)}
     */
    @Test
    public void test_wrap_otherExecutionException_withTimeout() {
        RuntimeException cause = new ArithmeticException("expected");
        Executor backing = s -> { throw cause; };
        Executor wrapped = new EelRuntime(context).apply(backing);
        SymbolsTable table = mock(SymbolsTable.class);

        EelRuntimeException actual = Assert.assertThrows(EelRuntimeException.class, () -> wrapped.execute(table));

        Assert.assertEquals("Unexpected message", "EEL execution failed", actual.getMessage());
        Assert.assertEquals("Unexpected cause", cause, actual.getCause());
    }

    /**
     * Unit test {@link EelRuntime#apply(Executor)}
     */
    @Test
    public void test_wrap_otherExecutionException_noTimeout() {
        when(context.getTimeout())
            .thenReturn(Duration.of(0, ChronoUnit.SECONDS));

        RuntimeException cause = new ArithmeticException("expected");
        Executor backing = s -> { throw cause; };
        Executor wrapped = new EelRuntime(context).apply(backing);
        SymbolsTable table = mock(SymbolsTable.class);

        EelRuntimeException actual = Assert.assertThrows(EelRuntimeException.class, () -> wrapped.execute(table));

        Assert.assertEquals("Unexpected message", "EEL execution failed", actual.getMessage());
        Assert.assertEquals("Unexpected cause", cause, actual.getCause());
    }


    private Result result;
    private Exception exception;

    /**
     * Unit test {@link EelRuntime#apply(Executor)}
     */
    @Test
    public void test_wrap_interruptThread() throws InterruptedException {
        Executor backing = s -> {
            try {
                Thread.sleep(5_000);
            } catch (Exception e) {
            }

            return Value.TRUE;
        };
        Executor wrapped = new EelRuntime(context).apply(backing);
        SymbolsTable table = mock(SymbolsTable.class);
        Runnable runner = () -> {
            try {
                result = wrapped.execute(table);
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
    }
}