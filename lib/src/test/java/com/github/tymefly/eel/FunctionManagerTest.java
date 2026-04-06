package com.github.tymefly.eel;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.stream.IntStream;

import com.github.tymefly.eel.exception.EelRuntimeException;
import com.github.tymefly.eel.exception.EelUnknownFunctionException;
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
import func.functions2.Half;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import uk.org.webcompere.systemstubs.jupiter.SystemStub;
import uk.org.webcompere.systemstubs.jupiter.SystemStubsExtension;
import uk.org.webcompere.systemstubs.stream.SystemErr;
import uk.org.webcompere.systemstubs.stream.SystemOut;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit test for {@link FunctionManager}
 */
@ExtendWith(SystemStubsExtension.class)
public class FunctionManagerTest {
    @SystemStub
    private SystemOut stdOut;

    @SystemStub
    private SystemErr stdErr;


    private SymbolsTable symbolsTable;
    private EelContextImpl context;

    @BeforeEach
    public void setUp() {
        symbolsTable = mock();
        context = mock();

        MathContext mathContext = new MathContext(2, RoundingMode.HALF_UP);

        when(context.contextId())
            .thenReturn("myContext!!");
        when(context.getMathContext())
            .thenReturn(mathContext);
        when(context.getResource(any(Class.class), anyString(), any(Function.class)))
            .thenAnswer(i -> i.getArgument(2, Function.class).apply(""));
        when(context.getFile(anyString()))
            .thenAnswer(i -> new File(i.getArgument(0, String.class)));

        when(symbolsTable.read("myVar"))
            .thenReturn("late");
    }


    /**
     * Unit test {@link FunctionManager}
     */
    @Test
    public void test_UnknownFunction() {
        EelUnknownFunctionException actual = assertThrows(EelUnknownFunctionException.class,
            () -> new FunctionManager.Builder().build()
                .compileCall(context, "Unknown", Collections.emptyList()));

        assertEquals("Undefined function 'Unknown'", actual.getMessage(), "Unexpected message");
    }

    /**
     * Unit test {@link FunctionManager}
     */
    @Test
    public void test_addPackage() {
        Package udfPackage = Half.class.getPackage();

        // Function "test.one" is not in the same class as Half, but it's in the same package
        Value parent = new FunctionManager.Builder()
            .withUdfPackage(udfPackage)
            .build()
            .compileCall(context, "test.one", Collections.emptyList())
            .evaluate(symbolsTable);

        assertEquals(Value.of(1), parent, "Unexpected value returned");

        // Function "test.twp" is not in the same package as Half - it's in a child package
        EelUnknownFunctionException child = assertThrows(EelUnknownFunctionException.class,
            () -> new FunctionManager.Builder()
                    .withUdfPackage(udfPackage)
                    .build()
                    .compileCall(context, "test.two", Collections.emptyList())
            );

        assertEquals("Undefined function 'test.two'", child.getMessage(), "Unexpected message");
    }


    /**
     * Unit test {@link FunctionManager}
     */
    @Test
    public void test_NoConstructor() {
        EelFunctionException actual = assertThrows(EelFunctionException.class,
            () -> new FunctionManager.Builder()
                .withUdfClass(Test1.class)
                .build()
                .compileCall(context, "test.1", Collections.emptyList()));

        assertEquals("Failed to execute default constructor for 'func.bad_functions.Test1'",
            actual.getMessage(),
            "Unexpected message");
    }

    /**
     * Unit test {@link FunctionManager}
     */
    @Test
    public void test_BadConstructor() {
        EelFunctionException actual = assertThrows(EelFunctionException.class,
            () -> new FunctionManager.Builder()
                .withUdfClass(Test2.class)
                .build()
                .compileCall(context, "test.2", Collections.emptyList()));

        assertEquals("Failed to execute default constructor for 'func.bad_functions.Test2'",
            actual.getMessage(),
            "Unexpected message");
    }

    /**
     * Unit test {@link FunctionManager}
     */
    @Test
    public void test_NoEntryPoints() {
        EelFunctionException actual = assertThrows(EelFunctionException.class,
            () -> new FunctionManager.Builder()
                .withUdfClass(Test3.class)
                .build()
                .compileCall(context, "test3", Collections.emptyList())
                .evaluate(symbolsTable));

        assertEquals("Invalid function class func.bad_functions.Test3", actual.getMessage(), "Unexpected message");

        assertTrue(stdOut.getLinesNormalized().contains("Function 'Test3' contains no EEL functions"),
            "Expected Error to be logged");
    }

    /**
     * Unit test {@link FunctionManager}
     */
    @Test
    public void test_FailedToExecute() {
        EelFunctionException actual = assertThrows(EelFunctionException.class,
            () -> new FunctionManager.Builder()
                .withUdfClass(Test5.class)
                .build()
                .compileCall(context, "test.5", Collections.emptyList())
                .evaluate(symbolsTable));

        assertEquals("Failed to execute function 'test.5'", actual.getMessage(), "Unexpected message");
    }

    /**
     * Unit test {@link FunctionManager}
     */
    @Test
    public void test_EelException() {
        EelFunctionException actual = assertThrows(EelFunctionException.class,
            () -> new FunctionManager.Builder()
                .withUdfClass(Test10.class)
                .build()
                .compileCall(context, "test.10", Collections.emptyList())
                .evaluate(symbolsTable));

        // Error message should not be wrapped
        assertEquals("Failed to execute function 'test.10'", actual.getMessage(), "Unexpected message");

        assertEquals(RuntimeException.class, actual.getCause().getClass(), "Unexpected cause type");
        assertEquals("This was thrown inside a Function", actual.getCause().getMessage(), "Unexpected cause");
    }

    /**
     * Unit test {@link FunctionManager}
     */
    @Test
    public void test_EelArithmeticException() {
        EelRuntimeException actual = assertThrows(EelFunctionException.class,
            () -> new FunctionManager.Builder()
                .build()
                .compileCall(context, "asin", List.of(Constant.of(12)))
                .evaluate(symbolsTable));

        // Error message should not be wrapped
        assertEquals("Failed to execute function 'asin'", actual.getMessage(), "Unexpected message");
    }

    /**
     * Unit test {@link FunctionManager}
     */
    @Test
    public void test_UnexpectedReturnType() {
        EelFunctionException actual = assertThrows(EelFunctionException.class,
            () -> new FunctionManager.Builder()
                .withUdfClass(Test6.class)
                .build()
                .compileCall(context, "test.6", Collections.emptyList())
                .evaluate(symbolsTable));

        assertEquals("Function 'test.6' returned unexpected type 'java.lang.Thread'",
            actual.getMessage(),
            "Unexpected message");
    }

    /**
     * Unit test {@link FunctionManager}
     */
    @Test
    public void test_NonPublicEntryPoint() {
        EelFunctionException actual = assertThrows(EelFunctionException.class,
            () -> new FunctionManager.Builder()
                .withUdfClass(Test7.class)
                .build()
                .compileCall(context, "Test7", Collections.emptyList())
                .evaluate(symbolsTable));

        assertEquals("Invalid function class func.bad_functions.Test7", actual.getMessage(), "Unexpected message");

        assertTrue(stdOut.getLinesNormalized().contains("Method 'func.bad_functions.Test7.test7' is not public"),
            "Expected Error to be logged");
    }

    /**
     * Unit test {@link FunctionManager}
     */
    @Test
    public void test_UnexpectedActualArgumentType() {
        EelFunctionException actual = assertThrows(EelFunctionException.class,
            () -> new FunctionManager.Builder()
                .withUdfClass(Test8.class)
                .build()
                .compileCall(context, "test.8", List.of(Constant.of(true)))
                .evaluate(symbolsTable));

        assertEquals("Argument 0 for function 'test.8' is of unsupported type java.lang.Thread",
            actual.getMessage(),
            "Unexpected message");
    }

    /**
     * Unit test {@link FunctionManager}
     */
    @Test
    public void test_NullReturned() {
        EelFunctionException actual = assertThrows(EelFunctionException.class,
            () -> new FunctionManager.Builder()
                .withUdfClass(Test9.class)
                .build()
                .compileCall(context, "test.9", List.of())
                .evaluate(symbolsTable));

        assertEquals("Function 'test.9' returned null", actual.getMessage(), "Unexpected message");
    }

    /**
     * Unit test {@link FunctionManager}
     */
    @Test
    public void test_NoPrefix() {
        EelFunctionException actual = assertThrows(EelFunctionException.class,
            () -> new FunctionManager.Builder()
                .withUdfClass(Test11.class));

        assertEquals("Invalid UDF name 'noPrefix'", actual.getMessage(), "Unexpected message");
    }

    /**
     * Unit test {@link FunctionManager}
     */
    @Test
    public void test_ReservedPrefix() {
        EelFunctionException actual = assertThrows(EelFunctionException.class,
            () -> new FunctionManager.Builder()
                .withUdfClass(Test12.class));

        assertEquals("Invalid UDF name 'eel.test12'", actual.getMessage(), "Unexpected message");
    }

    /**
     * Unit test {@link FunctionManager}
     */
    @Test
    public void test_badName() {
        EelFunctionException actual = assertThrows(EelFunctionException.class,
            () -> new FunctionManager.Builder()
                .withUdfClass(Test13.class));

        assertEquals("Invalid UDF name '123.badName'", actual.getMessage(), "Unexpected message");
    }

    /**
     * Unit test {@link FunctionManager}
     */
    @Test
    public void test_consecutiveDots() {
        EelFunctionException actual = assertThrows(EelFunctionException.class,
            () -> new FunctionManager.Builder()
                .withUdfClass(Test14.class));

        assertEquals("Invalid UDF name 'test..14'", actual.getMessage(), "Unexpected message");
    }

    /**
     * Unit test {@link FunctionManager}
     */
    @Test
    public void test_TrailingDot() {
        EelFunctionException actual = assertThrows(EelFunctionException.class,
            () -> new FunctionManager.Builder()
                .withUdfClass(Test15.class));

        assertEquals("Invalid UDF name 'test.15.'", actual.getMessage(), "Unexpected message");
    }

    /**
     * Unit test {@link FunctionManager}
     */
    @Test
    public void test_CheckedException() {
        EelFunctionException actual = assertThrows(EelFunctionException.class,
            () -> new FunctionManager.Builder()
                .withUdfClass(Test16.class)
                .build()
                .compileCall(context, "test.16", List.of())
                .evaluate(symbolsTable));
        Throwable cause = actual.getCause();

        assertEquals("Failed to execute function 'test.16'", actual.getMessage(), "Unexpected message");
        assertEquals(IOException.class, cause.getClass(), "Unexpected cause type");
        assertEquals("This message was thrown by test.16", cause.getMessage(), "Unexpected cause message");
    }

    /**
     * Unit test {@link FunctionManager}
     */
    @Test
    public void test_MissingArguments() {
        EelFunctionException actual = assertThrows(EelFunctionException.class,
            () -> new FunctionManager.Builder()
                .withUdfClass(Plus1.class)
                .build()
                .compileCall(context, "test.plus1", List.of())
                .evaluate(symbolsTable));

        assertEquals("Argument 0 for function 'test.plus1' is missing and no default exists",
            actual.getMessage(),
            "Unexpected message");
    }


    /**
     * Unit test {@link FunctionManager}
     */
    @Test
    public void test_TooManyArguments() {
        EelFunctionException actual = assertThrows(EelFunctionException.class,
            () -> new FunctionManager.Builder()
                .withUdfClass(Plus1.class)
                .build()
                .compileCall(context, "test.plus1", List.of(Constant.of(1), Constant.of(2)))
                .evaluate(symbolsTable));

        assertEquals("Expected 1 argument(s) for function 'test.plus1' but 2 were passed",
            actual.getMessage(),
            "Unexpected message");
    }

    /**
     * Unit test {@link FunctionManager}
     */
    @Test
    public void test_HappyPath() {
        Value actual = new FunctionManager.Builder()
            .withUdfClass(Plus1.class)
            .build()
            .compileCall(context, "test.plus1", List.of(Constant.of(3)))
            .evaluate(symbolsTable);

        assertEquals(Value.of(4), actual, "Unexpected value returned");
    }

    /**
     * Unit test {@link FunctionManager}
     */
    @Test
    public void test_reloadClass() {
        Value actual = new FunctionManager.Builder()
            .withUdfClass(Plus1.class)
            .withUdfClass(Plus1.class)
            .build()
            .compileCall(context, "test.plus1", List.of(Constant.of(3)))
            .evaluate(symbolsTable);

        assertEquals(Value.of(4), actual, "Unexpected value returned");
    }

    /**
     * Unit test {@link FunctionManager}
     */
    @Test
    public void test_reloadPackage() {
        Package udfPackage = Plus1.class.getPackage();
        Value actual = new FunctionManager.Builder()
            .withUdfPackage(udfPackage)
            .withUdfPackage(udfPackage)
            .build()
            .compileCall(context, "test.plus1", List.of(Constant.of(3)))
            .evaluate(symbolsTable);

        assertEquals(Value.of(4), actual, "Unexpected value returned");
    }

    /**
     * Unit test {@link FunctionManager}
     */
    @Test
    public void test_duplicateNames() {
        EelFunctionException actual = assertThrows(EelFunctionException.class,
            () -> new FunctionManager.Builder()
                .withUdfClass(func.functions2.One.class)
                .withUdfClass(func.bad_functions.One.class));

        assertEquals("Function 'test.one' has multiple implementations",
            actual.getMessage(),
            "Unexpected message");
    }

    /**
     * Unit test {@link FunctionManager}
     */
    @Test
    public void test_Types() {
        FunctionManager manager = new FunctionManager.Builder()
            .withUdfClass(TestTypes.class)
            .build();

        assertEquals(Value.of("myContext!!"),
            manager.compileCall(context, "types.context", List.of()).evaluate(symbolsTable),
            "pass EelContext");

        assertEquals(Value.of("resourceValue!!"),
            manager.compileCall(context, "types.functionalResource", List.of()).evaluate(symbolsTable),
            "pass FunctionalResource");

        assertEquals(Value.of("Hello"),
            manager.compileCall(context, "types.str", List.of(Constant.of("Hello"))).evaluate(symbolsTable),
            "pass String");

        assertEquals(Value.TRUE,
            manager.compileCall(context, "types.Bool", List.of(Constant.of(Boolean.TRUE))).evaluate(symbolsTable),
            "pass Boolean");
        assertEquals(Value.FALSE,
            manager.compileCall(context, "types.bool", List.of(Constant.of(false))).evaluate(symbolsTable),
            "pass boolean");

        assertEquals(Value.of(12),
            manager.compileCall(context, "types.Byte", List.of(Constant.of((byte) 12))).evaluate(symbolsTable),
            "pass Byte");
        assertEquals(Value.of(93),
            manager.compileCall(context, "types.byte", List.of(Constant.of((byte) 93))).evaluate(symbolsTable),
            "pass byte");

        assertEquals(Value.of(123),
            manager.compileCall(context, "types.Short", List.of(Constant.of((short) 123))).evaluate(symbolsTable),
            "pass Short");
        assertEquals(Value.of(345),
            manager.compileCall(context, "types.short", List.of(Constant.of((short) 345))).evaluate(symbolsTable),
            "pass short");

        assertEquals(Value.of(1234),
            manager.compileCall(context, "types.Int", List.of(Constant.of(1234))).evaluate(symbolsTable),
            "pass Int");
        assertEquals(Value.of(3456),
            manager.compileCall(context, "types.int", List.of(Constant.of(3456))).evaluate(symbolsTable),
            "pass int");

        assertEquals(Value.of(12345),
            manager.compileCall(context, "types.Long", List.of(Constant.of((long) 12345))).evaluate(symbolsTable),
            "pass Long");
        assertEquals(Value.of(34567),
            manager.compileCall(context, "types.long", List.of(Constant.of((long) 34567))).evaluate(symbolsTable),
            "pass long");

        assertEquals(Value.of(123.45),
            manager.compileCall(context, "types.Float", List.of(Constant.of(123.45f))).evaluate(symbolsTable),
            "pass Float");
        assertEquals(Value.of(345.67),
            manager.compileCall(context, "types.float", List.of(Constant.of(345.67f))).evaluate(symbolsTable),
            "pass float");

        assertEquals(Value.of(123.456),
            manager.compileCall(context, "types.Double", List.of(Constant.of(123.456))).evaluate(symbolsTable),
            "pass Double");
        assertEquals(Value.of(345.678),
            manager.compileCall(context, "types.double", List.of(Constant.of(345.678))).evaluate(symbolsTable),
            "pass double");

        assertEquals(Value.of(12345678),
            manager.compileCall(context, "types.BigInt", List.of(Constant.of(12345678))).evaluate(symbolsTable),
            "pass BigInt");
        assertEquals(Value.of(1234.5678),
            manager.compileCall(context, "types.BigDec", List.of(Constant.of(1234.5678))).evaluate(symbolsTable),
            "pass BigDec");
        assertEquals(Value.of(EelContext.FALSE_DATE),
            manager.compileCall(context, "types.date", List.of(Constant.of(EelContext.FALSE_DATE))).evaluate(symbolsTable),
            "pass date");

        assertEquals(Value.of("H"),
            manager.compileCall(context, "types.char", List.of(Constant.of("Hello"))).evaluate(symbolsTable),
            "pass char");
        assertEquals(Value.of("W"),
            manager.compileCall(context, "types.Character", List.of(Constant.of("World"))).evaluate(symbolsTable),
            "pass Character");

        String path = File.separatorChar == '/' ? "/path/to/myFile.txt" : "Z:\\path\\to\\myFile.txt";

        assertEquals(path,
            manager.compileCall(context, "types.File", List.of(Constant.of(path))).evaluate(symbolsTable).asText(),
            "pass File");
    }

    /**
     * Unit test {@link FunctionManager}
     */
    @Test
    public void test_Types_FileFactory() {
        String path = File.separatorChar == '/' ? "/path/to/myFile.txt" : "Z:\\path\\to\\myFile.txt";
        FunctionManager manager = new FunctionManager.Builder()
            .withUdfClass(TestTypes.class)
            .build();

        assertEquals(path,
            manager.compileCall(context, "types.File", List.of(Constant.of(path))).evaluate(symbolsTable).asText(),
            "pass File");
        verify(context).getFile(path);
    }


    /**
     * Unit test {@link FunctionManager}
     */
    @Test
    public void test_Types_value() {
        FunctionManager manager = new FunctionManager.Builder()
            .withUdfClass(TestTypes.class)
            .build();
        Term value = s -> Constant.of(123);

        Value actual = manager.compileCall(context, "types.value", List.of(value)).evaluate(symbolsTable);

        assertTrue((actual instanceof ValueArgument), "Unexpected type returned: " + value.getClass().getName());
        assertEquals(BigDecimal.valueOf(123), actual.asNumber(), "Unexpected value returned");
    }

    /**
     * Unit test {@link FunctionManager}
     */
    @Test
    public void test_HappyPath_MultipleEntryPoints() {
        FunctionManager functionManager = new FunctionManager.Builder()
            .withUdfClass(Test4.class)
            .build();

        Value actual_1 = functionManager.compileCall(context, "test.4_1", List.of()).evaluate(symbolsTable);
        Value actual_2 = functionManager.compileCall(context, "test.4_2", List.of()).evaluate(symbolsTable);

        assertEquals(Value.of("execute1"), actual_1, "Unexpected value returned from function 1");
        assertEquals(Value.of("execute2"), actual_2, "Unexpected value returned from function 1");
    }


    /**
     * Unit test {@link FunctionManager}
     */
    @Test
    public void test_ConvertNumerics() {
        Value actual = new FunctionManager.Builder()
                .withUdfClass(SameValue.class)
                .build()
                .compileCall(context, "test.sameValue",
                    List.of(Constant.of(1), Constant.of(1), Constant.of(1), Constant.of(1), Constant.of(1)))
            .evaluate(symbolsTable);

        assertEquals(Value.TRUE, actual, "Unexpected value returned");
    }


    /**
     * Unit test {@link FunctionManager}
     */
    @Test
    public void test_SuppliedDefaults() {
        List<Term> argumentList = List.of(
            Constant.of("myText"),
            Constant.of(-123),
            Constant.of(true),
            r -> Constant.of(r.read("myVar")),
            Constant.of(ZonedDateTime.of(2022, 1, 2, 3, 4, 5, 6_000_000, ZoneOffset.UTC)),
            Constant.of("myFunction")
        );

        Value actual = new FunctionManager.Builder()
            .withUdfClass(Defaults.class)
            .build()
            .compileCall(context, "test.defaults", argumentList)
            .evaluate(symbolsTable);

        assertEquals(Value.of("Passed 'myText', -123, true, 'late' ~ 2022-01-02T03:04:05.006Z, myFunction"),
            actual,
            "Unexpected value");
    }

    /**
     * Unit test {@link FunctionManager}
     */
    @Test
    public void test_UseDefaults() {
        List<Term> argumentList = List.of(Constant.of("requiredText"));

        Value actual = new FunctionManager.Builder()
            .withUdfClass(Defaults.class)
            .build()
            .compileCall(context, "test.defaults", argumentList)
            .evaluate(symbolsTable);

        assertEquals(Value.of("Passed 'requiredText', 987, false, '???' ~ 2001-02-03T04:05Z, ???"),
            actual,
            "Unexpected value");
    }

    /**
     * Unit test {@link FunctionManager}
     */
    @Test
    public void test_VarArgs() {
        List<Term> argumentList = List.of(Constant.of(1),
            Constant.of(3),
            Constant.of(5),
            Constant.of(11),
            Constant.of(23));

        Value actual = new FunctionManager.Builder()
            .withUdfClass(Sum.class)
            .build()
            .compileCall(context, "test.sum", argumentList)
            .evaluate(symbolsTable);

        assertEquals(Value.of(43), actual, "Unexpected value");
    }

    /**
     * Unit test {@link FunctionManager}
     */
    @Test
    public void test_Dates() {
        Value date = Value.of(ZonedDateTime.of(2022, 1, 2, 3, 4, 5, 6, ZoneOffset.UTC));
        List<Term> argumentList = List.of(r -> date);

        Value actual = new FunctionManager.Builder()
            .withUdfClass(Tomorrow.class)
            .build()
            .compileCall(context, "test.tomorrow", argumentList)
            .evaluate(symbolsTable);

        assertEquals(Constant.of(ZonedDateTime.of(2022, 1, 3, 3, 4, 5, 6, ZoneOffset.UTC)), actual, "Unexpected value");
    }


    /**
     * Unit test {@link FunctionManager}
     */
    @Test
    public void test_Concurrency() {
        Runnable build = () -> {
            long result = new FunctionManager.Builder()
                .build()
                .compileCall(context, "number.c", Collections.emptyList())
                .evaluate(symbolsTable)
                .asLong();

            assertEquals(299_792_458, result, "Unexpected result");
        };

        IntStream.range(0, 99)
            .parallel()
            .forEach(i -> build.run());
    }
}