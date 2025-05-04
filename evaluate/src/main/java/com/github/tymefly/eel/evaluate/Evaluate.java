package com.github.tymefly.eel.evaluate;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.stream.Stream;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.github.tymefly.eel.Eel;
import com.github.tymefly.eel.EelContext;
import com.github.tymefly.eel.Metadata;
import com.github.tymefly.eel.Result;
import com.github.tymefly.eel.SymbolsTable;
import com.github.tymefly.eel.annotation.VisibleForTesting;
import com.github.tymefly.eel.builder.EelContextBuilder;
import com.github.tymefly.eel.exception.EelException;

/**
 * A Command Line driven application that can be used to test EEL expressions.
 * Sample usage is:
 * <ul>
 *     <li>{@code java -jar evaluate-<version>.jar --help} - display CLI help</li>
 *     <li>{@code java -jar evaluate-<version>.jar 'One plus Two: $( 1 + 2 )'} - Evaluate simple expression</li>
 *     <li>{@code java -jar evaluate-<version>.jar --precision 2 '$( 1 / 3 )'} - Set numeric precision</li>
 *     <li>{@code java -jar evaluate-<version>.jar -D name=World 'Hello ${name}'} - Pass a value used in expression</li>
 *     <li>{@code java -jar evaluate-<version>.jar --props 'Hello ${user.name}'} - use JVM properties in expression</li>
 *     <li>{@code java -jar evaluate-<version>.jar '$( date("-7d", "yyyy-MM-dd") )'} - Call a standard function</li>
 * </ul>
 */
public class Evaluate {
    private final Config config;

    private Evaluate(@Nonnull Config config) {
        this.config = config;
    }

    @Nonnull
    private State run() {
        String expression = config.getExpression();
        File scriptFile = config.getScriptFile();
        State state;

        if (expression != null) {
            state = run(expression, null, ": " + expression);
        } else if ((scriptFile != null) && !scriptFile.isFile()) {
            System.err.println("Can not find script file " + scriptFile.getPath());
            state = State.SCRIPT_NOT_FOUND;
        } else if (scriptFile != null) {
            state = run(null, scriptFile, "script '" + scriptFile.getPath() + "'");
        } else { // Should not happen - Config guards against it
            throw new RuntimeException("Internal Error: No expression or script provided");
        }

        return state;
    }

    @Nonnull
    private State run(@Nullable String expression, @Nullable File scriptFile, @Nonnull String message) {
        State state;

        try (
            InputStream source = (expression != null ?
                new ByteArrayInputStream(expression.getBytes(StandardCharsets.UTF_8)) :
                new FileInputStream(scriptFile));
            InputStream buffered = new BufferedInputStream(source)
        ) {
            Result result = Eel.factory()
                .withContext(createContext())
                .compile(buffered)
                .evaluate(createSymbolsTable());
            String type = config.verbose() ? "[" + result.getType() + "] " : "";

            System.out.println(type + result.asText());

            state = State.EVALUATED;
        } catch (EelException e) {
            System.err.println("Failed to evaluate " + message);
            e.printStackTrace();
            state = State.EXPRESSION_FAILED;
        } catch (IOException e) {
            System.err.println("Failed to read script file " + scriptFile.getPath());
            e.printStackTrace();
            state = State.SCRIPT_NOT_FOUND;
        }

        return state;
    }


    @Nonnull
    private State showVersion() {
        Metadata metadata = Eel.metadata();

        System.out.printf("Eel version %s, built on %s%n",
            metadata.version(),
            metadata.buildDate());

        return State.VERSION;
    }

    @Nonnull
    private EelContext createContext() {
        EelContextBuilder builder = EelContext.factory()
            .withPrecision(config.precision())
            .withTimeout(config.timeout())
            .withIoLimit(config.ioLimit())
            .withStartOfWeek(config.startOfWeek())
            .withMinimalDaysInFirstWeek(config.daysInFirstWeek());

        for (var function : config.functionList()) {
            builder = builder.withUdfClass(function);
        }

        for (var functionPackage : config.packageList()) {
            builder = builder.withUdfPackage(functionPackage);
        }

        return builder.build();
    }

    @Nonnull
    private SymbolsTable createSymbolsTable() {
        String defaultValue = config.defaultValue();

        return Stream.of(SymbolsTable.factory())
            .map(f -> config.useProperties() ? f.withProperties() : f)
            .map(f -> config.useEnvironmentVariables() ? f.withEnvironment() : f)
            .map(f -> f.withValues(config.definitions()))
            .map(f -> defaultValue != null ? f.withDefault(defaultValue) : f)
            .findFirst()
            .get()
            .build();
    }

    @VisibleForTesting
    @Nonnull
    static State execute(@Nonnull String[] args) {
        Config config = Config.parse(args);
        State state;

        if (config.requestHelp()) {
            config.displayUsage();
            state = State.HELP;
        } else if (config.requestVersion()) {
            state = new Evaluate(config).showVersion();
        } else if (config.isValid()) {
            state = new Evaluate(config).run();
        } else {
            state = State.BAD_COMMAND_LINE;
        }

        return state;
    }

    /**
     * Application entry point
     * @param args  Command line arguments. Pass {@literal --help} to get a full help page
     */
    public static void main(@Nonnull String[] args) {
        System.exit(execute(args).getReturnCode());
    }
}
