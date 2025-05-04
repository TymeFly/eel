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
 * The Extensible Expression Language (EEL) Entry point.
 */
public class Eel {
    /** EEL Expression builder implementation */
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
            Preconditions.checkNotNull(expression, "Can not set a parse a null expression");

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
     * Public entry point for reading Eel language metadata
     * @return Eel language metadata
     * @since 2.0.0
     */
    @Nonnull
    public static Metadata metadata() {
        return BuildTime.getInstance();
    }


    /**
     * Public entry point for creating an EEL expression with a custom context
     * @return              A fluent interface
     * @see #compile(String)
     */
    @Nonnull
    public static EelBuilder factory() {
        return new EelBuilderImpl();
    }


    /**
     * Public entry point for an Expression with a default, shared, context
     * @param expression    The expression to be evaluated
     * @return              A generated expression
     * @see #factory() 
     */
    @Nonnull
    public static Eel compile(@Nonnull String expression) {
        return compile(DEFAULT_CONTEXT, expression);
    }

    /**
     * Public entry point for an Expression with a default, shared, context
     * @param expression    The expression to be evaluated
     * @return              A generated expression
     * @see #factory()
     * @since 3.0.0
     */
    @Nonnull
    public static Eel compile(@Nonnull InputStream expression) {
        return compile(DEFAULT_CONTEXT, expression);
    }

    /**
     * Public entry point for Expression with a custom context
     * @param expression    The expression to be evaluated
     * @param context       A custom context
     * @return              A generated expression
     * @see #factory()
     */
    @Nonnull
    public static Eel compile(@Nonnull EelContext context, @Nonnull String expression) {
        Preconditions.checkNotNull(context, "Can not compile with a null context");
        Preconditions.checkNotNull(expression, "Can not set a parse a null expression");

        EelContextImpl contextImpl = (EelContextImpl) context;

        return new Eel(contextImpl, Source.build(expression, contextImpl.maxExpressionLength()));
    }

    /**
     * Public entry point for Expression with a custom context
     * @param expression    The expression to be evaluated
     * @param context       A custom context
     * @return              A generated expression
     * @see #factory()
     * @since 3.0.0
     */
    @Nonnull
    public static Eel compile(@Nonnull EelContext context, @Nonnull InputStream expression) {
        Preconditions.checkNotNull(context, "Can not compile with a null context");
        Preconditions.checkNotNull(expression, "Can not set a parse a null expression");

        EelContextImpl contextImpl = (EelContextImpl) context;

        return new Eel(contextImpl, Source.build(expression, contextImpl.maxExpressionLength()));
    }


    /**
     * Evaluate this expression without reference to a SymbolsTable
     * @return the result of evaluating this expression
     */
    @Nonnull
    public Result evaluate() {
        return expression.evaluate(SymbolsTable.EMPTY);
    }

    /**
     * Evaluate this expression using the supplied {@code symbolsTable}
     * @param symbolsTable  an object that can provide the expression with values
     * @return the result of evaluating this expression
     * @see SymbolsTable#factory()
     */
    @Nonnull
    public Result evaluate(@Nonnull SymbolsTable symbolsTable) {
        Preconditions.checkNotNull(symbolsTable, "Can not evaluate with a null symbolsTable");

        return expression.evaluate(symbolsTable);
    }

    /**
     * Evaluate this expression using the supplied {@code values} as an anonymous {@link SymbolsTable}
     * @param values    A collection of key-value pairs that will be used as the symbols table
     * @return the result of evaluating this expression
     * @see SymbolsTable#from(Map)
     * @see #evaluate(String, Map) 
     */
    @Nonnull
    public Result evaluate(@Nonnull Map<String, String> values) {
        return expression.evaluate(SymbolsTable.from(values));
    }

    /**
     * Evaluate this expression using the supplied {@code values} as a scoped {@link SymbolsTable} with the
     * {@code scopeName} and the {@link SymbolsTable#DEFAULT_DELIMITER}
     * @param scopeName Name of the scope for the map
     * @param values    A collection of key-value pairs that will be used as the symbols table
     * @return the result of evaluating this expression
     * @see SymbolsTable#from(Map)
     * @see #evaluate(Map)
     */
    @Nonnull
    public Result evaluate(@Nonnull String scopeName, @Nonnull Map<String, String> values) {
        return expression.evaluate(SymbolsTable.from(scopeName, values));
    }

    /**
     * Evaluate this expression using the supplied {@code lookup} function as an anonymous {@link SymbolsTable}
     * @param lookup    A lookup function that will be used as when accessing the symbols table
     * @return the result of evaluating this expression
     * @see SymbolsTable#from(Function) 
     * @see #evaluate(String, Function)
     */
    @Nonnull
    public Result evaluate(@Nonnull Function<String, String> lookup) {
        return expression.evaluate(SymbolsTable.from(lookup));
    }

    /**
     * Evaluate this expression using the supplied {@code lookup} function as a scoped {@link SymbolsTable} with the
     * {@code scopeName} and the {@link SymbolsTable#DEFAULT_DELIMITER}
     * @param scopeName Name of the scope for lookup function
     * @param lookup    A lookup function that will be used as when accessing the symbols table
     * @return the result of evaluating this expression
     * @see SymbolsTable#from(Function)
     * @see #evaluate(Function)
     */
    @Nonnull
    public Result evaluate(@Nonnull String scopeName, @Nonnull Function<String, String> lookup) {
        return expression.evaluate(SymbolsTable.from(scopeName, lookup));
    }

    /**
     * Evaluate this expression using the system properties as an anonymous {@link SymbolsTable}
     * @return the result of evaluating this expression
     * @see SymbolsTable#fromEnvironment() 
     * @see #evaluateEnvironment(String) 
     */
    @Nonnull
    public Result evaluateEnvironment() {
        return expression.evaluate(SymbolsTable.fromEnvironment());
    }

    /**
     * Evaluate this expression using the system properties as a scoped {@link SymbolsTable} with the
     * {@code scopeName} and the {@link SymbolsTable#DEFAULT_DELIMITER}
     * @param scopeName Name of the scope for the environment variables
     * @return the result of evaluating this expression
     * @see SymbolsTable#fromEnvironment()
     * @see #evaluateEnvironment()
     */
    @Nonnull
    public Result evaluateEnvironment(@Nonnull String scopeName) {
        return expression.evaluate(SymbolsTable.fromEnvironment(scopeName));
    }

    /**
     * Evaluate this expression using the system properties as an anonymous {@link SymbolsTable}
     * @return the result of evaluating this expression
     * @see SymbolsTable#fromProperties() 
     * @see #evaluateProperties(String)
     */
    @Nonnull
    public Result evaluateProperties() {
        return expression.evaluate(SymbolsTable.fromProperties());
    }
    
    /**
     * Evaluate this expression using the system properties as a scoped {@link SymbolsTable} with the
     * {@code scopeName} and the {@link SymbolsTable#DEFAULT_DELIMITER}
     * @param scopeName Name of the scope for the properties
     * @return the result of evaluating this expression
     * @see SymbolsTable#fromProperties()
     * @see #evaluateProperties(String)
     */
    @Nonnull
    public Result evaluateProperties(@Nonnull String scopeName) {
        return expression.evaluate(SymbolsTable.fromProperties(scopeName));
    }

    /**
     * Evaluate this expression using the supplied {@code defaultValue} for all values in the {@code symbolsTable}
     * without a scope name
     * @param defaultValue  A string that will be used for all values ae could be in the symbols table
     * @return the result of evaluating this expression
     * @see SymbolsTable#from(String) 
     */
    @Nonnull
    public Result evaluate(@Nonnull String defaultValue) {
        return expression.evaluate(SymbolsTable.from(defaultValue));
    }
}
