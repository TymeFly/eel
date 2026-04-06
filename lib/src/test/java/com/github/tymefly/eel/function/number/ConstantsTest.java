package com.github.tymefly.eel.function.number;

import java.math.BigDecimal;

import com.github.tymefly.eel.EelContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Unit test for {@link Constants}
 */
public class ConstantsTest {
    private EelContext context;


    @BeforeEach
    public void setUp() {
        context = EelContext.factory()
            .withPrecision(8)
            .build();
    }

    /**
     * Unit test {@link Constants#e(EelContext)} , {@link Constants#pi(EelContext)} and {@link Constants#c()}
     */
    @Test
    public void test_constants() {
        assertEquals(new BigDecimal("2.7182818"), new Constants().e(context), "e");
        assertEquals(new BigDecimal("3.1415927"), new Constants().pi(context), "pi");
        assertEquals(299_792_458, new Constants().c(), "c");
    }
}