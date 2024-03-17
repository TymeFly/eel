package com.github.tymefly.eel;

import javax.annotation.Nonnull;

import com.github.tymefly.eel.udf.EelLambda;

/**
 * Implementation of {@link EelLambda}.
 */
class EelLambdaImpl implements EelLambda {
    private final Executor executor;
    private final SymbolsTable symbolsTable;


    EelLambdaImpl(@Nonnull Executor executor, @Nonnull SymbolsTable symbolsTable) {
        this.executor = executor;
        this.symbolsTable = symbolsTable;
    }


    @Nonnull
    @Override
    public EelValue get() {
        return executor.execute(symbolsTable);
    }
}
