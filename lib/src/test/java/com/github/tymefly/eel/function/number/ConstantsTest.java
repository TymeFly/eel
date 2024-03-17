package com.github.tymefly.eel.function.number;

import org.junit.Assert;
import org.junit.Test;

/**
 * Unit test for {@link Constants}
 */
public class ConstantsTest {
    private static final double PRECISION = 0.000_000_000_000_1;

    /**
     * Unit test {@link Constants#e(), {@link Constants#pi()} and {@link Constants#c()}
     */
    @Test
    public void test_constants() {
        Assert.assertEquals("e", 2.7182818284590452354, new Constants().e(), PRECISION);
        Assert.assertEquals("pi", 3.14159265358979323846, new Constants().pi(), PRECISION);
        Assert.assertEquals("c", 299_792_458, new Constants().c());
    }
}