package com.github.tymefly.eel.function.general;

import java.math.BigDecimal;

import com.github.tymefly.eel.EelContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Unit test for {@link Maths}
 */
public class MathsTest {
    private EelContext context;

    @BeforeEach
    public void setUp() {
        context = EelContext.factory()
            .withPrecision(5)
            .build();
    }

    /**
     * Unit test {@link Maths#abs(EelContext, BigDecimal)}
     */
    @Test
    public void test_abs() {
        assertEquals(new BigDecimal("123.46"), new Maths().abs(context, new BigDecimal("123.45678")), "Positive");
        assertEquals(new BigDecimal("123.46"), new Maths().abs(context, new BigDecimal("-123.45678")), "Negative");
    }

    /**
     * Unit test {@link Maths#sgn}
     */
    @Test
    public void test_sgn() {
        assertEquals(-1, new Maths().sgn(new BigDecimal("-0.1")), "negative value");
        assertEquals(0, new Maths().sgn(BigDecimal.ZERO), "zero");
        assertEquals(1, new Maths().sgn(new BigDecimal("0.1")), "positive value");
    }

    /**
     * Unit test {@link Maths#exp(EelContext, BigDecimal)}
     */
    @Test
    public void test_Exp() {
        assertEquals(new BigDecimal("3.4369"), new Maths().exp(context, new BigDecimal("1.2345678")), "exp");
    }


    /**
     * Unit test {@link Maths#factorial(EelContext, BigDecimal)}
     */
    @Test
    public void test_factorial() {
        assertEquals(new BigDecimal("40320"), new Maths().factorial(context, new BigDecimal("8")), "integer");
        assertEquals(new BigDecimal("52.343"), new Maths().factorial(context, new BigDecimal("4.5")), "fractional");
    }

    /**
     * Unit test {@link Maths#factorial(EelContext, BigDecimal)}
     */
    @Test
    public void test_factorial_rangeError() {
        assertThrows(ArithmeticException.class,
            () -> new Maths().factorial(context, new BigDecimal("-8")));
    }


    /**
     * Unit test {@link Maths#ln(EelContext, BigDecimal)}
     */
    @Test
    public void test_ln() {
        assertEquals(new BigDecimal("7.1185"), new Maths().ln(context, new BigDecimal("1234.5678")), "ln");
    }

    /**
     * Unit test {@link Maths#ln(EelContext, BigDecimal)}
     */
    @Test
    public void test_ln_rangeError() {
        assertThrows(ArithmeticException.class,
            () -> new Maths().ln(context, new BigDecimal("-1234.5678")));
    }



    /**
     * Unit test {@link Maths#log(EelContext, BigDecimal)}
     */
    @Test
    public void test_log() {
        assertEquals(new BigDecimal("3.0915"), new Maths().log(context, new BigDecimal("1234.5678")), "log");
    }

    /**
     * Unit test {@link Maths#log(EelContext, BigDecimal)}
     */
    @Test
    public void test_log_rangeError() {
        assertThrows(ArithmeticException.class,
            () -> new Maths().log(context, new BigDecimal("-1234.5678")));
    }


    /**
     * Unit test {@link Maths#root(EelContext, BigDecimal, BigDecimal)}
     */
    @Test
    public void test_root_positive() {
        assertEquals(new BigDecimal("6.5812"), new Maths().root(context, new BigDecimal("12345.678"), new BigDecimal("5")), "Positive");
    }

    /**
     * Unit test {@link Maths#root(EelContext, BigDecimal, BigDecimal)}
     */
    @Test
    public void test_root_rangeError() {
        assertThrows(ArithmeticException.class,
            () -> new Maths().root(context, new BigDecimal("-12345.678"), new BigDecimal("5")));
    }
}