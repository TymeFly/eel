package com.github.tymefly.eel.validate;

import org.junit.Assert;
import org.junit.Test;

/**
 * Unit test for {@link Preconditions}
 */
public class PreconditionsTest {

    /**
     * Unit test {@link Preconditions#checkNotNull}
     */
    @Test
    public void test_checkNotNull() {
        String result = Preconditions.checkNotNull("Not Null", "Hello %s", "world");

        Assert.assertSame("Unexpected result", "Not Null", result);


        Exception actual = Assert.assertThrows(NullPointerException.class,
                () -> Preconditions.checkNotNull(null, "Hello %s %d", "world", 1));

        Assert.assertEquals("Unexpected message", "Hello world 1", actual.getMessage());
    }

    /**
     * Unit test {@link Preconditions#checkState}
     */
    @Test
    public void test_checkState() {
        Preconditions.checkState(true, "Hello %s", "world");


        Exception actual = Assert.assertThrows(IllegalStateException.class,
                () -> Preconditions.checkState(false, "Hello %s %d", "world", 1));

        Assert.assertEquals("Unexpected message", "Hello world 1", actual.getMessage());
    }

    /**
     * Unit test {@link Preconditions#checkArgument}
     */
    @Test
    public void test_checkArgument() {
        Preconditions.checkArgument(true, "Hello %s", "world");


        Exception actual = Assert.assertThrows(IllegalArgumentException.class,
                () -> Preconditions.checkArgument(false, "Hello %s %d", "world", 1));

        Assert.assertEquals("Unexpected message", "Hello world 1", actual.getMessage());
    }
}