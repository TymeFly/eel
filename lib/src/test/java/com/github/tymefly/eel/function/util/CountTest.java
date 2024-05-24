package com.github.tymefly.eel.function.util;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import javax.annotation.Nonnull;

import com.github.tymefly.eel.FunctionalResource;
import org.junit.Assert;
import org.junit.Test;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit test for {@link Count}
 */
public class CountTest {
    private final Map<FunctionalResource, Map<String, Map<?, ?>>> counters = new HashMap<>();

    @Nonnull
    private FunctionalResource mockResourceManager() {
        FunctionalResource manager = mock();

        when(manager.getResource(anyString(), any(Function.class)))
            .thenAnswer(a -> counters.computeIfAbsent(manager, k -> new HashMap<>())
                .computeIfAbsent(a.getArgument(0), k -> new HashMap<>()));

        return manager;
    }



    /**
     * Unit test {@link Count#count(FunctionalResource, String)}
     */
    @Test
    public void test_Count_sameContext_0based() {
        FunctionalResource manager = mockResourceManager();

        Count count = new Count();

        Assert.assertEquals("First", 0, count.count(manager, Count.DEFAULT_COUNTER));
        Assert.assertEquals("Second", 1, count.count(manager, Count.DEFAULT_COUNTER));
        Assert.assertEquals("Third", 2, count.count(manager, Count.DEFAULT_COUNTER));

        verify(manager, times(3)).getResource(eq(""), any(Function.class));
    }


    /**
     * Unit test {@link Count#count(FunctionalResource, String)}
     */
    @Test
    public void test_Count_flipContexts() {
        FunctionalResource manager1 = mockResourceManager();
        FunctionalResource manager2 = mockResourceManager();
        FunctionalResource manager3 = mockResourceManager();

        Count count = new Count();

        Assert.assertEquals("Context 1", 0, count.count(manager1, Count.DEFAULT_COUNTER));
        Assert.assertEquals("Context 2", 0, count.count(manager2, Count.DEFAULT_COUNTER));
        Assert.assertEquals("Context 3", 0, count.count(manager3, Count.DEFAULT_COUNTER));

        Assert.assertEquals("Context 1 again", 1, count.count(manager1, Count.DEFAULT_COUNTER));
        Assert.assertEquals("Context 2 again", 1, count.count(manager2, Count.DEFAULT_COUNTER));
        Assert.assertEquals("Context 3 again", 1, count.count(manager3, Count.DEFAULT_COUNTER));

        verify(manager1, times(2)).getResource(eq(""), any(Function.class));
        verify(manager2, times(2)).getResource(eq(""), any(Function.class));
        verify(manager3, times(2)).getResource(eq(""), any(Function.class));
    }

    /**
     * Unit test {@link Count#count(FunctionalResource, String)}
     */
    @Test
    public void test_Count_namedCounters() {
        FunctionalResource manager = mockResourceManager();

        Count count = new Count();

        Assert.assertEquals("Counter 1", 0, count.count(manager, "Counter1"));
        Assert.assertEquals("Counter 2", 0, count.count(manager, "Counter2"));
        Assert.assertEquals("Counter 3", 0, count.count(manager, "Counter3"));

        Assert.assertEquals("Counter 1 again", 1, count.count(manager, "Counter1"));
        Assert.assertEquals("Counter 2 again", 1, count.count(manager, "Counter2"));
        Assert.assertEquals("Counter 3 again", 1, count.count(manager, "Counter3"));

        verify(manager, times(2)).getResource(eq("Counter1"), any(Function.class));
        verify(manager, times(2)).getResource(eq("Counter2"), any(Function.class));
        verify(manager, times(2)).getResource(eq("Counter3"), any(Function.class));
    }
}