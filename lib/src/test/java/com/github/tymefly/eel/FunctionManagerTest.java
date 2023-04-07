package com.github.tymefly.eel;

import java.math.MathContext;
import java.math.RoundingMode;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.List;

import com.github.tymefly.eel.exception.EelArithmeticException;
import com.github.tymefly.eel.exception.EelFunctionException;
import func.bad_functions.Test1;
import func.bad_functions.Test10;
import func.bad_functions.Test11;
import func.bad_functions.Test12;
import func.bad_functions.Test13;
import func.bad_functions.Test14;
import func.bad_functions.Test15;
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
import func.functions.Tomorrow;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.SystemErrRule;
import org.junit.contrib.java.lang.system.SystemOutRule;

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
    private EelContext context;

    @Before
    public void setUp() {
        symbolsTable = mock(SymbolsTable.class);
        context = mock(EelContext.class);

        MathContext mathContext = new MathContext(2, RoundingMode.HALF_UP);

        when(context.getMathContext())
            .thenReturn(mathContext);

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
                .compileCall("test3", context, Collections.emptyList()).execute(symbolsTable));

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
                .compileCall("test.5", context, Collections.emptyList()).execute(symbolsTable));

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
                .compileCall("test.10", context, Collections.emptyList()).execute(symbolsTable));

        // Error message should not be wrapped
        Assert.assertEquals("Unexpected message",
            "This was thrown in side a Function",
            actual.getMessage());
    }

    /**
     * Unit test {@link FunctionManager}
     */
    @Test
    public void test_EelArithmeticException() {
        EelFunctionException actual = Assert.assertThrows(EelArithmeticException.class,
            () -> new FunctionManager.Builder()
                .build()
                .compileCall("asin", context, List.of(s -> Value.of(12))).execute(symbolsTable));

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
                .compileCall("test.6", context, Collections.emptyList()).execute(symbolsTable));

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
                .compileCall("Test7", context, Collections.emptyList()).execute(symbolsTable));

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
                .compileCall("test.8", context, List.of(r -> Value.TRUE)).execute(symbolsTable));

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
                .compileCall("test.9", context, List.of()).execute(symbolsTable));

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
    public void test_MissingArguments() {
        EelFunctionException actual = Assert.assertThrows(EelFunctionException.class,
            () -> new FunctionManager.Builder()
                .withUdfClass(Plus1.class)
                .build()
                .compileCall("test.plus1", context, List.of()).execute(symbolsTable));

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
                .compileCall("test.plus1", context, List.of(r -> Value.of(1), r -> Value.of(2))).execute(symbolsTable));

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
                .compileCall("test.plus1", context, List.of(r -> Value.of(3))).execute(symbolsTable);

        Assert.assertEquals("Unexpected value returned", Value.of(4), actual);
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

        Assert.assertEquals("Unexpected value returned", Value.TRUE, actual);
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
            r -> Value.of(ZonedDateTime.of(2022, 1, 2, 3, 4, 5, 6_000_000, ZoneOffset.UTC))
        );

        Value actual = new FunctionManager.Builder()
            .withUdfClass(Defaults.class)
            .build()
            .compileCall("test.defaults", context, argumentList)
            .execute(symbolsTable);

        Assert.assertEquals("Unexpected value", Value.of("Passed 'myText', -123, true, 'late' ~ 2022-01-02T03:04:05Z"), actual);
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

        Assert.assertEquals("Unexpected value", Value.of("Passed 'requiredText', 987, false, '???' ~ 2001-02-03T04:05Z"), actual);
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