package com.github.tymefly.eel;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.function.Function;

import javax.annotation.Nonnull;

import com.github.tymefly.eel.builder.EelContextSettingBuilder;
import func.bad_functions.Test1;
import func.functions.Plus1;
import helper.MockConstructor;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit test for {@link Eel}
 */
public class EelTest {
    private EelContextImpl context;
    private SymbolsTable symbolsTable;
    private Executor executor;
    private BuildTime buildTime;


    @Before
    public void setUp() {
        context = mock();
        symbolsTable = mock();
        executor = mock();
        buildTime = mock();

        when(context.maxExpressionLength())
            .thenReturn(100);

        when(buildTime.version())
            .thenReturn("mockedVersion");
        when(buildTime.buildDate())
            .thenReturn(EelContext.FALSE_DATE);
    }


    private void mockContextBuilder(@Nonnull EelContextImpl.Builder mock) {
        when(mock.build())
            .thenReturn(context);
    }

    private void mockParser(@Nonnull Parser mock) {
        when(mock.parse())
            .thenReturn(executor);
    }



    /**
     * Unit test {@link Eel#metadata()}
     */
    @Test
    public void test_eelInfo() {
        try (
            MockedStatic<BuildTime> buildTime = Mockito.mockStatic(BuildTime.class)
        ) {
            buildTime.when(BuildTime::getInstance)
                .thenReturn(this.buildTime);

            Metadata actual = Eel.metadata();

            Assert.assertEquals("Unexpected information", "mockedVersion", actual.version());
        }
    }


    /**
     * Unit test {@link Eel#compile(String)}
     */
    @Test
    public void test_compile_basic() {
        try (
            MockConstructor<Tokenizer> tokenizerMock = new MockConstructor<>(Tokenizer.class);
            MockConstructor<LambdaCompiler> compilerMock = new MockConstructor<>(LambdaCompiler.class);
            MockConstructor<Parser> parserMock = new MockConstructor<>(Parser.class)
        ) {
            Eel.compile("Test me");

            Source source = tokenizerMock.getArgument(0, Source.class);
            String expression = readExpression(source);

            Assert.assertEquals("Unexpected expression",
                "Test me",
                expression);
            Assert.assertEquals("Unexpected expression length",
                EelContextSettingBuilder.DEFAULT_MAX_EXPRESSION_LENGTH,
                source.getMaxLength());
            Assert.assertSame("Inconsistent context",
                compilerMock.getArgument(0, EelContext.class),
                parserMock.getArgument(0, EelContext.class));
            Assert.assertSame("Unexpected tokenizer",
                tokenizerMock.getMock(),
                parserMock.getArgument(1, Tokenizer.class));
            Assert.assertSame("Unexpected compiler",
                compilerMock.getMock(),
                parserMock.getArgument(2, LambdaCompiler.class));
        }
    }


    /**
     * Unit test {@link Eel#compile(String)}
     */
    @Test
    public void test_compile_basic_reuseContext() {
        EelContext first;
        EelContext second;

        try (
            MockConstructor<Tokenizer> tokenizerMock = new MockConstructor<>(Tokenizer.class);
            MockConstructor<LambdaCompiler> compilerMock = new MockConstructor<>(LambdaCompiler.class);
            MockConstructor<Parser> parserMock = new MockConstructor<>(Parser.class)
        ) {
            Eel.compile("First");

            first = compilerMock.getArgument(0, EelContext.class);
        }

        try (
            MockConstructor<Tokenizer> tokenizerMock = new MockConstructor<>(Tokenizer.class);
            MockConstructor<LambdaCompiler> compilerMock = new MockConstructor<>(LambdaCompiler.class);
            MockConstructor<Parser> parserMock = new MockConstructor<>(Parser.class)
        ) {
            Eel.compile("Second");

            second = compilerMock.getArgument(0, EelContext.class);
        }

        Assert.assertSame("Context was not reused", first, second);
    }

    /**
     * Unit test {@link Eel#compile(EelContext, String)}
     */
    @Test
    public void test_compile_customContext() {
        EelContextImpl context = mock();

        when(context.maxExpressionLength())
            .thenReturn(100);

        try (
            MockConstructor<Tokenizer> tokenizerMock = new MockConstructor<>(Tokenizer.class);
            MockConstructor<LambdaCompiler> compilerMock = new MockConstructor<>(LambdaCompiler.class);
            MockConstructor<Parser> parserMock = new MockConstructor<>(Parser.class)
        ) {
            Eel.compile(context, "Test me");

            Source source = tokenizerMock.getArgument(0, Source.class);
            String expression = readExpression(source);

            Assert.assertEquals("Unexpected expression",
                "Test me",
                expression);
            Assert.assertEquals("Unexpected expression length",
                100,
                source.getMaxLength());
            Assert.assertSame("Unexpected context passed to compiler",
                context,
                compilerMock.getArgument(0, EelContext.class));
            Assert.assertSame("Unexpected context passed to parser",
                context,
                parserMock.getArgument(0, EelContext.class));
            Assert.assertSame("Unexpected tokenizer",
                tokenizerMock.getMock(),
                parserMock.getArgument(1, Tokenizer.class));
            Assert.assertSame("Unexpected compiler",
                compilerMock.getMock(),
                parserMock.getArgument(2, LambdaCompiler.class));
        }
    }

    /**
     * Unit test {@link Eel#factory()}
     */
    @Test
    public void test_factory_explicitContext() {
        try (
            MockConstructor<Tokenizer> tokenizerMock = new MockConstructor<>(Tokenizer.class);
            MockConstructor<LambdaCompiler> compilerMock = new MockConstructor<>(LambdaCompiler.class);
            MockConstructor<Parser> parserMock = new MockConstructor<>(Parser.class);
        ) {
            Eel.factory()
                .withContext(context)
                .compile("Test me");

            Source source = tokenizerMock.getArgument(0, Source.class);
            String expression = readExpression(source);

            Assert.assertEquals("Unexpected expression",
                "Test me",
                expression);
            Assert.assertEquals("Unexpected expression length",
                100,
                source.getMaxLength());
            Assert.assertSame("Unexpected context passed to compiler",
                context,
                compilerMock.getArgument(0, EelContext.class));
            Assert.assertSame("Unexpected context passed to parser",
                context,
                parserMock.getArgument(0, EelContext.class));
            Assert.assertSame("Unexpected tokenizer",
                tokenizerMock.getMock(),
                parserMock.getArgument(1, Tokenizer.class));
            Assert.assertSame("Unexpected compiler",
                compilerMock.getMock(),
                parserMock.getArgument(2, LambdaCompiler.class));
        }
    }

    /**
     * Unit test {@link Eel#factory()}
     */
    @Test
    public void test_factory_inlineContext() {
        try (
            MockConstructor<Tokenizer> tokenizerMock = new MockConstructor<>(Tokenizer.class);
            MockConstructor<LambdaCompiler> compilerMock = new MockConstructor<>(LambdaCompiler.class);
            MockConstructor<Parser> parserMock = new MockConstructor<>(Parser.class);
            MockConstructor<EelContextImpl.Builder> contextFactory =
                new MockConstructor<>(EelContextImpl.Builder.class, this::mockContextBuilder);
        ) {
            Eel.factory()
                .withMaxExpressionSize(123)
                .withPrecision(6)
                .withUdfPackage(Plus1.class.getPackage())
                .withUdfClass(Test1.class)
                .compile("Test me");

            Source source = tokenizerMock.getArgument(0, Source.class);
            String expression = readExpression(source);

            Assert.assertEquals("Unexpected expression",
                "Test me",
                expression);
            Assert.assertSame("Unexpected context passed to compiler",
                context,
                compilerMock.getArgument(0, EelContext.class));
            Assert.assertSame("Unexpected context passed to parser",
                context,
                parserMock.getArgument(0, EelContext.class));
            Assert.assertSame("Unexpected tokenizer",
                tokenizerMock.getMock(),
                parserMock.getArgument(1, Tokenizer.class));
            Assert.assertSame("Unexpected compiler",
                compilerMock.getMock(),
                parserMock.getArgument(2, LambdaCompiler.class));

            verify(contextFactory.getMock()).withMaxExpressionSize(123);
            verify(contextFactory.getMock()).withPrecision(6);
            verify(contextFactory.getMock()).withUdfPackage(Plus1.class.getPackage());
            verify(contextFactory.getMock()).withUdfClass(Test1.class);
        }
    }


    /**
     * Unit test {@link Eel#factory()}
     */
    @Test
    public void test_factory_fromStream() {
        try (
            MockConstructor<Tokenizer> tokenizerMock = new MockConstructor<>(Tokenizer.class);
            MockConstructor<LambdaCompiler> compilerMock = new MockConstructor<>(LambdaCompiler.class);
            MockConstructor<Parser> parserMock = new MockConstructor<>(Parser.class)
        ) {
            Eel.factory()
                .compile(new ByteArrayInputStream("myStream".getBytes(StandardCharsets.UTF_8)));

            Source source = tokenizerMock.getArgument(0, Source.class);
            String expression = readExpression(source);

            Assert.assertEquals("Unexpected expression",
                "myStream",
                expression);
            Assert.assertSame("Inconsistent context",
                compilerMock.getArgument(0, EelContext.class),
                parserMock.getArgument(0, EelContext.class));
            Assert.assertSame("Unexpected tokenizer",
                tokenizerMock.getMock(),
                parserMock.getArgument(1, Tokenizer.class));
            Assert.assertSame("Unexpected compiler",
                compilerMock.getMock(),
                parserMock.getArgument(2, LambdaCompiler.class));
        }
    }


    /**
     * Unit test {@link Eel#evaluate()}
     */
    @Test
    public void test_evaluate_NoSymbolsTable() {
        ArgumentCaptor<SymbolsTable> captor = ArgumentCaptor.forClass(SymbolsTable.class);

        try (
            MockConstructor<Tokenizer> tokenizerMock = new MockConstructor<>(Tokenizer.class);
            MockConstructor<LambdaCompiler> compilerMock = new MockConstructor<>(LambdaCompiler.class);
            MockConstructor<Parser> parserMock = new MockConstructor<>(Parser.class, this::mockParser)
        ) {
            Eel.compile("${myVar}")
                .evaluate();

            verify(executor).execute(captor.capture());

            Assert.assertSame("Unexpected symbols table", SymbolsTable.EMPTY, captor.getValue());
        }
    }

    /**
     * Unit test {@link Eel#evaluate(SymbolsTable)}
     */
    @Test
    public void test_evaluate_SymbolsTable_explicit() {
        ArgumentCaptor<SymbolsTable> captor = ArgumentCaptor.forClass(SymbolsTable.class);
        SymbolsTable actual = mock();

        try (
            MockConstructor<Tokenizer> tokenizerMock = new MockConstructor<>(Tokenizer.class);
            MockConstructor<LambdaCompiler> compilerMock = new MockConstructor<>(LambdaCompiler.class);
            MockConstructor<Parser> parserMock = new MockConstructor<>(Parser.class, this::mockParser)
        ) {
            Eel.compile("${myVar}")
                .evaluate(actual);

            verify(executor).execute(captor.capture());

            Assert.assertSame("Unexpected symbols table", actual, captor.getValue());
        }
    }

    /**
     * Unit test {@link Eel#evaluate(Map)}
     */
    @Test
    public void test_evaluate_SymbolsTable_map() {
        ArgumentCaptor<SymbolsTable> captor = ArgumentCaptor.forClass(SymbolsTable.class);
        Map<String, String> backing = Map.of("key", "value");

        try (
            MockConstructor<Tokenizer> tokenizerMock = new MockConstructor<>(Tokenizer.class);
            MockConstructor<LambdaCompiler> compilerMock = new MockConstructor<>(LambdaCompiler.class);
            MockConstructor<Parser> parserMock = new MockConstructor<>(Parser.class, this::mockParser);
            MockedStatic<SymbolsTable> symbolsMock = mockStatic(SymbolsTable.class)
        ) {
            symbolsMock.when(() -> SymbolsTable.from(anyMap()))
                .thenReturn(symbolsTable);

            Eel.compile("${myVar}")
                .evaluate(backing);

            symbolsMock.verify(() -> SymbolsTable.from(backing));
            verify(executor).execute(captor.capture());

            Assert.assertSame("Unexpected symbols table", symbolsTable, captor.getValue());
        }
    }

    /**
     * Unit test {@link Eel#evaluate(Map)}
     */
    @Test
    public void test_evaluate_SymbolsTable_map_scoped() {
        ArgumentCaptor<SymbolsTable> captor = ArgumentCaptor.forClass(SymbolsTable.class);
        Map<String, String> backing = Map.of("key", "value");

        try (
            MockConstructor<Tokenizer> tokenizerMock = new MockConstructor<>(Tokenizer.class);
            MockConstructor<LambdaCompiler> compilerMock = new MockConstructor<>(LambdaCompiler.class);
            MockConstructor<Parser> parserMock = new MockConstructor<>(Parser.class, this::mockParser);
            MockedStatic<SymbolsTable> symbolsMock = mockStatic(SymbolsTable.class)
        ) {
            symbolsMock.when(() -> SymbolsTable.from(eq("map"), anyMap()))
                .thenReturn(symbolsTable);

            Eel.compile("${map.myVar}")
                .evaluate("map", backing);

            symbolsMock.verify(() -> SymbolsTable.from("map", backing));
            verify(executor).execute(captor.capture());

            Assert.assertSame("Unexpected symbols table", symbolsTable, captor.getValue());
        }
    }

    /**
     * Unit test {@link Eel#evaluate(Function)}
     */
    @Test
    public void test_evaluate_SymbolsTable_callBack() {
        ArgumentCaptor<SymbolsTable> captor = ArgumentCaptor.forClass(SymbolsTable.class);
        Function<String, String> backing = s -> s + "!";

        try (
            MockConstructor<Tokenizer> tokenizerMock = new MockConstructor<>(Tokenizer.class);
            MockConstructor<LambdaCompiler> compilerMock = new MockConstructor<>(LambdaCompiler.class);
            MockConstructor<Parser> parserMock = new MockConstructor<>(Parser.class, this::mockParser);
            MockedStatic<SymbolsTable> symbolsMock = mockStatic(SymbolsTable.class)
        ) {
            symbolsMock.when(() -> SymbolsTable.from(any(Function.class)))
                .thenReturn(symbolsTable);

            Eel.compile("${myVar}")
                .evaluate(backing);

            symbolsMock.verify(() -> SymbolsTable.from(backing));
            verify(executor).execute(captor.capture());

            Assert.assertSame("Unexpected symbols table", symbolsTable, captor.getValue());
        }
    }

    /**
     * Unit test {@link Eel#evaluate(Function)}
     */
    @Test
    public void test_evaluate_SymbolsTable_callBack_scoped() {
        ArgumentCaptor<SymbolsTable> captor = ArgumentCaptor.forClass(SymbolsTable.class);
        Function<String, String> backing = s -> s + "!";

        try (
            MockConstructor<Tokenizer> tokenizerMock = new MockConstructor<>(Tokenizer.class);
            MockConstructor<LambdaCompiler> compilerMock = new MockConstructor<>(LambdaCompiler.class);
            MockConstructor<Parser> parserMock = new MockConstructor<>(Parser.class, this::mockParser);
            MockedStatic<SymbolsTable> symbolsMock = mockStatic(SymbolsTable.class)
        ) {
            symbolsMock.when(() -> SymbolsTable.from(eq("func"), any(Function.class)))
                .thenReturn(symbolsTable);

            Eel.compile("${func.myVar}")
                .evaluate("func", backing);

            symbolsMock.verify(() -> SymbolsTable.from("func", backing));
            verify(executor).execute(captor.capture());

            Assert.assertSame("Unexpected symbols table", symbolsTable, captor.getValue());
        }
    }

    /**
     * Unit test {@link Eel#evaluateEnvironment()}
     */
    @Test
    public void test_evaluate_SymbolsTable_Environment() {
        ArgumentCaptor<SymbolsTable> captor = ArgumentCaptor.forClass(SymbolsTable.class);

        try (
            MockConstructor<Tokenizer> tokenizerMock = new MockConstructor<>(Tokenizer.class);
            MockConstructor<LambdaCompiler> compilerMock = new MockConstructor<>(LambdaCompiler.class);
            MockConstructor<Parser> parserMock = new MockConstructor<>(Parser.class, this::mockParser);
            MockedStatic<SymbolsTable> symbolsMock = mockStatic(SymbolsTable.class)
        ) {
            symbolsMock.when(SymbolsTable::fromEnvironment)
                .thenReturn(symbolsTable);

            Eel.compile("${myVar}")
                .evaluateEnvironment();

            symbolsMock.verify(SymbolsTable::fromEnvironment);
            verify(executor).execute(captor.capture());

            Assert.assertSame("Unexpected symbols table", symbolsTable, captor.getValue());
        }
    }

    /**
     * Unit test {@link Eel#evaluateEnvironment()}
     */
    @Test
    public void test_evaluate_SymbolsTable_Environment_scoped() {
        ArgumentCaptor<SymbolsTable> captor = ArgumentCaptor.forClass(SymbolsTable.class);

        try (
            MockConstructor<Tokenizer> tokenizerMock = new MockConstructor<>(Tokenizer.class);
            MockConstructor<LambdaCompiler> compilerMock = new MockConstructor<>(LambdaCompiler.class);
            MockConstructor<Parser> parserMock = new MockConstructor<>(Parser.class, this::mockParser);
            MockedStatic<SymbolsTable> symbolsMock = mockStatic(SymbolsTable.class)
        ) {
            symbolsMock.when(() -> SymbolsTable.fromEnvironment("env"))
                .thenReturn(symbolsTable);

            Eel.compile("${env.myVar}")
                .evaluateEnvironment("env");

            symbolsMock.verify(() -> SymbolsTable.fromEnvironment("env"));
            verify(executor).execute(captor.capture());

            Assert.assertSame("Unexpected symbols table", symbolsTable, captor.getValue());
        }
    }

    /**
     * Unit test {@link Eel#evaluateProperties()}
     */
    @Test
    public void test_evaluate_SymbolsTable_Properties() {
        ArgumentCaptor<SymbolsTable> captor = ArgumentCaptor.forClass(SymbolsTable.class);

        try (
            MockConstructor<Tokenizer> tokenizerMock = new MockConstructor<>(Tokenizer.class);
            MockConstructor<LambdaCompiler> compilerMock = new MockConstructor<>(LambdaCompiler.class);
            MockConstructor<Parser> parserMock = new MockConstructor<>(Parser.class, this::mockParser);
            MockedStatic<SymbolsTable> symbolsMock = mockStatic(SymbolsTable.class)
        ) {
            symbolsMock.when(SymbolsTable::fromProperties)
                .thenReturn(symbolsTable);

            Eel.compile("${myVar}")
                .evaluateProperties();

            symbolsMock.verify(SymbolsTable::fromProperties);
            verify(executor).execute(captor.capture());

            Assert.assertSame("Unexpected symbols table", symbolsTable, captor.getValue());
        }
    }

    /**
     * Unit test {@link Eel#evaluateProperties()}
     */
    @Test
    public void test_evaluate_SymbolsTable_Properties_scoped() {
        ArgumentCaptor<SymbolsTable> captor = ArgumentCaptor.forClass(SymbolsTable.class);

        try (
            MockConstructor<Tokenizer> tokenizerMock = new MockConstructor<>(Tokenizer.class);
            MockConstructor<LambdaCompiler> compilerMock = new MockConstructor<>(LambdaCompiler.class);
            MockConstructor<Parser> parserMock = new MockConstructor<>(Parser.class, this::mockParser);
            MockedStatic<SymbolsTable> symbolsMock = mockStatic(SymbolsTable.class)
        ) {
            symbolsMock.when(() -> SymbolsTable.fromProperties("prop"))
                .thenReturn(symbolsTable);

            Eel.compile("${prop.myVar}")
                .evaluateProperties("prop");

            symbolsMock.verify(() -> SymbolsTable.fromProperties("prop"));
            verify(executor).execute(captor.capture());

            Assert.assertSame("Unexpected symbols table", symbolsTable, captor.getValue());
        }
    }

    /**
     * Unit test {@link Eel#evaluate(Function)}
     */
    @Test
    public void test_evaluate_SymbolsTable_String() {
        ArgumentCaptor<SymbolsTable> captor = ArgumentCaptor.forClass(SymbolsTable.class);

        try (
            MockConstructor<Tokenizer> tokenizerMock = new MockConstructor<>(Tokenizer.class);
            MockConstructor<LambdaCompiler> compilerMock = new MockConstructor<>(LambdaCompiler.class);
            MockConstructor<Parser> parserMock = new MockConstructor<>(Parser.class, this::mockParser);
            MockedStatic<SymbolsTable> symbolsMock = mockStatic(SymbolsTable.class)
        ) {
            symbolsMock.when(() -> SymbolsTable.from(anyString()))
                .thenReturn(symbolsTable);

            Eel.compile("${myVar}")
                .evaluate("defaultValue");

            symbolsMock.verify(() -> SymbolsTable.from("defaultValue"));
            verify(executor).execute(captor.capture());

            Assert.assertSame("Unexpected symbols table", symbolsTable, captor.getValue());
        }
    }



    @Nonnull
    private String readExpression(@Nonnull Source source) {
        StringBuilder result = new StringBuilder();
        char in = source.current();

        while (in != Input.END) {
            result.append(in);
            in = source.read();
        }

        return result.toString();
    }
}