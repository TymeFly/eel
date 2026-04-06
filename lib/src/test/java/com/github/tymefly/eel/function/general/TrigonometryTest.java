package com.github.tymefly.eel.function.general;

import java.math.BigDecimal;

import com.github.tymefly.eel.EelContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Unit test for {@link Trigonometry}
 */
public class TrigonometryTest {
    private EelContext context;

    @BeforeEach
    public void setUp() {
        context = EelContext.factory()
            .withPrecision(5)
            .build();
    }

    /**
     * Unit test {@link Trigonometry#sin(EelContext, BigDecimal)}
     */
    @Test
    public void test_Sin() {
        assertEquals(new BigDecimal("-0.21891"), new Trigonometry().sin(context, new BigDecimal("12.345678")), "sin");
    }


    /**
     * Unit test {@link Trigonometry#cos(EelContext, BigDecimal)}
     */
    @Test
    public void test_cos() {
        assertEquals(new BigDecimal("0.32993"), new Trigonometry().cos(context, new BigDecimal("1.2345678")), "cos");
    }


    /**
     * Unit test {@link Trigonometry#tan(EelContext, BigDecimal)}
     */
    @Test
    public void test_tan() {
        assertEquals(new BigDecimal("-0.22435"), new Trigonometry().tan(context, new BigDecimal("12.345678")), "tan");
    }


    /**
     * Unit test {@link Trigonometry#asin(EelContext, BigDecimal)}
     */
    @Test
    public void test_asin() {
        assertEquals(new BigDecimal("0.12377"), new Trigonometry().asin(context, new BigDecimal("0.12345678")), "asin");
    }

    /**
     * Unit test {@link Trigonometry#asin(EelContext, BigDecimal)}
     */
    @Test
    public void test_asin_rangeError() {
        assertThrows(ArithmeticException.class,
            () -> new Trigonometry().asin(context, new BigDecimal("12345.678")));
    }


    /**
     * Unit test {@link Trigonometry#acos(EelContext, BigDecimal)}
     */
    @Test
    public void test_acos() {
        assertEquals(new BigDecimal("1.4470"), new Trigonometry().acos(context, new BigDecimal("0.12345678")), "acos");
    }

    /**
     * Unit test {@link Trigonometry#acos(EelContext, BigDecimal)}
     */
    @Test
    public void test_acos_rangeError() {
        assertThrows(ArithmeticException.class,
            () -> new Trigonometry().acos(context, new BigDecimal("12345.678")));
    }


    /**
     * Unit test {@link Trigonometry#atan(EelContext, BigDecimal)}
     */
    @Test
    public void test_Atan() {
        assertEquals(new BigDecimal("1.5627"), new Trigonometry().atan(context, new BigDecimal("123.45678")), "atan");
    }
}