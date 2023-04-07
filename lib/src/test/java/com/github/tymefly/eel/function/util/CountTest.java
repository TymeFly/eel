package com.github.tymefly.eel.function.util;

import com.github.tymefly.eel.EelContext;
import org.junit.Assert;
import org.junit.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Unit test for {@link Count}
 */
public class CountTest {
    /**
     * Unit test {@link Count#count(EelContext)}
     */
    @Test
    public void test_Count_sameContext() {
        EelContext context = mock(EelContext.class);

        Count count = new Count();

        Assert.assertEquals("First", 1, count.count(context));
        Assert.assertEquals("Second", 2, count.count(context));
        Assert.assertEquals("Third", 3, count.count(context));
    }

    /**
     * Unit test {@link Count#count(EelContext)}
     */
    @Test
    public void test_Count_flipContexts() {
        EelContext context1 = mock(EelContext.class);
        EelContext context2 = mock(EelContext.class);
        EelContext context3 = mock(EelContext.class);

        when(context1.contextId()).thenReturn("Context1");
        when(context2.contextId()).thenReturn("Context2");
        when(context3.contextId()).thenReturn("Context3");

        Count count = new Count();

        Assert.assertEquals("Context 1", 1, count.count(context1));
        Assert.assertEquals("Context 2", 1, count.count(context2));
        Assert.assertEquals("Context 3", 1, count.count(context3));

        Assert.assertEquals("Context 1 again", 2, count.count(context1));
        Assert.assertEquals("Context 2 again", 2, count.count(context2));
        Assert.assertEquals("Context 3 again", 2, count.count(context3));
    }
}