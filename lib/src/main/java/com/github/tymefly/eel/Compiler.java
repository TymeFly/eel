package com.github.tymefly.eel;

import javax.annotation.Nonnull;

/**
 * Defines the contract of a Compiler in the EEL language. The root operations are
 * {@link #readSymbol(String)}, {@link #textConstant(String)}, {@link #numericConstant(Number)} and
 * {@link #logicConstant(boolean)}. The remaining operations are created by combining and encapsulating previously
 * generated operations.
 */
interface Compiler {
    /** Transformations on values read from the symbols table */
    @FunctionalInterface
    interface SymbolTransformation {
        SymbolTransformation IDENTITY = (s, t) -> t;
        SymbolTransformation LENGTH = (s, t) -> String.valueOf(t.length());

        @Nonnull
        String transform(@Nonnull SymbolsTable symbols, @Nonnull String text);

        @Nonnull
        default SymbolTransformation andThen(@Nonnull SymbolTransformation after) {
            return (SymbolsTable s, String t) -> after.transform(s, transform(s, t));
        }
    }

    /** Fluent interface used to read values from the Symbols Table. Multiple SymbolTransformation can be set */
    interface SymbolBuilder {
        @Nonnull
        SymbolBuilder withDefault(@Nonnull Executor defaultValue);

        @Nonnull
        SymbolBuilder withTransformation(@Nonnull SymbolTransformation transformation);

        @Nonnull
        Executor build();
    }


    @Nonnull
    Executor isDefined(@Nonnull String identifier);

    @Nonnull
    SymbolBuilder readSymbol(@Nonnull String identifier);


    @Nonnull
    Executor textConstant(@Nonnull String value);

    @Nonnull
    Executor logicConstant(boolean value);

    @Nonnull
    Executor numericConstant(@Nonnull Number value);


    @Nonnull
    Executor conditional(@Nonnull Executor condition, @Nonnull Executor first, @Nonnull Executor second);


    @Nonnull
    Executor equal(@Nonnull Executor left, @Nonnull Executor right);

    @Nonnull
    Executor notEqual(@Nonnull Executor left, @Nonnull Executor right);

    @Nonnull
    Executor greaterThan(@Nonnull Executor left, @Nonnull Executor right);

    @Nonnull
    Executor greaterThenEquals(@Nonnull Executor left, @Nonnull Executor right);

    @Nonnull
    Executor lessThan(@Nonnull Executor left, @Nonnull Executor right);

    @Nonnull
    Executor lessThanEquals(@Nonnull Executor left, @Nonnull Executor right);

    @Nonnull
    Executor isBefore(@Nonnull Executor left, @Nonnull Executor right);

    @Nonnull
    Executor isAfter(@Nonnull Executor left, @Nonnull Executor right);


    @Nonnull
    Executor negate(@Nonnull Executor value);

    @Nonnull
    Executor plus(@Nonnull Executor left, @Nonnull Executor right);

    @Nonnull
    Executor minus(@Nonnull Executor left, @Nonnull Executor right);

    @Nonnull
    Executor multiply(@Nonnull Executor left, @Nonnull Executor right);

    @Nonnull
    Executor divide(@Nonnull Executor left, @Nonnull Executor right);

    @Nonnull
    Executor divideFloor(@Nonnull Executor left, @Nonnull Executor right);

    @Nonnull
    Executor divideTruncate(@Nonnull Executor left, @Nonnull Executor right);

    @Nonnull
    Executor modulus(@Nonnull Executor left, @Nonnull Executor right);

    @Nonnull
    Executor power(@Nonnull Executor left, @Nonnull Executor right);


    @Nonnull
    Executor logicalNot(@Nonnull Executor value);

    @Nonnull
    Executor logicalAnd(@Nonnull Executor left, @Nonnull Executor right);

    @Nonnull
    Executor logicalOr(@Nonnull Executor left, @Nonnull Executor right);


    @Nonnull
    Executor bitwiseNot(@Nonnull Executor value);

    @Nonnull
    Executor bitwiseAnd(@Nonnull Executor left, @Nonnull Executor right);

    @Nonnull
    Executor bitwiseOr(@Nonnull Executor left, @Nonnull Executor right);

    @Nonnull
    Executor bitwiseXor(@Nonnull Executor left, @Nonnull Executor right);

    @Nonnull
    Executor leftShift(@Nonnull Executor value, @Nonnull Executor shift);

    @Nonnull
    Executor rightShift(@Nonnull Executor value, @Nonnull Executor shift);


    @Nonnull
    Executor concatenate(@Nonnull Executor first, @Nonnull Executor second);
}
