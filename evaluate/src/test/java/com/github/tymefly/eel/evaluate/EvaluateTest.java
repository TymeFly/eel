package com.github.tymefly.eel.evaluate;

import java.io.File;
import java.io.InputStream;
import java.time.DayOfWeek;
import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.github.tymefly.eel.Eel;
import com.github.tymefly.eel.EelContext;
import com.github.tymefly.eel.Result;
import com.github.tymefly.eel.SymbolsTable;
import com.github.tymefly.eel.Type;
import com.github.tymefly.eel.builder.EelBuilder;
import com.github.tymefly.eel.builder.EelContextBuilder;
import com.github.tymefly.eel.builder.SymbolsTableBuilder;
import com.github.tymefly.eel.exception.EelUnknownSymbolException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.MockedStatic;
import test.functions1.Plus1;
import test.functions2.Plus2;
import uk.org.webcompere.systemstubs.rules.SystemErrRule;
import uk.org.webcompere.systemstubs.rules.SystemExitRule;
import uk.org.webcompere.systemstubs.rules.SystemOutRule;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit test for {@link Evaluate}
 */
public class EvaluateTest {
    @Rule
    public final SystemExitRule exit = new SystemExitRule();

    @Rule
    public SystemOutRule stdOut = new SystemOutRule();

    @Rule
    public SystemErrRule stdErr = new SystemErrRule();


    private Config config;
    private EelBuilder eelBuilder;
    private Eel eel;
    private EelContextBuilder contextBuilder;
    private EelContext context;
    private SymbolsTableBuilder symbolsTableBuilder;
    private SymbolsTable symbolTable;
    private final String testScript = this.getClass()
        .getClassLoader()
        .getResource("UnitTest.eel")
        .getFile();

    private Package functions;


    @Before
    public void setUp() {
        Result result = mock();

        config = mock();
        eelBuilder = mock();
        eel = mock();
        contextBuilder = mock();
        context = spy(EelContext.factory().build());
        symbolsTableBuilder = mock();
        symbolTable = mock();
        functions = mock();

        // train the config
        when(config.getExpression())
            .thenReturn("myExpression");
        when(config.isValid())
            .thenReturn(true);
        when(config.verbose())
            .thenReturn(true);
        when(config.precision())
            .thenReturn(123);
        when(config.ioLimit())
            .thenReturn(1024);
        when(config.startOfWeek())
            .thenReturn(DayOfWeek.SUNDAY);
        when(config.daysInFirstWeek())
            .thenReturn(1);
        when(config.functionList())
            .thenReturn(List.of(Plus1.class, Plus2.class));
        when(config.packageList())
            .thenReturn(List.of(functions));
        when(config.useProperties())
            .thenReturn(true);
        when(config.useEnvironmentVariables())
            .thenReturn(true);
        when(config.definitions())
            .thenReturn(Map.of("k1", "v1", "k2", "v2"));
        when(config.defaultValue())
            .thenReturn("<default>");

        // train the expression builder API
        when(eelBuilder.withContext(any(EelContext.class)))
            .thenReturn(eelBuilder);
        when(eelBuilder.compile(anyString()))
            .thenReturn(eel);
        when(eelBuilder.compile(any(InputStream.class)))
            .thenReturn(eel);
        when(eel.evaluate(any(SymbolsTable.class)))
            .thenReturn(result);

        // train the context builder API
        when(contextBuilder.withPrecision(anyInt()))
            .thenReturn(contextBuilder);
        when(contextBuilder.withTimeout(any(Duration.class)))
            .thenReturn(contextBuilder);
        when(contextBuilder.withIoLimit(anyInt()))
            .thenReturn(contextBuilder);
        when(contextBuilder.withMinimalDaysInFirstWeek(anyInt()))
            .thenReturn(contextBuilder);
        when(contextBuilder.withStartOfWeek(any(DayOfWeek.class)))
            .thenReturn(contextBuilder);
        when(contextBuilder.withUdfClass(any(Class.class)))
            .thenReturn(contextBuilder);
        when(contextBuilder.withUdfPackage(any(Package.class)))
            .thenReturn(contextBuilder);
        when(contextBuilder.build())
            .thenReturn(context);

        // train the symbols table builder API
        when(symbolsTableBuilder.withProperties())
            .thenReturn(symbolsTableBuilder);
        when(symbolsTableBuilder.withEnvironment())
            .thenReturn(symbolsTableBuilder);
        when(symbolsTableBuilder.withValues(anyMap()))
            .thenReturn(symbolsTableBuilder);
        when(symbolsTableBuilder.withDefault(anyString()))
            .thenReturn(symbolsTableBuilder);
        when(symbolsTableBuilder.build())
            .thenReturn(symbolTable);

        // train the result
        when(result.getType())
            .thenReturn(Type.LOGIC);
        when(result.asText())
            .thenReturn("Generated Text");
    }

    /**
     * Unit test {@link Evaluate}
     */
    @Test
    public void test_HappyPath() throws Exception {
        try (
            MockedStatic<Config> staticConfig = mockStatic(Config.class);
            MockedStatic<Eel> staticEel = mockStatic(Eel.class);
            MockedStatic<EelContext> staticContextBuilder = mockStatic(EelContext.class);
            MockedStatic<SymbolsTable> staticSymbolsTable = mockStatic(SymbolsTable.class)
        ) {
            staticConfig.when(() -> Config.parse(any(String[].class)))
                .thenReturn(config);
            staticEel.when(Eel::factory)
                .thenReturn(eelBuilder);
            staticContextBuilder.when(EelContext::factory)
                .thenReturn(contextBuilder);
            staticSymbolsTable.when(SymbolsTable::factory)
                .thenReturn(symbolsTableBuilder);

            exit.execute(() -> Evaluate.main(new String[] { "Hello", "World" }));

            Assert.assertEquals("Unexpected return code", 0, (int) exit.getExitCode());
        }

        Assert.assertTrue("Unexpected output", stdOut.getLinesNormalized().startsWith("[LOGIC] Generated Text"));
    }

    /**
     * Unit test {@link Evaluate}
     */
    @Test
    public void test_HappyPath_calls() {
        try (
            MockedStatic<Config> staticConfig = mockStatic(Config.class);
            MockedStatic<Eel> staticEel = mockStatic(Eel.class);
            MockedStatic<EelContext> staticContextBuilder = mockStatic(EelContext.class);
            MockedStatic<SymbolsTable> staticSymbolsTable = mockStatic(SymbolsTable.class)
        ) {
            staticConfig.when(() -> Config.parse(any(String[].class)))
                .thenReturn(config);
            staticEel.when(Eel::factory)
                .thenReturn(eelBuilder);
            staticContextBuilder.when(EelContext::factory)
                .thenReturn(contextBuilder);
            staticSymbolsTable.when(SymbolsTable::factory)
                .thenReturn(symbolsTableBuilder);

            Evaluate.execute(new String[] { "Hello", "World" });
        }

        // verify context builder API
        verify(contextBuilder).withPrecision(123);
        verify(contextBuilder).withIoLimit(1024);
        verify(contextBuilder).withMinimalDaysInFirstWeek(1);
        verify(contextBuilder).withUdfClass(Plus1.class);
        verify(contextBuilder).withUdfClass(Plus2.class);
        verify(contextBuilder).withUdfPackage(functions);
        verify(contextBuilder).build();

        // symbols table builder API
        verify(symbolsTableBuilder).withProperties();
        verify(symbolsTableBuilder).withEnvironment();
        verify(symbolsTableBuilder).withValues(Map.of("k1", "v1", "k2", "v2"));
        verify(symbolsTableBuilder).withDefault("<default>");
        verify(symbolsTableBuilder).build();

        // verify expression builder API
        verify(eelBuilder).withContext(context);
        verify(eelBuilder).compile(any(InputStream.class));
        verify(eel).evaluate(symbolTable);

        Assert.assertTrue("Unexpected output", stdOut.getLinesNormalized().startsWith("[LOGIC] Generated Text"));
    }

    /**
     * Unit test {@link Evaluate}
     */
    @Test
    public void test_Script() {
        when(config.getScriptFile())
            .thenReturn(new File(testScript));
        when(config.getExpression())
            .thenReturn(null);
        when(config.functionList())
            .thenReturn(Collections.emptyList());
        when(config.packageList())
            .thenReturn(Collections.emptyList());

        try (
            MockedStatic<Config> staticConfig = mockStatic(Config.class);
            MockedStatic<Eel> staticEel = mockStatic(Eel.class);
            MockedStatic<EelContext> staticContextBuilder = mockStatic(EelContext.class);
            MockedStatic<SymbolsTable> staticSymbolsTable = mockStatic(SymbolsTable.class)
        ) {
            staticConfig.when(() -> Config.parse(any(String[].class)))
                .thenReturn(config);
            staticEel.when(Eel::factory)
                .thenReturn(eelBuilder);
            staticContextBuilder.when(EelContext::factory)
                .thenReturn(contextBuilder);
            staticSymbolsTable.when(SymbolsTable::factory)
                .thenReturn(symbolsTableBuilder);

            Evaluate.execute(new String[] { "Hello", "World" });
        }

        Assert.assertTrue("Unexpected output", stdOut.getLinesNormalized().startsWith("[LOGIC] Generated Text"));
    }

    /**
     * Unit test {@link Evaluate}
     */
    @Test
    public void test_Script_NotFound() throws Exception {
        when(config.getScriptFile())
            .thenReturn(new File("not Found"));
        when(config.getExpression())
            .thenReturn(null);
        when(config.functionList())
            .thenReturn(Collections.emptyList());
        when(config.packageList())
            .thenReturn(Collections.emptyList());

        try (
            MockedStatic<Config> staticConfig = mockStatic(Config.class)
        ) {
            staticConfig.when(() -> Config.parse(any(String[].class)))
                .thenReturn(config);

            exit.execute(() -> Evaluate.main(new String[] { "Hello", "World" }));

            Assert.assertEquals("Unexpected return code", 12, (int) exit.getExitCode());
        }
    }

    /**
     * Unit test {@link Evaluate}
     */
    @Test
    public void test_HelpPage() throws Exception {
        when(config.requestHelp())
            .thenReturn(true);

        try (
            MockedStatic<Config> staticConfig = mockStatic(Config.class)
        ) {
            staticConfig.when(() -> Config.parse(any(String[].class)))
                .thenReturn(config);

            exit.execute(() -> Evaluate.main(new String[] { "Hello", "World" }));

            Assert.assertEquals("Unexpected return code", 1, (int) exit.getExitCode());
        }
    }

    /**
     * Unit test {@link Evaluate}
     */
    @Test
    public void test_Version() throws Exception {
        when(config.requestVersion())
            .thenReturn(true);

        try (
            MockedStatic<Config> staticConfig = mockStatic(Config.class)
        ) {
            staticConfig.when(() -> Config.parse(any(String[].class)))
                .thenReturn(config);

            exit.execute(() -> Evaluate.main(new String[] { "--version" }));

            Assert.assertEquals("Unexpected return code", 2, (int) exit.getExitCode());
        }
    }


    /**
     * Unit test {@link Evaluate}
     */
    @Test
    public void test_BadExpression() throws Exception {
        when(eel.evaluate(any(SymbolsTable.class))).
            thenThrow(new EelUnknownSymbolException("expected"));

        try (
            MockedStatic<Config> staticConfig = mockStatic(Config.class);
            MockedStatic<Eel> staticEel = mockStatic(Eel.class);
            MockedStatic<EelContext> staticContextBuilder = mockStatic(EelContext.class);
            MockedStatic<SymbolsTable> staticSymbolsTable = mockStatic(SymbolsTable.class)
        ) {
            staticConfig.when(() -> Config.parse(any(String[].class)))
                .thenReturn(config);
            staticEel.when(Eel::factory)
                .thenReturn(eelBuilder);
            staticContextBuilder.when(EelContext::factory)
                .thenReturn(contextBuilder);
            staticSymbolsTable.when(SymbolsTable::factory)
                .thenReturn(symbolsTableBuilder);

            exit.execute(() -> Evaluate.main(new String[] { "Hello", "World" }));

            Assert.assertEquals("Unexpected return code", 11, (int) exit.getExitCode());
        }
    }


    /**
     * Unit test {@link Evaluate}
     */
    @Test
    public void test_BadCommandLine() throws Exception {
        when(config.isValid())
            .thenReturn(false);

        try (
            MockedStatic<Config> staticConfig = mockStatic(Config.class)
        ) {
            staticConfig.when(() -> Config.parse(any(String[].class)))
                .thenReturn(config);

            exit.execute(() -> Evaluate.main(new String[] { "Hello", "World" }));

            Assert.assertEquals("Unexpected return code", 10, (int) exit.getExitCode());
        }
    }
}