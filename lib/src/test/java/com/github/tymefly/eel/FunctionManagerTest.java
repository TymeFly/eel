package com.github.tymefly.eel;

import java.io.IOException;
import java.math.MathContext;
import java.math.RoundingMode;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

import com.github.tymefly.eel.exception.EelRuntimeException;
import func.bad_functions.Test1;
import func.bad_functions.Test10;
import func.bad_functions.Test11;
import func.bad_functions.Test12;
import func.bad_functions.Test13;
import func.bad_functions.Test14;
import func.bad_functions.Test15;
import func.bad_functions.Test16;
import func.bad_functions.Test2;
import func.bad_functions.Test3;
import func.bad_functions.Test4;
import func.bad_functions.Test5;
import func.bad_functions.Test6;
import func.bad_functions.Test7;
import func.bad_functions.Test8;
import func.bad_functions.Test9;
import func.functions.Defaults;
import func.functions.Plus1;
import func.functions.SameValue;
import func.functions.Sum;
import func.functions.TestTypes;
import func.functions.Tomorrow;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.SystemErrRule;
import org.junit.contrib.java.lang.system.SystemOutRule;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Unit test for {@link FunctionManager}
 */
public class FunctionManagerTest {
    @Rule
    public SystemOutRule stdOut = new SystemOutRule().enableLog().muteForSuccessfulTests();

    @Rule
    public SystemErrRule stdErr = new SystemErrRule().enableLog().muteForSuccessfulTests();


    private SymbolsTable symbolsTable;
    private EelContextImpl context;

    @Before
    public void setUp() {
        symbolsTable = mock(SymbolsTable.class);
        context = mock(EelContextImpl.class);

        MathContext mathContext = new MathContext(2, RoundingMode.HALF_UP);

        when(context.contextId())
            .thenReturn("myContext!!");
        when(context.getMathContext())
            .thenReturn(mathContext);
        when(context.getResource(any(Class.class), anyString(), any(Function.class)))
            .thenAnswer(a -> ((Function<String, ?>) a.getArguments()[2]).apply(""));

        when(symbolsTable.read("myVar"))
            .thenReturn("late");
    }

    /**
     * Unit test {@link FunctionManager}
     */
    @Test
    public void test_UnknownFunction() {
        EelFunctionException actual = Assert.assertThrows(EelFunctionException.class,
            () -> new FunctionManager.Builder().build()
                .compileCall("Unknown", context, Collections.emptyList()));

        Assert.assertEquals("Unexpected message", "Undefined function 'Unknown'", actual.getMessage());
    }


    /**
     * Unit test {@link FunctionManager}
     */
    @Test
    public void test_NoConstructor() {
        EelFunctionException actual = Assert.assertThrows(EelFunctionException.class,
            () -> new FunctionManager.Builder()
                .withUdfClass(Test1.class)
                .build()
                .compileCall("test.1", context, Collections.emptyList()));

        Assert.assertEquals("Unexpected message",
            "Failed to execute default constructor for 'func.bad_functions.Test1'",
            actual.getMessage());
    }

    /**
     * Unit test {@link FunctionManager}
     */
    @Test
    public void test_BadConstructor() {
        EelFunctionException actual = Assert.assertThrows(EelFunctionException.class,
            () -> new FunctionManager.Builder()
                .withUdfClass(Test2.class)
                .build()
                .compileCall("test.2", context, Collections.emptyList()));

        Assert.assertEquals("Unexpected message",
            "Failed to execute default constructor for 'func.bad_functions.Test2'",
            actual.getMessage());
    }

    /**
     * Unit test {@link FunctionManager}
     */
    @Test
    public void test_NoEntryPoints() {
        EelFunctionException actual = Assert.assertThrows(EelFunctionException.class,
            () -> new FunctionManager.Builder()
                .withUdfClass(Test3.class)
                .build()
                .compileCall("test3", context, Collections.emptyList())
                .execute(symbolsTable));

        Assert.assertEquals("Unexpected message",
            "Invalid function class func.bad_functions.Test3",
            actual.getMessage());

        Assert.assertTrue("Expected Error to be logged",
                stdOut.getLog().contains("Function 'Test3' contains no EEL functions"));
    }

    /**
     * Unit test {@link FunctionManager}
     */
    @Test
    public void test_FailedToExecute() {
        EelFunctionException actual = Assert.assertThrows(EelFunctionException.class,
            () -> new FunctionManager.Builder()
                .withUdfClass(Test5.class)
                .build()
                .compileCall("test.5", context, Collections.emptyList())
                .execute(symbolsTable));

        Assert.assertEquals("Unexpected message",
            "Failed to execute function 'test.5'",
            actual.getMessage());
    }

    /**
     * Unit test {@link FunctionManager}
     */
    @Test
    public void test_EelException() {
        EelFunctionException actual = Assert.assertThrows(EelFunctionException.class,
            () -> new FunctionManager.Builder()
                .withUdfClass(Test10.class)
                .build()
                .compileCall("test.10", context, Collections.emptyList())
                .execute(symbolsTable));

        // Error message should not be wrapped
        Assert.assertEquals("Unexpected message",
            "Failed to execute function 'test.10'",
            actual.getMessage());

        Assert.assertEquals("Unexpected cause type", RuntimeException.class, actual.getCause().getClass());
        Assert.assertEquals("Unexpected cause",
            "This was thrown inside a Function",
            actual.getCause().getMessage());
    }

    /**
     * Unit test {@link FunctionManager}
     */
    @Test
    public void test_EelArithmeticException() {
        EelRuntimeException actual = Assert.assertThrows(EelFunctionException.class,
            () -> new FunctionManager.Builder()
                .build()
                .compileCall("asin", context, List.of(s -> Value.of(12)))
                .execute(symbolsTable));

        // Error message should not be wrapped
        Assert.assertEquals("Unexpected message",
            "Failed to execute function 'asin'",
            actual.getMessage());
    }

    /**
     * Unit test {@link FunctionManager}
     */
    @Test
    public void test_UnexpectedReturnType() {
        EelFunctionException actual = Assert.assertThrows(EelFunctionException.class,
            () -> new FunctionManager.Builder()
                .withUdfClass(Test6.class)
                .build()
                .compileCall("test.6", context, Collections.emptyList())
                .execute(symbolsTable));

        Assert.assertEquals("Unexpected message",
            "Function 'test.6' returned unexpected type 'java.lang.Thread'",
            actual.getMessage());
    }

    /**
     * Unit test {@link FunctionManager}
     */
    @Test
    public void test_NonPublicEntryPoint() {
        EelFunctionException actual = Assert.assertThrows(EelFunctionException.class,
            () -> new FunctionManager.Builder()
                .withUdfClass(Test7.class)
                .build()
                .compileCall("Test7", context, Collections.emptyList())
                .execute(symbolsTable));

        Assert.assertEquals("Unexpected message",
            "Invalid function class func.bad_functions.Test7",
            actual.getMessage());

        Assert.assertTrue("Expected Error to be logged",
                stdOut.getLog().contains("Method 'func.bad_functions.Test7.test7' is not public"));
    }

    /**
     * Unit test {@link FunctionManager}
     */
    @Test
    public void test_UnexpectedActualArgumentType() {
        EelFunctionException actual = Assert.assertThrows(EelFunctionException.class,
            () -> new FunctionManager.Builder()
                .withUdfClass(Test8.class)
                .build()
                .compileCall("test.8", context, List.of(r -> Value.of(true)))
                .execute(symbolsTable));

        Assert.assertEquals("Unexpected message",
            "Argument 0 for function 'test.8' is of unsupported type java.lang.Thread",
            actual.getMessage());
    }

    /**
     * Unit test {@link FunctionManager}
     */
    @Test
    public void test_NullReturned() {
        EelFunctionException actual = Assert.assertThrows(EelFunctionException.class,
            () -> new FunctionManager.Builder()
                .withUdfClass(Test9.class)
                .build()
                .compileCall("test.9", context, List.of())
                .execute(symbolsTable));

        Assert.assertEquals("Unexpected message",
            "Function 'test.9' returned null",
            actual.getMessage());
    }

    /**
     * Unit test {@link FunctionManager}
     */
    @Test
    public void test_NoPrefix() {
        EelFunctionException actual = Assert.assertThrows(EelFunctionException.class,
            () -> new FunctionManager.Builder()
                .withUdfClass(Test11.class));

        Assert.assertEquals("Unexpected message",
            "Invalid UDF name 'noPrefix'",
            actual.getMessage());
    }

    /**
     * Unit test {@link FunctionManager}
     */
    @Test
    public void test_ReservedPrefix() {
        EelFunctionException actual = Assert.assertThrows(EelFunctionException.class,
            () -> new FunctionManager.Builder()
                .withUdfClass(Test12.class));

        Assert.assertEquals("Unexpected message",
            "Invalid UDF name 'eel.test12'",
            actual.getMessage());
    }

    /**
     * Unit test {@link FunctionManager}
     */
    @Test
    public void test_badName() {
        EelFunctionException actual = Assert.assertThrows(EelFunctionException.class,
            () -> new FunctionManager.Builder()
                .withUdfClass(Test13.class));

        Assert.assertEquals("Unexpected message",
            "Invalid UDF name '123.badName'",
            actual.getMessage());
    }

    /**
     * Unit test {@link FunctionManager}
     */
    @Test
    public void test_consecutiveDots() {
        EelFunctionException actual = Assert.assertThrows(EelFunctionException.class,
            () -> new FunctionManager.Builder()
                .withUdfClass(Test14.class));

        Assert.assertEquals("Unexpected message",
            "Invalid UDF name 'test..14'",
            actual.getMessage());
    }

    /**
     * Unit test {@link FunctionManager}
     */
    @Test
    public void test_TrailingDot() {
        EelFunctionException actual = Assert.assertThrows(EelFunctionException.class,
            () -> new FunctionManager.Builder()
                .withUdfClass(Test15.class));

        Assert.assertEquals("Unexpected message",
            "Invalid UDF name 'test.15.'",
            actual.getMessage());
    }

    /**
     * Unit test {@link FunctionManager}
     */
    @Test
    public void test_CheckedException() {
        EelFunctionException actual = Assert.assertThrows(EelFunctionException.class,
            () -> new FunctionManager.Builder()
                .withUdfClass(Test16.class)
                .build()
                .compileCall("test.16", context, List.of())
                .execute(symbolsTable));
        Throwable cause = actual.getCause();

        Assert.assertEquals("Unexpected message",
            "Failed to execute function 'test.16'",
            actual.getMessage());
        Assert.assertEquals("Unexpected cause type", IOException.class, cause.getClass());
        Assert.assertEquals("Unexpected cause message",
            "This message was thrown by test.16",
            cause.getMessage());
    }

    /**
     * Unit test {@link FunctionManager}
     */
    @Test
    public void test_MissingArguments() {
        EelFunctionException actual = Assert.assertThrows(EelFunctionException.class,
            () -> new FunctionManager.Builder()
                .withUdfClass(Plus1.class)
                .build()
                .compileCall("test.plus1", context, List.of())
                .execute(symbolsTable));

        Assert.assertEquals("Unexpected message",
            "Argument 0 for function 'test.plus1' is missing and no default exists",
            actual.getMessage());
    }


    /**
     * Unit test {@link FunctionManager}
     */
    @Test
    public void test_TooManyArguments() {
        EelFunctionException actual = Assert.assertThrows(EelFunctionException.class,
            () -> new FunctionManager.Builder()
                .withUdfClass(Plus1.class)
                .build()
                .compileCall("test.plus1", context, List.of(r -> Value.of(1), r -> Value.of(2)))
                .execute(symbolsTable));

        Assert.assertEquals("Unexpected message",
            "Expected 1 argument(s) for function 'test.plus1' but 2 were passed",
            actual.getMessage());
    }

    /**
     * Unit test {@link FunctionManager}
     */
    @Test
    public void test_HappyPath() {
        Value actual = new FunctionManager.Builder()
            .withUdfClass(Plus1.class)
            .build()
            .compileCall("test.plus1", context, List.of(r -> Value.of(3)))
            .execute(symbolsTable);

        Assert.assertEquals("Unexpected value returned", Value.of(4), actual);
    }

    /**
     * Unit test {@link FunctionManager}
     */
    @Test
    public void test_Types() {
        FunctionManager manager = new FunctionManager.Builder()
            .withUdfClass(TestTypes.class)
            .build();

        Assert.assertEquals("pass EelContext",
            Value.of("myContext!!"),
            manager.compileCall("types.context", context, List.of()).execute(symbolsTable));

        Assert.assertEquals("pass FunctionalResource",
            Value.of("resourceValue!!"),
            manager.compileCall("types.functionalResource", context, List.of()).execute(symbolsTable));


        Assert.assertEquals("pass Value",
            Value.of(3),
            manager.compileCall("types.value", context, List.of(r -> Value.of(3))).execute(symbolsTable));

        Assert.assertEquals("pass String",
            Value.of("Hello"),
            manager.compileCall("types.str", context, List.of(r -> Value.of("Hello"))).execute(symbolsTable));

        Assert.assertEquals("pass Boolean",
            EelValue.TRUE,
            manager.compileCall("types.Bool", context, List.of(r -> Value.of(Boolean.TRUE))).execute(symbolsTable));
        Assert.assertEquals("pass boolean",
            EelValue.FALSE,
            manager.compileCall("types.bool", context, List.of(r -> Value.of(false))).execute(symbolsTable));

        Assert.assertEquals("pass Byte",
            Value.of(12),
            manager.compileCall("types.Byte", context, List.of(r -> Value.of((byte) 12))).execute(symbolsTable));
        Assert.assertEquals("pass byte",
            Value.of(93),
            manager.compileCall("types.byte", context, List.of(r -> Value.of((byte) 93))).execute(symbolsTable));

        Assert.assertEquals("pass Short",
            Value.of(123),
            manager.compileCall("types.Short", context, List.of(r -> Value.of((short) 123))).execute(symbolsTable));
        Assert.assertEquals("pass short",
            Value.of(345),
            manager.compileCall("types.short", context, List.of(r -> Value.of((short) 345))).execute(symbolsTable));

        Assert.assertEquals("pass Int",
            Value.of(1234),
            manager.compileCall("types.Int", context, List.of(r -> Value.of(1234))).execute(symbolsTable));
        Assert.assertEquals("pass int",
            Value.of(3456),
            manager.compileCall("types.int", context, List.of(r -> Value.of(3456))).execute(symbolsTable));

        Assert.assertEquals("pass Long",
            Value.of(12345),
            manager.compileCall("types.Long", context, List.of(r -> Value.of((long) 12345))).execute(symbolsTable));
        Assert.assertEquals("pass long",
            Value.of(34567),
            manager.compileCall("types.long", context, List.of(r -> Value.of((long) 34567))).execute(symbolsTable));

        Assert.assertEquals("pass Float",
            Value.of(123.45),
            manager.compileCall("types.Float", context, List.of(r -> Value.of(123.45f))).execute(symbolsTable));
        Assert.assertEquals("pass float",
            Value.of(345.67),
            manager.compileCall("types.float", context, List.of(r -> Value.of(345.67f))).execute(symbolsTable));

        Assert.assertEquals("pass Double",
            Value.of(123.456),
            manager.compileCall("types.Double", context, List.of(r -> Value.of(123.456))).execute(symbolsTable));
        Assert.assertEquals("pass double",
            Value.of(345.678),
            manager.compileCall("types.double", context, List.of(r -> Value.of(345.678))).execute(symbolsTable));

        Assert.assertEquals("pass BigInt",
            Value.of(12345678),
            manager.compileCall("types.BigInt", context, List.of(r -> Value.of(12345678))).execute(symbolsTable));
        Assert.assertEquals("pass BigDec",
            Value.of(1234.5678),
            manager.compileCall("types.BigDec", context, List.of(r -> Value.of(1234.5678))).execute(symbolsTable));
        Assert.assertEquals("pass date",
            Value.of(EelContext.FALSE_DATE),
            manager.compileCall("types.date", context, List.of(r -> Value.of(EelContext.FALSE_DATE))).execute(symbolsTable));

        Assert.assertEquals("pass char",
            Value.of("H"),
            manager.compileCall("types.char", context, List.of(r -> Value.of("Hello"))).execute(symbolsTable));
        Assert.assertEquals("pass Character",
            Value.of("W"),
            manager.compileCall("types.Character", context, List.of(r -> Value.of("World"))).execute(symbolsTable));

        Assert.assertEquals("pass Lambda",
            Value.of("Callback!!"),
            manager.compileCall("types.Lambda", context, List.of(r -> Value.of("Callback"))).execute(symbolsTable));
    }

    /**
     * Unit test {@link FunctionManager}
     */
    @Test
    public void test_HappyPath_MultipleEntryPoints() {
        FunctionManager functionManager = new FunctionManager.Builder()
            .withUdfClass(Test4.class)
            .build();

        Value actual_1 = functionManager.compileCall("test.4_1", context, List.of()).execute(symbolsTable);
        Value actual_2 = functionManager.compileCall("test.4_2", context, List.of()).execute(symbolsTable);

        Assert.assertEquals("Unexpected value returned from function 1", Value.of("execute1"), actual_1);
        Assert.assertEquals("Unexpected value returned from function 1", Value.of("execute2"), actual_2);
    }


    /**
     * Unit test {@link FunctionManager}
     */
    @Test
    public void test_ConvertNumerics() {
        Value actual = new FunctionManager.Builder()
                .withUdfClass(SameValue.class)
                .build()
                .compileCall("test.sameValue",
                    context,
                    List.of(r -> Value.of(1), r -> Value.of(1), r -> Value.of(1), r -> Value.of(1), r -> Value.of(1)))
            .execute(symbolsTable);

        Assert.assertEquals("Unexpected value returned", EelValue.TRUE, actual);
    }


    /**
     * Unit test {@link FunctionManager}
     */
    @Test
    public void test_SuppliedDefaults() {
        List<Executor> argumentList = List.of(
            r -> Value.of("myText"),
            r -> Value.of(-123),
            r -> Value.of(true),
            r -> Value.of(r.read("myVar")),
            r -> Value.of(ZonedDateTime.of(2022, 1, 2, 3, 4, 5, 6_000_000, ZoneOffset.UTC)),
            r -> Value.of("myFunction")
        );

        Value actual = new FunctionManager.Builder()
            .withUdfClass(Defaults.class)
            .build()
            .compileCall("test.defaults", context, argumentList)
            .execute(symbolsTable);

        Assert.assertEquals("Unexpected value",
            Value.of("Passed 'myText', -123, true, 'late' ~ 2022-01-02T03:04:05Z, myFunction"),
            actual);
    }

    /**
     * Unit test {@link FunctionManager}
     */
    @Test
    public void test_UseDefaults() {
        List<Executor> argumentList = List.of(r -> Value.of("requiredText"));

        Value actual = new FunctionManager.Builder()
            .withUdfClass(Defaults.class)
            .build()
            .compileCall("test.defaults", context, argumentList)
            .execute(symbolsTable);

        Assert.assertEquals("Unexpected value",
            Value.of("Passed 'requiredText', 987, false, '???' ~ 2001-02-03T04:05Z, ???"),
            actual);
    }

    /**
     * Unit test {@link FunctionManager}
     */
    @Test
    public void test_VarArgs() {
        List<Executor> argumentList = List.of(r -> Value.of(1),
            r -> Value.of(3),
            r -> Value.of(5),
            r -> Value.of(11),
            r -> Value.of(23));

        Value actual = new FunctionManager.Builder()
            .withUdfClass(Sum.class)
            .build()
            .compileCall("test.sum", context, argumentList)
            .execute(symbolsTable);

        Assert.assertEquals("Unexpected value", Value.of(43), actual);
    }

    /**
     * Unit test {@link FunctionManager}
     */
    @Test
    public void test_Dates() {
        Value date = Value.of(ZonedDateTime.of(2022, 1, 2, 3, 4, 5, 6, ZoneOffset.UTC));
        List<Executor> argumentList = List.of(r -> date);

        Value actual = new FunctionManager.Builder()
            .withUdfClass(Tomorrow.class)
            .build()
            .compileCall("test.tomorrow", context, argumentList)
            .execute(symbolsTable);

        Assert.assertEquals("Unexpected value",
            Value.of(ZonedDateTime.of(2022, 1, 3, 3, 4, 5, 6, ZoneOffset.UTC)),
            actual);
    }
}