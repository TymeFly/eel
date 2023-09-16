package com.github.tymefly.eel;


import java.io.InputStream;
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
            if (context instanceof EelContextImpl contextImpl) {
                this.context = contextImpl;
            } else {
                throw new IllegalStateException("Invalid EelContext");
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
            Source source = new Source(expression, context.maxExpressionLength());

            return new Eel(context, source);
        }

        @Override
        @Nonnull
        public Eel compile(@Nonnull InputStream expression) {
            Preconditions.checkNotNull(expression, "Can not parse a null input stream");

            resolveContext();
            Source source = new Source(expression, context.maxExpressionLength());

            return new Eel(context, source);
        }


        private void resolveContext() {
            if (this.context == null) {
                context = contextBuilder.build();
            }
        }
    }


    private static final EelContextImpl DEFAULT_CONTEXT = new EelContextImpl.Builder().build();

    private final Executor expression;


    private Eel(@Nonnull EelContextImpl context, @Nonnull Source source) {
        Tokenizer tokenizer = new Tokenizer(source);
        LambdaCompiler compiler = new LambdaCompiler(context);
        Parser parser = new Parser(context, tokenizer, compiler);

        this.expression = new EelRuntime(context)
            .apply(parser.parse());
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
     * Public entry point for Expression with a shared custom context
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

        return new Eel(contextImpl, new Source(expression, contextImpl.maxExpressionLength()));
    }


    /**
     * Evaluate this expression without referencing the supplied {@code symbolsTable} for variables
     * @param symbolsTable  an object that can provide the expression with values
     * @return the result of evaluating this expression
     * @see SymbolsTable#factory()
     */
    @Nonnull
    public Result evaluate(@Nonnull SymbolsTable symbolsTable) {
        Preconditions.checkNotNull(symbolsTable, "Can not evaluate with a null symbolsTable");

        return expression.execute(symbolsTable);
    }

    /**
     * Evaluate this expression using the supplied {@code values} for the  {@code symbolsTable}
     * @param values    A collection of key-value pairs that will be used as the symbols table
     * @return the result of evaluating this expression
     * @see SymbolsTable#from(Map) 
     */
    @Nonnull
    public Result evaluate(@Nonnull Map<String, String> values) {
        return expression.execute(SymbolsTable.from(values));
    }

    /**
     * Evaluate this expression using the supplied {@code lookup} function for the {@code symbolsTable}
     * @param lookup    A lookup function that will be used as when accessing the symbols table
     * @return the result of evaluating this expression
     * @see SymbolsTable#from(Function) 
     */
    @Nonnull
    public Result evaluate(@Nonnull Function<String, String> lookup) {
        return expression.execute(SymbolsTable.from(lookup));
    }

    /**
     * Evaluate this expression using the system properties for the {@code symbolsTable}
     * @return the result of evaluating this expression
     * @see SymbolsTable#fromEnvironment() 
     */
    @Nonnull
    public Result evaluateEnvironment() {
        return expression.execute(SymbolsTable.fromEnvironment());
    }

    /**
     * Evaluate this expression using the system properties for the {@code symbolsTable}
     * @return the result of evaluating this expression
     * @see SymbolsTable#fromProperties() 
     */
    @Nonnull
    public Result evaluateProperties() {
        return expression.execute(SymbolsTable.fromProperties());
    }

    /**
     * Evaluate this expression using the supplied {@code defaultValue} for all values in the {@code symbolsTable}
     * @param defaultValue  A string that will be used for all values ae could be in the symbols table
     * @return the result of evaluating this expression
     * @see SymbolsTable#from(String) 
     */
    @Nonnull
    public Result evaluate(@Nonnull String defaultValue) {
        return expression.execute(SymbolsTable.from(defaultValue));
    }

    /**
     * Evaluate this expression without reference to a SymbolsTable
     * @return the result of evaluating this expression
     */
    @Nonnull
    public Result evaluate() {
        return expression.execute(SymbolsTable.EMPTY);
    }
}
