package com.github.tymefly.eel;


import java.io.InputStream;
import java.time.DayOfWeek;
import java.time.Duration;
import java.util.Map;
import java.util.function.Function;

import javax.annotation.Nonnull;

import com.github.tymefly.eel.builder.EelBuilder;
import com.github.tymefly.eel.validate.Preconditions;

/**
 * Entry point for the Extensible Expression Language (EEL).
 */
public class Eel {
    /** EEL expression builder implementation. */
    private static class EelBuilderImpl implements EelBuilder {
        private final EelContextImpl.Builder contextBuilder;
        private EelContextImpl context;


        private EelBuilderImpl() {
            this.contextBuilder = new EelContextImpl.Builder();
        }


        @Override
        @Nonnull
        public EelBuilder withContext(@Nonnull EelContext context) {
            // Guaranteed to work: EelContext is a sealed interface with EelContextImpl as the sole implementation
            if (context instanceof EelContextImpl contextImpl) {
                this.context = contextImpl;
            } else {
                throw new IllegalStateException("Invalid EelContext " + context.getClass().getName());
            }

            return this;
        }

        @Nonnull
        @Override
        public EelBuilder withMaxExpressionSize(int maxLength) {
            contextBuilder.withMaxExpressionSize(maxLength);
            context = null;

            return this;
        }

        @Nonnull
        @Override
        public EelBuilder withTimeout(@Nonnull Duration timeout) {
            contextBuilder.withTimeout(timeout);
            context = null;

            return this;
        }

        @Nonnull
        @Override
        public EelBuilder withPrecision(int precision) {
            contextBuilder.withPrecision(precision);
            context = null;

            return this;
        }

        @Nonnull
        @Override
        public EelBuilder withIoLimit(int bytes) {
            contextBuilder.withIoLimit(bytes);
            context = null;

            return this;
        }

        @Override
        public EelBuilder withStartOfWeek(@Nonnull DayOfWeek startOfWeek) {
            contextBuilder.withStartOfWeek(startOfWeek);
            context = null;

            return this;
        }

        @Nonnull
        @Override
        public EelBuilder withMinimalDaysInFirstWeek(int minimalDaysInFirstWeek) {
            contextBuilder.withMinimalDaysInFirstWeek(minimalDaysInFirstWeek);
            context = null;

            return this;
        }

        @Nonnull
        @Override
        public EelBuilder withFileFactory(@Nonnull FileFactory factory) {
            Preconditions.checkNotNull(factory, "Can not set a null fileFactory");

            contextBuilder.withFileFactory(factory);
            context = null;

            return this;
        }

        @Override
        @Nonnull
        public EelBuilder withUdfPackage(@Nonnull Package location) {
            Preconditions.checkNotNull(location, "Can not set a null location");

            contextBuilder.withUdfPackage(location);
            context = null;

            return this;
        }

        @Override
        @Nonnull
        public EelBuilder withUdfClass(@Nonnull Class<?> udfClass) {
            Preconditions.checkNotNull(udfClass, "Can not set a null function");

            contextBuilder.withUdfClass(udfClass);
            context = null;

            return this;
        }

        @Override
        @Nonnull
        public Eel compile(@Nonnull String expression) {
            Preconditions.checkNotNull(expression, "Can not parse a null expression");
            resolveContext();
            Source source = Source.build(expression, context.maxExpressionLength());

            return new Eel(context, source);
        }

        @Override
        @Nonnull
        public Eel compile(@Nonnull InputStream expression) {
            Preconditions.checkNotNull(expression, "Can not parse a null input stream");

            resolveContext();
            Source source = Source.build(expression, context.maxExpressionLength());

            return new Eel(context, source);
        }

        private void resolveContext() {
            if (this.context == null) {
                context = contextBuilder.build();
            }
        }
    }


    private static final EelContextImpl DEFAULT_CONTEXT = new EelContextImpl.Builder().build();

    private final Expression expression;

    private Eel(@Nonnull EelContextImpl context, @Nonnull Source source) {
        Tokenizer tokenizer = new Tokenizer(source);
        LambdaCompiler compiler = new LambdaCompiler(context);
        Parser parser = new Parser(context, tokenizer, compiler);

        this.expression = new EelRuntime(context)
            .wrap(parser.parse());
    }

    /**
     * Returns metadata describing the EEL compiler.
     * @return the EEL language metadata
     * @since 2.0
     */
    @Nonnull
    public static Metadata metadata() {
        return BuildTime.getInstance();
    }


    /**
     * Creates a builder for constructing an EEL expression with a custom context.
     * @return a builder for configuring and compiling expressions
     * @see #compile(String)
     */
    @Nonnull
    public static EelBuilder factory() {
        return new EelBuilderImpl();
    }


    /**
     * Compiles an expression using the default, shared, context.
     * @param expression    the expression to compile
     * @return the compiled expression
     * @see #factory()
     */
    @Nonnull
    public static Eel compile(@Nonnull String expression) {
        return compile(DEFAULT_CONTEXT, expression);
    }

    /**
     * Compiles an expression from an input stream using the default, shared, context.
     * @param expression    the input stream containing the expression
     * @return the compiled expression
     * @see #factory()
     * @since 3.0
     */
    @Nonnull
    public static Eel compile(@Nonnull InputStream expression) {
        return compile(DEFAULT_CONTEXT, expression);
    }

    /**
     * Compiles an expression using a custom context.
     * @param context       A custom context
     * @param expression    the expression to compile
     * @return the compiled expression
     * @see #factory()
     */
    @Nonnull
    public static Eel compile(@Nonnull EelContext context, @Nonnull String expression) {
        Preconditions.checkNotNull(context, "Can not compile with a null context");
        Preconditions.checkNotNull(expression, "Can not parse a null expression");
        EelContextImpl contextImpl = (EelContextImpl) context;

        return new Eel(contextImpl, Source.build(expression, contextImpl.maxExpressionLength()));
    }

    /**
     * Compiles an expression from an input stream using a custom context.
     * @param context       A custom context
     * @param expression    the input stream containing the expression
     * @return the compiled expression
     * @see #factory()
     * @since 3.0
     */
    @Nonnull
    public static Eel compile(@Nonnull EelContext context, @Nonnull InputStream expression) {
        Preconditions.checkNotNull(context, "Can not compile with a null context");
        Preconditions.checkNotNull(expression, "Can not parse a null expression");
        EelContextImpl contextImpl = (EelContextImpl) context;

        return new Eel(contextImpl, Source.build(expression, contextImpl.maxExpressionLength()));
    }


    /**
     * Evaluates this expression without a SymbolsTable.
     * @return the result of the evaluation
     */
    @Nonnull
    public Result evaluate() {
        return expression.evaluate(SymbolsTable.EMPTY);
    }

    /**
     * Evaluates this expression using the provided {@code symbolsTable}.
     * @param symbolsTable  the Symbols table supplying values
     * @return the result of the evaluation
     * @see SymbolsTable#factory()
     */
    @Nonnull
    public Result evaluate(@Nonnull SymbolsTable symbolsTable) {
        Preconditions.checkNotNull(symbolsTable, "Can not evaluate with a null symbolsTable");

        return expression.evaluate(symbolsTable);
    }

    /**
     * Evaluates this expression using the provided {@code values} as an anonymous SymbolsTable.
     * @param values        key-value pairs used to resolve symbols
     * @return the result of the evaluation
     * @see SymbolsTable#from(Map)
     * @see #evaluate(String, Map)
     */
    @Nonnull
    public Result evaluate(@Nonnull Map<String, String> values) {
        return expression.evaluate(SymbolsTable.from(values));
    }

    /**
     * Evaluates this expression using the provided {@code values} as a scoped {@link SymbolsTable}.
     * @param scopeName     unique name of the scope
     * @param values        key-value pairs used to resolve symbols
     * @return the result of the evaluation
     * @see SymbolsTable#from(Map)
     * @see #evaluate(Map)
     */
    @Nonnull
    public Result evaluate(@Nonnull String scopeName, @Nonnull Map<String, String> values) {
        return expression.evaluate(SymbolsTable.from(scopeName, values));
    }

    /**
     * Evaluates this expression using the provided {@code lookup} function as an anonymous {@link SymbolsTable}.
     * @param lookup        function used to resolve symbol values
     * @return the result of the evaluation
     * @see SymbolsTable#from(Function)
     * @see #evaluate(String, Function)
     */
    @Nonnull
    public Result evaluate(@Nonnull Function<String, String> lookup) {
        return expression.evaluate(SymbolsTable.from(lookup));
    }

    /**
     * Evaluates this expression using the provided {@code lookup} function as a scoped {@link SymbolsTable}.
     * @param scopeName     unique name of the scope
     * @param lookup        function used to resolve symbol values
     * @return the result of the evaluation
     * @see SymbolsTable#from(Function)
     * @see #evaluate(Function)
     */
    @Nonnull
    public Result evaluate(@Nonnull String scopeName, @Nonnull Function<String, String> lookup) {
        return expression.evaluate(SymbolsTable.from(scopeName, lookup));
    }

    /**
     * Evaluates this expression using environment variables as an anonymous {@link SymbolsTable}.
     * @return the result of the evaluation
     * @see SymbolsTable#fromEnvironment()
     * @see #evaluateEnvironment(String)
     */
    @Nonnull
    public Result evaluateEnvironment() {
        return expression.evaluate(SymbolsTable.fromEnvironment());
    }

    /**
     * Evaluates this expression using environment variables as a scoped {@link SymbolsTable} with the
     * {@code scopeName} and the {@link SymbolsTable#DEFAULT_DELIMITER}
     * @param scopeName     unique name of the scope
     * @return the result of the evaluation
     * @see SymbolsTable#fromEnvironment()
     * @see #evaluateEnvironment()
     */
    @Nonnull
    public Result evaluateEnvironment(@Nonnull String scopeName) {
        return expression.evaluate(SymbolsTable.fromEnvironment(scopeName));
    }

    /**
     * Evaluates this expression using system properties as an anonymous {@link SymbolsTable}.
     * @return the result of the evaluation
     * @see SymbolsTable#fromProperties()
     * @see #evaluateProperties(String)
     */
    @Nonnull
    public Result evaluateProperties() {
        return expression.evaluate(SymbolsTable.fromProperties());
    }

    /**
     * Evaluates this expression using system properties as a scoped {@link SymbolsTable} with the
     * {@code scopeName} and the {@link SymbolsTable#DEFAULT_DELIMITER}
     * @param scopeName     unique name of the scope
     * @return the result of the evaluation
     * @see SymbolsTable#fromProperties()
     * @see #evaluateProperties(String)
     */
    @Nonnull
    public Result evaluateProperties(@Nonnull String scopeName) {
        return expression.evaluate(SymbolsTable.fromProperties(scopeName));
    }

    /**
     * Evaluates this expression using a {@code defaultValue} for all unresolved symbols.
     * @param defaultValue      the value returned when a symbol cannot be resolved
     * @return the result of the evaluation
     * @see SymbolsTable#from(String)
     */
    @Nonnull
    public Result evaluate(@Nonnull String defaultValue) {
        return expression.evaluate(SymbolsTable.from(defaultValue));
    }
}