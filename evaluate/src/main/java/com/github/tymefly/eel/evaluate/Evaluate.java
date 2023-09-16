package com.github.tymefly.eel.evaluate;

import java.util.stream.Stream;

import javax.annotation.Nonnull;

import com.github.tymefly.eel.Eel;
import com.github.tymefly.eel.EelContext;
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
        State state;
        String source = config.getExpression();

        try {
            Result result = Eel.factory()
                .withContext(createContext())
                .compile(source)
                .evaluate(createSymbolsTable());

            String type = config.verbose() ? "[" + result.getType() + "] " : "";

            System.out.printf("%s%s%n", type, result.asText());

            state = State.EVALUATED;
        } catch (EelException e) {
            System.err.println("Failed to evaluate : " + source);
            e.printStackTrace();
            state = State.EXPRESSION_FAILED;
        }

        return state;
    }

    @Nonnull
    private EelContext createContext() {
        EelContextBuilder builder = EelContext.factory()
            .withPrecision(config.precision())
            .withTimeout(config.timeout());

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
