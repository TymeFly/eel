package com.github.tymefly.eel;

import java.util.List;

import javax.annotation.Nonnull;

/**
 * Defines the contract of a Compiler in the EEL language. The root operations are
 * {@link #read(String)}, {@link #textConstant(String)}, {@link #numericConstant(Number)} and
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
        SymbolBuilder withDefault(@Nonnull Term defaultValue);

        @Nonnull
        SymbolBuilder withBlankDefault(@Nonnull Term defaultValue);

        @Nonnull
        SymbolBuilder withTransformation(@Nonnull SymbolTransformation transformation);

        @Nonnull
        Term build();
    }

    
    @Nonnull
    Term cached(@Nonnull Term term);


    @Nonnull
    Term isDefined(@Nonnull String identifier);

    @Nonnull
    SymbolBuilder read(@Nonnull String identifier);


    @Nonnull
    Term textConstant(@Nonnull String value);

    @Nonnull
    Term logicConstant(boolean value);

    @Nonnull
    Term numericConstant(@Nonnull Number value);


    @Nonnull
    Term conditional(@Nonnull Term condition, @Nonnull Term first, @Nonnull Term second);


    @Nonnull
    Term equal(@Nonnull Term left, @Nonnull Term right);

    @Nonnull
    Term notEqual(@Nonnull Term left, @Nonnull Term right);

    @Nonnull
    Term greaterThan(@Nonnull Term left, @Nonnull Term right);

    @Nonnull
    Term greaterThenEquals(@Nonnull Term left, @Nonnull Term right);

    @Nonnull
    Term lessThan(@Nonnull Term left, @Nonnull Term right);

    @Nonnull
    Term lessThanEquals(@Nonnull Term left, @Nonnull Term right);

    @Nonnull
    Term isBefore(@Nonnull Term left, @Nonnull Term right);

    @Nonnull
    Term isAfter(@Nonnull Term left, @Nonnull Term right);

    @Nonnull
    Term in(@Nonnull Term left, @Nonnull List<Term> terms);


    @Nonnull
    Term negate(@Nonnull Term value);

    @Nonnull
    Term add(@Nonnull Term left, @Nonnull Term right);

    @Nonnull
    Term subtract(@Nonnull Term left, @Nonnull Term right);

    @Nonnull
    Term multiply(@Nonnull Term left, @Nonnull Term right);

    @Nonnull
    Term divide(@Nonnull Term left, @Nonnull Term right);

    @Nonnull
    Term divideFloor(@Nonnull Term left, @Nonnull Term right);

    @Nonnull
    Term divideTruncate(@Nonnull Term left, @Nonnull Term right);

    @Nonnull
    Term modulus(@Nonnull Term left, @Nonnull Term right);

    @Nonnull
    Term power(@Nonnull Term left, @Nonnull Term right);


    @Nonnull
    Term logicalNot(@Nonnull Term value);

    @Nonnull
    Term logicalAnd(@Nonnull Term left, @Nonnull Term right);

    @Nonnull
    Term logicalOr(@Nonnull Term left, @Nonnull Term right);

    @Nonnull
    Term logicalXor(@Nonnull Term left, @Nonnull Term right);


    @Nonnull
    Term bitwiseNot(@Nonnull Term value);

    @Nonnull
    Term bitwiseAnd(@Nonnull Term left, @Nonnull Term right);

    @Nonnull
    Term bitwiseOr(@Nonnull Term left, @Nonnull Term right);

    @Nonnull
    Term bitwiseXor(@Nonnull Term left, @Nonnull Term right);

    @Nonnull
    Term leftShift(@Nonnull Term value, @Nonnull Term shift);

    @Nonnull
    Term rightShift(@Nonnull Term value, @Nonnull Term shift);


    @Nonnull
    Term concatenate(@Nonnull Term first, @Nonnull Term second);


    @Nonnull
    Term callText(@Nonnull Term operand);

    @Nonnull
    Term callNumber(@Nonnull Term operand);

    @Nonnull
    Term callLogic(@Nonnull Term operand);

    @Nonnull
    Term callDate(@Nonnull Term operand);
}
