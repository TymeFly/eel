package com.github.tymefly.eel.function.number;

import java.math.BigDecimal;

import com.github.tymefly.eel.EelContext;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Unit test for {@link Constants}
 */
public class ConstantsTest {
    private EelContext context;


    @Before
    public void setUp() {
        context = EelContext.factory()
            .withPrecision(8)
            .build();
    }

    /**
     * Unit test {@link Constants#e(), {@link Constants#pi()} and {@link Constants#c()}
     */
    @Test
    public void test_constants() {
        Assert.assertEquals("e", new BigDecimal("2.7182818"), new Constants().e(context));
        Assert.assertEquals("pi", new BigDecimal("3.1415927"), new Constants().pi(context));
        Assert.assertEquals("c", 299_792_458, new Constants().c());
    }
}