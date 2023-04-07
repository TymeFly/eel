package com.github.tymefly.eel;

import java.math.MathContext;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;

import func.functions.SameValue;
import func.functions.Sum;
import func.functions2.Half;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.MockedConstruction;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit test for {@link EelContextImpl}
 */
public class EelContextImplTest {
    /**
     * Unit test {@link EelContextImpl}
     */
    @Test
    public void test_defaultContext() {
        FunctionManager funcMan = mock(FunctionManager.class);

        try (
            MockedConstruction<FunctionManager.Builder> funcManConstructor =
                mockConstruction(FunctionManager.Builder.class, (c, s) -> when(c.build()).thenReturn(funcMan))
        ) {
            EelContextImpl context = (EelContextImpl) new EelContextImpl.Builder()
                .build();

            Assert.assertEquals("Bad MathContext", new MathContext(16, RoundingMode.HALF_UP), context.getMathContext());
            Assert.assertTrue("Bad Id" + context.contextId(), context.contextId().matches("_id\\d+"));
            Assert.assertEquals("functionManager", funcMan, context.getFunctionManager());

            FunctionManager.Builder funcManBuilder = funcManConstructor.constructed().get(0);

            verify(funcManBuilder, never()).withUdfClass(any(Class.class));
            verify(funcManBuilder, never()).withUdfPackage(any(Package.class));
        }
    }


    /**
     * Unit test {@link EelContextImpl#getTimeout()}
     */
    @Test
    public void test_withMaxLength() {
        FunctionManager funcMan = mock(FunctionManager.class);

        try (
            MockedConstruction<FunctionManager.Builder> funcManConstructor =
                mockConstruction(FunctionManager.Builder.class, (c, s) -> when(c.build()).thenReturn(funcMan))
        ) {
            EelContextImpl context = (EelContextImpl) new EelContextImpl.Builder()
                .withMaxExpressionSize(123)
                .build();

            Assert.assertEquals("Bad Max Expression Length", 123, context.maxExpressionLength());
            Assert.assertTrue("Bad Id" + context.contextId(), context.contextId().matches("_id\\d+"));
            Assert.assertEquals("functionManager", funcMan, context.getFunctionManager());

            FunctionManager.Builder funcManBuilder = funcManConstructor.constructed().get(0);

            verify(funcManBuilder, never()).withUdfClass(any(Class.class));
            verify(funcManBuilder, never()).withUdfPackage(any(Package.class));
        }
    }

    /**
     * Unit test {@link EelContextImpl#getTimeout()}
     */
    @Test
    public void test_withMaxLength_negative() {
        Assert.assertThrows(IllegalArgumentException.class, () -> new EelContextImpl.Builder()
            .withMaxExpressionSize(-123));
    }


    /**
     * Unit test {@link EelContextImpl#getTimeout()}
     */
    @Test
    public void test_getTimeout() {
        FunctionManager funcMan = mock(FunctionManager.class);

        try (
            MockedConstruction<FunctionManager.Builder> funcManConstructor =
                mockConstruction(FunctionManager.Builder.class, (c, s) -> when(c.build()).thenReturn(funcMan))
        ) {
            EelContextImpl context = (EelContextImpl) new EelContextImpl.Builder()
                .withTimeout(Duration.of(6, ChronoUnit.HOURS))
                .build();

            Assert.assertEquals("Bad Max Expression Length", Duration.of(6, ChronoUnit.HOURS), context.getTimeout());
            Assert.assertTrue("Bad Id" + context.contextId(), context.contextId().matches("_id\\d+"));
            Assert.assertEquals("functionManager", funcMan, context.getFunctionManager());

            FunctionManager.Builder funcManBuilder = funcManConstructor.constructed().get(0);

            verify(funcManBuilder, never()).withUdfClass(any(Class.class));
            verify(funcManBuilder, never()).withUdfPackage(any(Package.class));
        }
    }



    /**
     * Unit test {@link EelContextImpl#getTimeout()}
     */
    @Test
    public void test_getTimeout_negative() {
        Assert.assertThrows(IllegalArgumentException.class, () -> new EelContextImpl.Builder()
            .withTimeout(Duration.of(-6, ChronoUnit.HOURS)));
    }


    /**
     * Unit test {@link EelContextImpl#getMathContext()}
     */
    @Test
    public void test_withPrecision() {
        FunctionManager funcMan = mock(FunctionManager.class);

        try (
            MockedConstruction<FunctionManager.Builder> funcManConstructor =
                mockConstruction(FunctionManager.Builder.class, (c, s) -> when(c.build()).thenReturn(funcMan))
        ) {
            EelContextImpl context = (EelContextImpl) new EelContextImpl.Builder()
                .withPrecision(15)
                .build();

            Assert.assertEquals("Bad Context", new MathContext(15, RoundingMode.HALF_UP), context.getMathContext());
            Assert.assertTrue("Bad Id" + context.contextId(), context.contextId().matches("_id\\d+"));
            Assert.assertEquals("functionManager", funcMan, context.getFunctionManager());

            FunctionManager.Builder funcManBuilder = funcManConstructor.constructed().get(0);

            verify(funcManBuilder, never()).withUdfClass(any(Class.class));
            verify(funcManBuilder, never()).withUdfPackage(any(Package.class));
        }
    }


    /**
     * Unit test {@link EelContextImpl}
     */
    @Test
    public void test_addFunctions() {
        FunctionManager funcMan = mock(FunctionManager.class);

        try (
            MockedConstruction<FunctionManager.Builder> funcManConstructor =
                mockConstruction(FunctionManager.Builder.class, (c, s) -> when(c.build()).thenReturn(funcMan))
        ) {
            EelContextImpl context = (EelContextImpl) new EelContextImpl.Builder()
                .withUdfClass(SameValue.class)
                .withUdfClass(Half.class)
                .build();

            Assert.assertEquals("Bad Context", new MathContext(16, RoundingMode.HALF_UP), context.getMathContext());
            Assert.assertTrue("Bad Id" + context.contextId(), context.contextId().matches("_id\\d+"));
            Assert.assertEquals("functionManager", funcMan, context.getFunctionManager());

            FunctionManager.Builder funcManBuilder = funcManConstructor.constructed().get(0);

            verify(funcManBuilder, times(2)).withUdfClass(any(Class.class));
            verify(funcManBuilder, never()).withUdfPackage(any(Package.class));

            verify(funcManBuilder).withUdfClass(SameValue.class);
            verify(funcManBuilder).withUdfClass(Half.class);
        }
    }


    /**
     * Unit test {@link EelContextImpl}
     */
    @Test
    public void test_addPackage() {
        FunctionManager funcMan = mock(FunctionManager.class);

        try (
            MockedConstruction<FunctionManager.Builder> funcManConstructor =
                mockConstruction(FunctionManager.Builder.class, (c, s) -> when(c.build()).thenReturn(funcMan))
        ) {
            EelContextImpl context = (EelContextImpl) new EelContextImpl.Builder()
                .withUdfPackage(SameValue.class.getPackage())
                .withUdfPackage(Half.class.getPackage())
                .build();

            Assert.assertEquals("Bad Context", new MathContext(16, RoundingMode.HALF_UP), context.getMathContext());
            Assert.assertTrue("Bad Id" + context.contextId(), context.contextId().matches("_id\\d+"));
            Assert.assertEquals("functionManager", funcMan, context.getFunctionManager());

            FunctionManager.Builder funcManBuilder = funcManConstructor.constructed().get(0);

            verify(funcManBuilder, never()).withUdfClass(any(Class.class));
            verify(funcManBuilder, times(2)).withUdfPackage(any(Package.class));

            verify(funcManBuilder).withUdfPackage(SameValue.class.getPackage());
            verify(funcManBuilder).withUdfPackage(Sum.class.getPackage());
        }
    }

    /**
     * Unit test {@link EelContextImpl#getStartTime()}
     */
    @Test
    public void test_getStartTime() {
        ZonedDateTime now = ZonedDateTime.now(ZoneId.of("UTC"));
        EelContextImpl context = (EelContextImpl) new EelContextImpl.Builder()
            .build();

        ZonedDateTime actual = context.getStartTime();

        long difference = now.until(actual, ChronoUnit.MILLIS);

        Assert.assertTrue("Unexpected time", (difference < 1_000));
        Assert.assertEquals("Unexpected Zone", "UTC", actual.getZone().getId());
        Assert.assertSame("Time stamp should not change", actual, context.getStartTime());
    }
}