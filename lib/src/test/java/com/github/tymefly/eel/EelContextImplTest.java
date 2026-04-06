package com.github.tymefly.eel;

import java.io.File;
import java.io.IOException;
import java.math.MathContext;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.Duration;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.time.temporal.WeekFields;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

import func.functions.SameValue;
import func.functions.Sum;
import func.functions2.Half;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
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
    private BuildTime buildTime;

    @BeforeEach
    public void setUp() {
        buildTime = mock();

        when(buildTime.version())
            .thenReturn("mockedVersion");
        when(buildTime.buildDate())
            .thenReturn(EelContext.FALSE_DATE);
    }

    /**
     * Unit test {@link EelContextImpl}
     */
    @Test
    public void test_defaultContext() {
        FunctionManager funcMan = mock();

        try (
            MockedConstruction<FunctionManager.Builder> funcManConstructor =
                mockConstruction(FunctionManager.Builder.class, (c, s) -> when(c.build()).thenReturn(funcMan))
        ) {
            EelContextImpl context = new EelContextImpl.Builder()
                .build();

            assertEquals(new MathContext(16, RoundingMode.HALF_UP), context.getMathContext(), "Bad MathContext");
            assertTrue(context.contextId().matches("_id\\d+"), "Bad Id" + context.contextId());
            assertEquals(funcMan, context.getFunctionManager(), "functionManager");

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
        FunctionManager funcMan = mock();

        try (
            MockedConstruction<FunctionManager.Builder> funcManConstructor =
                mockConstruction(FunctionManager.Builder.class, (c, s) -> when(c.build()).thenReturn(funcMan))
        ) {
            EelContextImpl context = (EelContextImpl) new EelContextImpl.Builder()
                .withMaxExpressionSize(123)
                .build();

            assertEquals(123, context.maxExpressionLength(), "Bad Max Expression Length");
            assertTrue(context.contextId().matches("_id\\d+"), "Bad Id" + context.contextId());
            assertEquals(funcMan, context.getFunctionManager(), "functionManager");

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
        assertThrows(IllegalArgumentException.class, () -> new EelContextImpl.Builder()
            .withMaxExpressionSize(-123));
    }


    /**
     * Unit test {@link EelContext#getIoLimit()}
     */
    @Test
    public void test_withStartOfWeek() {
        assertEquals(32768, new EelContextImpl.Builder().build().getIoLimit(), "default");
        assertEquals(101, new EelContextImpl.Builder().withIoLimit(101).build().getIoLimit(), "custom");
    }


    /**
     * Unit test {@link EelContext#getWeek()}
     */
    @Test
    public void test_getWeek() {
        assertEquals(WeekFields.of(DayOfWeek.MONDAY, 4), new EelContextImpl.Builder().build().getWeek(), "default");
        assertEquals(WeekFields.of(DayOfWeek.THURSDAY, 4), new EelContextImpl.Builder().withStartOfWeek(DayOfWeek.THURSDAY).build().getWeek(), "custom");
    }


    /**
     * Unit test {@link EelContext#getFile(String)}
     */
    @Test
    public void test_getFile_String() {
        FileFactory customFileFactory = (f -> new File(f + ".2"));

        assertEquals("MyFile.txt.2",
            new EelContextImpl.Builder()
                .withFileFactory(customFileFactory)
                .build()
                .getFile("MyFile.txt")
                .getName(),
            "unexpected file");
    }

    /**
     * Unit test {@link EelContext#getFile(Value)}
     */
    @Test
    public void test_getFile_Value() {
        FileFactory customFileFactory = (f -> new File(f + ".2"));
        Value path = Value.of("MyFile.txt");

        assertEquals("MyFile.txt.2",
            new EelContextImpl.Builder()
                .withFileFactory(customFileFactory)
                .build()
                .getFile(path)
                .getName(),
            "unexpected file");
    }

    /**
     * Unit test {@link FunctionManager}
     */
    @Test
    public void test_getFile__fails() {
        IOException cause = new IOException("Expected");
        FileFactory customFileFactory = f -> { throw cause; };
        EelContext context = new EelContextImpl.Builder()
            .withFileFactory(customFileFactory)
            .build();
        Exception actual = assertThrows(RuntimeException.class, () -> context.getFile("MyFile.txt"));

        assertEquals("File Factory for 'MyFile.txt' failed", actual.getMessage(), "Unexpected message");
        assertSame(cause, actual.getCause(), "Unexpected cause");
    }


    /**
     * Unit test {@link EelContext#getWeek()}
     */
    @Test
    public void test_withMinimalDaysInFirstWeek() {
        assertEquals(WeekFields.of(DayOfWeek.MONDAY, 4), new EelContextImpl.Builder().build().getWeek(), "default");
        assertEquals(WeekFields.of(DayOfWeek.MONDAY, 1), new EelContextImpl.Builder().withMinimalDaysInFirstWeek(1).build().getWeek(), "custom");
    }


    /**
     * Unit test {@link EelContextImpl#getTimeout()}
     */
    @Test
    public void test_getTimeout() {
        FunctionManager funcMan = mock();

        try (
            MockedConstruction<FunctionManager.Builder> funcManConstructor =
                mockConstruction(FunctionManager.Builder.class, (c, s) -> when(c.build()).thenReturn(funcMan))
        ) {
            EelContextImpl context = (EelContextImpl) new EelContextImpl.Builder()
                .withTimeout(Duration.of(6, ChronoUnit.HOURS))
                .build();

            assertEquals(Duration.of(6, ChronoUnit.HOURS), context.getTimeout(), "Bad Max Expression Length");
            assertTrue(context.contextId().matches("_id\\d+"), "Bad Id" + context.contextId());
            assertEquals(funcMan, context.getFunctionManager(), "functionManager");

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
        assertThrows(IllegalArgumentException.class, () -> new EelContextImpl.Builder()
            .withTimeout(Duration.of(-6, ChronoUnit.HOURS)));
    }


    /**
     * Unit test {@link EelContextImpl#getMathContext()}
     */
    @Test
    public void test_withPrecision() {
        FunctionManager funcMan = mock();

        try (
            MockedConstruction<FunctionManager.Builder> funcManConstructor =
                mockConstruction(FunctionManager.Builder.class, (c, s) -> when(c.build()).thenReturn(funcMan))
        ) {
            EelContextImpl context = (EelContextImpl) new EelContextImpl.Builder()
                .withPrecision(15)
                .build();

            assertEquals(new MathContext(15, RoundingMode.HALF_UP), context.getMathContext(), "Bad Context");
            assertTrue(context.contextId().matches("_id\\d+"), "Bad Id" + context.contextId());
            assertEquals(funcMan, context.getFunctionManager(), "functionManager");

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
        FunctionManager funcMan = mock();

        try (
            MockedConstruction<FunctionManager.Builder> funcManConstructor =
                mockConstruction(FunctionManager.Builder.class, (c, s) -> when(c.build()).thenReturn(funcMan))
        ) {
            EelContextImpl context = (EelContextImpl) new EelContextImpl.Builder()
                .withUdfClass(SameValue.class)
                .withUdfClass(Half.class)
                .build();

            assertEquals(new MathContext(16, RoundingMode.HALF_UP), context.getMathContext(), "Bad Context");
            assertTrue(context.contextId().matches("_id\\d+"), "Bad Id" + context.contextId());
            assertEquals(funcMan, context.getFunctionManager(), "functionManager");

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
        FunctionManager funcMan = mock();

        try (
            MockedConstruction<FunctionManager.Builder> funcManConstructor =
                mockConstruction(FunctionManager.Builder.class, (c, s) -> when(c.build()).thenReturn(funcMan))
        ) {
            EelContextImpl context = (EelContextImpl) new EelContextImpl.Builder()
                .withUdfPackage(SameValue.class.getPackage())
                .withUdfPackage(Half.class.getPackage())
                .build();

            assertEquals(new MathContext(16, RoundingMode.HALF_UP), context.getMathContext(), "Bad Context");
            assertTrue(context.contextId().matches("_id\\d+"), "Bad Id" + context.contextId());
            assertEquals(funcMan, context.getFunctionManager(), "functionManager");

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
        EelContextImpl context = new EelContextImpl.Builder()
            .build();

        ZonedDateTime actual = context.getStartTime();

        long difference = now.until(actual, ChronoUnit.MILLIS);

        assertTrue((difference < 1_000), "Unexpected time");
        assertEquals("UTC", actual.getZone().getId(), "Unexpected Zone");
        assertSame(actual, context.getStartTime(), "Time stamp should not change");
    }


    /**
     * Unit test {@link EelContextImpl#metadata()}
     */
    @Test
    public void test_eelInfo() {
        try (
            MockedStatic<BuildTime> buildTime = Mockito.mockStatic(BuildTime.class)
        ) {
            buildTime.when(BuildTime::getInstance)
                .thenReturn(this.buildTime);

            Metadata actual = new EelContextImpl.Builder()
                .build()
                .metadata();

            assertEquals("mockedVersion", actual.version(), "Unexpected information");
        }
    }


    /**
     * Unit test {@link EelContextImpl#getResource(Class, String, Function)} 
     */
    @Test
    public void test_getResource() {
        Class<?> owner1 = getClass();
        Class<?> owner2 = EelContextImpl.class;
        AtomicInteger count = new AtomicInteger();
        Function<String, String> constructor = n -> "Item" + count.incrementAndGet() + " (" + n + ")";
        EelContextImpl context = new EelContextImpl.Builder().build();

        String resource1 = context.getResource(owner1, "Name1", constructor);
        String resource2 = context.getResource(owner1, "Name1", constructor);
        String resource3 = context.getResource(owner1, "Name2", constructor);
        String resource4 = context.getResource(owner2, "Name1", constructor);
        String resource5 = context.getResource(owner2, "Name2", constructor);
        String resource6 = context.getResource(owner2, "Name2", constructor);

        assertEquals("Item1 (Name1)", resource1, "Unexpected resource1");
        assertSame(resource1, resource2, "Unexpected resource2");
        assertEquals("Item2 (Name2)", resource3, "Unexpected resource3");
        assertEquals("Item3 (Name1)", resource4, "Unexpected resource4");
        assertEquals("Item4 (Name2)", resource5, "Unexpected resource5");
        assertSame(resource5, resource6, "Unexpected resource6");
    }
}