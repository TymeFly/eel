package com.github.tymefly.eel;


import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.time.ZonedDateTime;
import java.util.List;

import javax.annotation.Nonnull;

import ch.obermuhlner.math.big.BigDecimalMath;
import com.github.tymefly.eel.exception.EelUnknownSymbolException;
import com.github.tymefly.eel.utils.BigDecimals;

/**
 * Class that creates code {@link Term} objects for the Parser.
 * The generated terms are optimised where ever possible.
 * @implNote Notes on optimisation:
 * 1. Constants are Terms in their own right.These can be returned directly from this class without needing
 *      an intermediate lambda function.
 * 2. Operations that act only on constants can be evaluated at compile time rather than run time.
 * 3. Some operations have special cases (such as adding 0 to a number) that can be optimised away
 * 4. Optimised Terms need to behave exactly the same as unoptimised terms. Therefore, if optimising a term throws
 *      an Exception (typically if a term cannot be converted to the expected type) it must be ignored. Instead, this
 *      class must generate an unoptimised term that will fail in the same way. This is important because Terms in EEL
 *      are lazily evaluated, and at runtime the exception might never be thrown.
 * 5. Checking a term to see if it's a constant will not throw an Exception, but checking to see if is a particular
 *      numeric, logical or date value might.
 * 6. If the complete expression can be optimised into a single constant term then the EelRuntime can evaluate it
 *      without needing to set up a timeout thread.
 */
class LambdaCompiler implements Compiler {
    private static class LambdaSymbolBuilder implements SymbolBuilder {
        private final String identifier;
        private boolean blankDefault;
        private Term defaultTerm;
        private SymbolTransformation transformations = SymbolTransformation.IDENTITY;

        private LambdaSymbolBuilder(@Nonnull String identifier) {
            this.identifier = identifier;
        }

        @Nonnull
        @Override
        public SymbolBuilder withDefault(@Nonnull Term defaultTerm) {
            this.defaultTerm = defaultTerm;
            this.blankDefault = false;

            return this;
        }

        @Nonnull
        @Override
        public SymbolBuilder withBlankDefault(@Nonnull Term defaultTerm) {
            this.defaultTerm = defaultTerm;
            this.blankDefault = true;

            return this;
        }

        @Nonnull
        @Override
        public SymbolBuilder withTransformation(@Nonnull SymbolTransformation transformation) {
            transformations = transformations.andThen(transformation);

            return this;
        }

        @Nonnull
        @Override
        public Term build() {
            Term result = s -> {
                String text = s.read(identifier);
                Value value;

                if ("".equals(text) && blankDefault) {
                    value = defaultTerm.evaluate(s);
                } else if (text != null) {
                    text = transformations.transform(s, text);
                    value = Constant.of(text);
                } else if (defaultTerm != null) {
                    value = defaultTerm.evaluate(s);
                } else {
                    throw new EelUnknownSymbolException("Unknown variable '%s'", identifier);
                }

                return value;
            };

            return result;
        }
    }

    private static class CachedTerm implements Term {
        private final Term term;
        private Value value;

        private CachedTerm(@Nonnull Term term) {
            this.term = term;
            this.value = null;
        }

        @Nonnull
        @Override
        public Value evaluate(@Nonnull SymbolsTable symbols) {
            if (value == null) {
                value = term.evaluate(symbols);
            }

            return value;
        }

        @Override
        public boolean isConstant() {
            return term.isConstant();
        }
    }



    private final EelContext context;

    LambdaCompiler(@Nonnull EelContext context) {
        this.context = context;
    }


                //*** Cached terms ***//


    @Nonnull
    @Override
    public Term cached(@Nonnull Term term) {
        // values from the Constants pool do not need to be cached
        return (term.isConstant() ? term : new CachedTerm(term));
    }


                //*** Variables ***//

    @Nonnull
    @Override
    public Term isDefined(@Nonnull String identifier) {
        Term result = s -> Constant.of(s.read(identifier) != null);

        return result;
    }

    @Nonnull
    @Override
    public SymbolBuilder read(@Nonnull String identifier) {
        return new LambdaSymbolBuilder(identifier);
    }

                //*** Constants ***//

    @Override
    @Nonnull
    public Term textConstant(@Nonnull String value) {
        return Constant.of(value);
    }

    @Override
    @Nonnull
    public Term logicConstant(boolean value) {
        return Constant.of(value);
    }

    @Override
    @Nonnull
    public Term numericConstant(@Nonnull Number value) {
        return Constant.of(value);
    }


                //*** ternary Op ***//

    @Override
    @Nonnull
    public Term conditional(@Nonnull Term condition, @Nonnull Term first, @Nonnull Term second) {
        Term result = null;

        if (condition.isConstant()) {
            try {
                result = condition.evaluate(SymbolsTable.EMPTY).asLogic() ? first : second;
            } catch (RuntimeException e) {
                // do nothing - to be consistent with unoptimised code, generate a term that will fail when evaluated
            }
        }

        if (result == null) {
            result = s -> condition.evaluate(s).asLogic() ? first.evaluate(s) : second.evaluate(s);
        }

        return result;
    }

                //*** Relational Ops ***//

    @Override
    @Nonnull
    public Term equal(@Nonnull Term left, @Nonnull Term right) {
        Term result;

        if (left.isConstant() && right.isConstant()) {
            result = Constant.of(equal(left, right, SymbolsTable.EMPTY));
        } else {
            result = s -> Constant.of(equal(left, right, s));
        }

        return result;
    }

    private boolean equal(@Nonnull Term left, @Nonnull Term right, @Nonnull SymbolsTable symbols) {
        boolean equal;
        Value leftValue = left.evaluate(symbols);
        Value rightValue = right.evaluate(symbols);
        Type leftType = leftValue.getType();
        Type rightType = rightValue.getType();

        if ((leftType == Type.TEXT) || (rightType == Type.TEXT)) {              // All values can be converted to text
            equal = leftValue.asText().equals(rightValue.asText());
        } else if ((leftType == Type.LOGIC) && (rightType == Type.LOGIC)) {
            equal = leftValue.asLogic() == rightValue.asLogic();
        } else {
            /*
                Converting Dates to Numbers returns a numeric offset from UTC. As a consequence two dates are
                considered equal if they represent the same instant even if they are in different time zones.
             */

            equal = BigDecimals.eq(leftValue.asNumber(), rightValue.asNumber());
        }

        return equal;
    }

    @Override
    @Nonnull
    public Term notEqual(@Nonnull Term left, @Nonnull Term right) {
        Term result;

        if (left.isConstant() && right.isConstant()) {
            result = Constant.of(!equal(left, right, SymbolsTable.EMPTY));
        } else {
            result = s -> Constant.of(!equal(left, right, s));
        }

        return result;
    }

    @Override
    @Nonnull
    public Term greaterThan(@Nonnull Term left, @Nonnull Term right) {
        Term result = null;

        if (left.isConstant() && right.isConstant()) {
            try {
                result = Constant.of(greaterThan(left, right, SymbolsTable.EMPTY));
            } catch (RuntimeException e) {
                // do nothing - to be consistent with unoptimised code, generate a term that will fail when evaluated
            }
        }

        if (result == null) {
            result = s -> Constant.of(greaterThan(left, right, s));
        }

        return result;
    }

    private boolean greaterThan(@Nonnull Term left, @Nonnull Term right, @Nonnull SymbolsTable symbols) {
        BigDecimal leftValue = left.evaluate(symbols).asNumber();
        BigDecimal rightValue = right.evaluate(symbols).asNumber();

        return BigDecimals.gt(leftValue, rightValue);
    }


    @Override
    @Nonnull
    public Term lessThan(@Nonnull Term left, @Nonnull Term right) {
        Term result = null;

        if (left.isConstant() && right.isConstant()) {
            try {
                result = Constant.of(lessThan(left, right, SymbolsTable.EMPTY));
            } catch (RuntimeException e) {
                // do nothing - to be consistent with unoptimised code, generate a term that will fail when evaluated
            }
        }

        if (result == null) {
            result = s -> Constant.of(lessThan(left, right, s));
        }

        return result;
    }

    private boolean lessThan(@Nonnull Term left, @Nonnull Term right, @Nonnull SymbolsTable symbols) {
        BigDecimal leftValue = left.evaluate(symbols).asNumber();
        BigDecimal rightValue = right.evaluate(symbols).asNumber();

        return BigDecimals.lt(leftValue, rightValue);
    }

    @Override
    @Nonnull
    public Term greaterThenEquals(@Nonnull Term left, @Nonnull Term right) {
        Term result = null;

        if (left.isConstant() && right.isConstant()) {
            try {
                result = Constant.of(!lessThan(left, right, SymbolsTable.EMPTY));
            } catch (RuntimeException e) {
                // do nothing - to be consistent with unoptimised code, generate a term that will fail when evaluated
            }
        }

        if (result == null) {
            result = s -> Constant.of(!lessThan(left, right, s));
        }

        return result;
    }

    @Override
    @Nonnull
    public Term lessThanEquals(@Nonnull Term left, @Nonnull Term right) {
        Term result = null;

        if (left.isConstant() && right.isConstant()) {
            try {
                result = Constant.of(!greaterThan(left, right, SymbolsTable.EMPTY));
            } catch (RuntimeException e) {
                // do nothing - to be consistent with unoptimised code, generate a term that will fail when evaluated
            }
        }

        if (result == null) {
            result = s -> Constant.of(!greaterThan(left, right, s));
        }

        return result;
    }

    @Nonnull
    @Override
    public Term isBefore(@Nonnull Term left, @Nonnull Term right) {
        Term result = null;

        if (left.isConstant() && right.isConstant()) {
            try {
                result = isBefore(left, right, SymbolsTable.EMPTY);
            } catch (RuntimeException e) {
                // do nothing - to be consistent with unoptimised code, generate a term that will fail when evaluated
            }
        }

        if (result == null) {
            result = s -> isBefore(left, right, s);
        }

        return result;
    }

    @Nonnull
    private Constant isBefore(@Nonnull Term left, @Nonnull Term right, @Nonnull SymbolsTable symbols) {
        ZonedDateTime leftValue = left.evaluate(symbols).asDate();
        ZonedDateTime rightValue = right.evaluate(symbols).asDate();

        return Constant.of(leftValue.isBefore(rightValue));
    }

    @Nonnull
    @Override
    public Term isAfter(@Nonnull Term left, @Nonnull Term right) {
        Term result = null;

        if (left.isConstant() && right.isConstant()) {
            try {
                result = isAfter(left, right, SymbolsTable.EMPTY);
            } catch (RuntimeException e) {
                // do nothing - to be consistent with unoptimised code, generate a term that will fail when evaluated
            }
        }

        if (result == null) {
            result = s -> isAfter(left, right, s);
        }

        return result;
    }

    @Nonnull
    private Constant isAfter(@Nonnull Term left, @Nonnull Term right, @Nonnull SymbolsTable symbols) {
        ZonedDateTime leftValue = left.evaluate(symbols).asDate();
        ZonedDateTime rightValue = right.evaluate(symbols).asDate();

        return Constant.of(leftValue.isAfter(rightValue));
    }


    @Nonnull
    @Override
    public Term in(@Nonnull Term left, @Nonnull List<Term> terms) {
        // Optimising this is an O(n) operation as we will need to check of the terms are all constants
        return s -> {
            boolean found = false;

            for (var term : terms) {
                found = equal(left, term, s);

                if (found) {
                    break;
                }
            }

            return Constant.of(found);
        };
    }

                //*** Numeric Ops ***//

    @Override
    @Nonnull
    public Term negate(@Nonnull Term value) {
        Term result = null;

        if (value.isConstant()) {
            try {
                result = negate(value, SymbolsTable.EMPTY);
            } catch (RuntimeException e) {
                // do nothing - to be consistent with unoptimised code, generate a term that will fail when evaluated
            }
        }

        if (result == null) {
            result = s -> negate(value, s);
        }

        return result;
    }

    @Nonnull
    private Constant negate(@Nonnull Term value, @Nonnull SymbolsTable symbols) {
        return Constant.of(value.evaluate(symbols).asNumber().negate());
    }


    @Override
    @Nonnull
    public Term add(@Nonnull Term left, @Nonnull Term right) {
        // Optimise: adding a constant zero to a number always returns the original number
        Term result = null;

        try {
            if (is(left, BigDecimal.ZERO)) {
                result = right;
            } else if (is(right, BigDecimal.ZERO)) {
                result = left;
            } else if (left.isConstant() && right.isConstant()) {
                result = add(left, right, SymbolsTable.EMPTY);
            } else {
                // Do nothing - the following check will assign the correct value
            }
        } catch (RuntimeException e) {
            // do nothing - to be consistent with unoptimised code, generate a term that will fail when evaluated
        }

        if (result == null) {
            result = s -> add(left, right, s);
        }

        return result;
    }

    @Nonnull
    private Constant add(@Nonnull Term left, @Nonnull Term right, @Nonnull SymbolsTable symbols) {
        BigDecimal leftValue = left.evaluate(symbols).asNumber();
        BigDecimal rightValue = right.evaluate(symbols).asNumber();

        return Constant.of(leftValue.add(rightValue));
    }

    @Override
    @Nonnull
    public Term subtract(@Nonnull Term left, @Nonnull Term right) {
        // Optimise: subtracting a constant zero from a number always returns the original number

        Term result = null;

        try {
            if (is(right, BigDecimal.ZERO)) {
                result = left;
            } else if (left.isConstant() && right.isConstant()) {
                result = subtract(left, right, SymbolsTable.EMPTY);
            } else {
                // Do nothing - the following check will assign the correct value
            }
        } catch (RuntimeException e) {
            // do nothing - to be consistent with unoptimised code, generate a term that will fail when evaluated
        }

        if (result == null) {
            result = s -> subtract(left, right, s);
        }

        return result;
    }

    @Nonnull
    private Constant subtract(@Nonnull Term left, @Nonnull Term right, @Nonnull SymbolsTable symbols) {
        BigDecimal leftValue = left.evaluate(symbols).asNumber();
        BigDecimal rightValue = right.evaluate(symbols).asNumber();

        return Constant.of(leftValue.subtract(rightValue));
    }

    @Override
    @Nonnull
    public Term multiply(@Nonnull Term left, @Nonnull Term right) {
        // Optimise: multiplying by a constant one always returns the original number

        Term result = null;

        try {
            if (is(left, BigDecimal.ONE)) {
                result = right;
            } else if (is(right, BigDecimal.ONE)) {
                result = left;
            } else if (left.isConstant() && right.isConstant()) {
                result =  multiply(left, right, SymbolsTable.EMPTY);
            } else {
                // Do nothing - the following check will assign the correct value
            }
        } catch (RuntimeException e) {
            // do nothing - to be consistent with unoptimised code, generate a term that will fail when evaluated
        }

        if (result == null) {
            result = s -> multiply(left, right, s);
        }

        return result;
    }

    @Nonnull
    private Constant multiply(@Nonnull Term left, @Nonnull Term right, @Nonnull SymbolsTable symbols) {
        BigDecimal leftValue = left.evaluate(symbols).asNumber();
        BigDecimal rightValue = right.evaluate(symbols).asNumber();

        return Constant.of(leftValue.multiply(rightValue));
    }

    @Override
    @Nonnull
    public Term divide(@Nonnull Term left, @Nonnull Term right) {
        // Optimise: dividing by a constant one always returns the original number

        Term result = null;

        try {
            if (is(right, BigDecimal.ONE)) {
                result = left;
            } else if (left.isConstant() && right.isConstant()) {
                result = divide(left, right, SymbolsTable.EMPTY);
            } else {
                // Do nothing - the following check will assign the correct value
            }
        } catch (RuntimeException e) {
            // do nothing - to be consistent with unoptimised code, generate a term that will fail when evaluated
        }

        if (result == null) {
            result = s -> divide(left, right, s);
        }

        return result;
    }

    @Nonnull
    private Constant divide(@Nonnull Term left, @Nonnull Term right, @Nonnull SymbolsTable symbols) {
        BigDecimal leftValue = left.evaluate(symbols).asNumber();
        BigDecimal rightValue = right.evaluate(symbols).asNumber();
        BigDecimal result = leftValue.divide(rightValue, context.getMathContext());

        return Constant.of(result);
    }

    @Nonnull
    @Override
    public Term divideFloor(@Nonnull Term left, @Nonnull Term right) {
        // Can't optimise dividing by constant one as the result changes scale

        Term result = null;

        if (left.isConstant() && right.isConstant()) {
            try {
                result = divideHelper(left, right, SymbolsTable.EMPTY, RoundingMode.FLOOR);
            } catch (RuntimeException e) {
                // do nothing - to be consistent with unoptimised code, generate a term that will fail when evaluated
            }
        }

        if (result == null) {
            result = s -> divideHelper(left, right, s, RoundingMode.FLOOR);
        }

        return result;
    }

    @Nonnull
    @Override
    public Term divideTruncate(@Nonnull Term left, @Nonnull Term right) {
        // Can't optimise dividing by constant one as the result changes scale

        Term result = null;

        if (left.isConstant() && right.isConstant()) {
            try {
                result = divideHelper(left, right, SymbolsTable.EMPTY, RoundingMode.DOWN);
            } catch (RuntimeException e) {
                // do nothing - to be consistent with unoptimised code, generate a term that will fail when evaluated
            }
        }

        if (result == null) {
            result = s -> divideHelper(left, right, s, RoundingMode.DOWN);
        }

        return result;
    }

    @Nonnull
    private Constant divideHelper(@Nonnull Term left,
                                  @Nonnull Term right,
                                  @Nonnull SymbolsTable symbols,
                                  @Nonnull RoundingMode mode) {
        BigDecimal leftValue = left.evaluate(symbols).asNumber();
        BigDecimal rightValue = right.evaluate(symbols).asNumber();
        BigDecimal quotient = leftValue.divide(rightValue, context.getMathContext())
            .setScale(0, mode);

        return Constant.of(quotient);
    }


    @Override
    @Nonnull
    public Term modulus(@Nonnull Term left, @Nonnull Term right) {
        Term result = null;

        if (left.isConstant() && right.isConstant()) {
            try {
                result = modulus(left, right, SymbolsTable.EMPTY);
            } catch (RuntimeException e) {
                // do nothing - to be consistent with unoptimised code, generate a term that will fail when evaluated
            }
        }

        if (result == null) {
            result = s -> modulus(left, right, s);
        }

        return result;
    }

    @Nonnull
    private Constant modulus(@Nonnull Term left, @Nonnull Term right, @Nonnull SymbolsTable symbols) {
        BigInteger leftValue = left.evaluate(symbols).asBigInteger();
        BigInteger rightValue = right.evaluate(symbols).asBigInteger();

        return Constant.of(leftValue.mod(rightValue));
    }

    @Override
    @Nonnull
    public Term power(@Nonnull Term left, @Nonnull Term right) {
        // Optimise: Raising a number to the power of a constant one always returns the original number

        Term result = null;

        try {
            if (is(right, BigDecimal.ONE)) {
                result = left;
            } else if (left.isConstant() && right.isConstant()) {
                result = power(left, right, SymbolsTable.EMPTY);
            } else {
                // Do nothing - the following check will assign the correct value
            }
        } catch (RuntimeException e) {
            // do nothing - to be consistent with unoptimised code, generate a term that will fail when evaluated
        }

        if (result == null) {
            result = s -> power(left, right, s);
        }

        return result;
    }

    @Nonnull
    private Constant power(@Nonnull Term left, @Nonnull Term right, @Nonnull SymbolsTable symbols) {
        BigDecimal leftValue = left.evaluate(symbols).asNumber();
        BigDecimal rightValue = right.evaluate(symbols).asNumber();
        BigDecimal result = BigDecimalMath.pow(leftValue, rightValue, context.getMathContext());

        return Constant.of(result);
    }


                //*** Logic Ops ***//

    @Override
    @Nonnull
    public Term logicalNot(@Nonnull Term value) {
        Term result = null;

        if (value.isConstant()) {
            try {
                result = logicalNot(value, SymbolsTable.EMPTY);
            } catch (RuntimeException e) {
                // do nothing - to be consistent with unoptimised code, generate a term that will fail when evaluated
            }
        }

        if (result == null) {
            result = s -> logicalNot(value, s);
        }

        return result;
    }

    @Nonnull
    private Constant logicalNot(@Nonnull Term value, @Nonnull SymbolsTable symbols) {
        return Constant.of(!value.evaluate(symbols).asLogic());
    }

    @Override
    @Nonnull
    public Term logicalAnd(@Nonnull Term left, @Nonnull Term right) {
        // Optimise: false and anything will short circuit to false
        // but...  : anything and false may generate an error from first term

        Term result = null;

        try {
            if (is(left, false)) {
                result = left;
            } else if (left.isConstant() && right.isConstant()) {
                result = logicalAnd(left, right, SymbolsTable.EMPTY);
            } else {
                // Do nothing - the following check will assign the correct value
            }
        } catch (RuntimeException e) {
            // do nothing - to be consistent with unoptimised code, generate a term that will fail when evaluated
        }

        if (result == null) {
            result = s -> logicalAnd(left, right, s);
        }

        return result;
    }

    @Nonnull
    private Constant logicalAnd(@Nonnull Term left, @Nonnull Term right, @Nonnull SymbolsTable symbols) {
        return Constant.of(left.evaluate(symbols).asLogic() && right.evaluate(symbols).asLogic());
    }

    @Override
    @Nonnull
    public Term logicalOr(@Nonnull Term left, @Nonnull Term right) {
        // Optimise: true or anything will short circuit to true
        // but...  : anything or true may generate an error from first term

        Term result = null;

        try {
            if (is(left, true)) {
                result = left;
            } else if (left.isConstant() && right.isConstant()) {
                result = logicalOr(left, right, SymbolsTable.EMPTY);
            } else {
                // Do nothing - the following check will assign the correct value
            }
        } catch (RuntimeException e) {
            // do nothing - to be consistent with unoptimised code, generate a term that will fail when evaluated
        }

        if (result == null) {
            result = s -> logicalOr(left, right, s);
        }

        return result;
    }

    @Nonnull
    private Constant logicalOr(@Nonnull Term left, @Nonnull Term right, @Nonnull SymbolsTable symbols) {
        return Constant.of(left.evaluate(symbols).asLogic() || right.evaluate(symbols).asLogic());
    }

    @Override
    @Nonnull
    public Term logicalXor(@Nonnull Term left, @Nonnull Term right) {
        Term result = null;

        if (left.isConstant() && right.isConstant()) {
            try {
                result = logicalXor(left, right, SymbolsTable.EMPTY);
            } catch (RuntimeException e) {
                // do nothing - to be consistent with unoptimised code, generate a term that will fail when evaluated
            }
        }

        if (result == null) {
            result = s -> logicalXor(left, right, s);
        }

        return result;
    }

    @Nonnull
    private Constant logicalXor(@Nonnull Term left, @Nonnull Term right, @Nonnull SymbolsTable symbols) {
        return Constant.of(left.evaluate(symbols).asLogic() ^ right.evaluate(symbols).asLogic());
    }

                    //*** Bitwise Ops ***//

    @Override
    @Nonnull
    public Term bitwiseNot(@Nonnull Term value) {
        Term result = null;

        if (value.isConstant()) {
            try {
                result = bitwiseNot(value, SymbolsTable.EMPTY);
            } catch (RuntimeException e) {
                // do nothing - to be consistent with unoptimised code, generate a term that will fail when evaluated
            }
        }

        if (result == null) {
            result = s -> bitwiseNot(value, s);
        }

        return result;
    }

    @Nonnull
    private Constant bitwiseNot(@Nonnull Term value, @Nonnull SymbolsTable symbols) {
        return Constant.of(value.evaluate(symbols).asBigInteger().not());
    }

    @Override
    @Nonnull
    public Term bitwiseAnd(@Nonnull Term left, @Nonnull Term right) {
        // Optimise: Bitwise AND isn't short-circuited => both terms always have to be evaluated

        Term result = null;

        if (left.isConstant() && right.isConstant()) {
            try {
                result = bitwiseAnd(left, right, SymbolsTable.EMPTY);
            } catch (RuntimeException e) {
                // do nothing - to be consistent with unoptimised code, generate a term that will fail when evaluated
            }
        }

        if (result == null) {
            result = s -> bitwiseAnd(left, right, s);
        }

        return result;
    }

    @Nonnull
    private Constant bitwiseAnd(@Nonnull Term left, @Nonnull Term right, @Nonnull SymbolsTable symbols) {
        BigInteger leftValue = left.evaluate(symbols).asBigInteger();
        BigInteger rightValue = right.evaluate(symbols).asBigInteger();

        return Constant.of(leftValue.and(rightValue));
    }

    @Override
    @Nonnull
    public Term bitwiseOr(@Nonnull Term left, @Nonnull Term right) {
        // Optimise: Bitwise OR isn't short-circuited => both terms always have to be evaluated

        Term result = null;

        if (left.isConstant() && right.isConstant()) {
            try {
                result = bitwiseOr(left, right, SymbolsTable.EMPTY);
            } catch (RuntimeException e) {
                // do nothing - to be consistent with unoptimised code, generate a term that will fail when evaluated
            }
        }

        if (result == null) {
            result = s -> bitwiseOr(left, right, s);
        }

        return result;
    }

    @Nonnull
    private Constant bitwiseOr(@Nonnull Term left, @Nonnull Term right, @Nonnull SymbolsTable symbols) {
        BigInteger leftValue = left.evaluate(symbols).asBigInteger();
        BigInteger rightValue = right.evaluate(symbols).asBigInteger();

        return Constant.of(leftValue.or(rightValue));
    }

    @Override
    @Nonnull
    public Term bitwiseXor(@Nonnull Term left, @Nonnull Term right) {
        Term result = null;

        if (left.isConstant() && right.isConstant()) {
            try {
                result = bitwiseXor(left, right, SymbolsTable.EMPTY);
            } catch (RuntimeException e) {
                // do nothing - to be consistent with unoptimised code, generate a term that will fail when evaluated
            }
        }

        if (result == null) {
            result = s -> bitwiseXor(left, right, s);
        }

        return result;
    }

    @Nonnull
    private Constant bitwiseXor(@Nonnull Term left, @Nonnull Term right, @Nonnull SymbolsTable symbols) {
        BigInteger leftValue = left.evaluate(symbols).asBigInteger();
        BigInteger rightValue = right.evaluate(symbols).asBigInteger();

        return Constant.of(leftValue.xor(rightValue));
    }

    @Override
    @Nonnull
    public Term leftShift(@Nonnull Term value, @Nonnull Term shift) {
        // Optimise: anything << 0 will return the original value
        // but...  : 0 << anything can't be optimised to 0 as anything could be invalid

        Term result = null;

        try {
            if (is(shift, BigDecimal.ZERO)) {
                result = value;
            } else if (value.isConstant() && shift.isConstant()) {
                result = leftShift(value, shift, SymbolsTable.EMPTY);
            } else {
                // Do nothing - the following check will assign the correct value
            }
        } catch (RuntimeException e) {
            // do nothing - to be consistent with unoptimised code, generate a term that will fail when evaluated
        }

        if (result == null) {
            result = s -> leftShift(value, shift, s);
        }

        return result;
    }

    @Nonnull
    private Constant leftShift(@Nonnull Term value, @Nonnull Term shift, @Nonnull SymbolsTable symbols) {
        BigInteger number = value.evaluate(symbols).asBigInteger();
        int by = shift.evaluate(symbols).asInt();

        return Constant.of(number.shiftLeft(by));
    }

    @Override
    @Nonnull
    public Term rightShift(@Nonnull Term value, @Nonnull Term shift) {
        // Optimise: anything >> 0 will return the original value
        // but...  : 0 >> anything can't be optimised to 0 as anything could be invalid

        Term result = null;

        try {
            if (is(shift, BigDecimal.ZERO)) {
                result = value;
            } else if (value.isConstant() && shift.isConstant()) {
                result = rightShift(value, shift, SymbolsTable.EMPTY);
            } else {
                // Do nothing - the following check will assign the correct value
            }
        } catch (RuntimeException e) {
            // do nothing - to be consistent with unoptimised code, generate a term that will fail when evaluated
        }

        if (result == null) {
            result = s -> rightShift(value, shift, s);
        }

        return result;
    }

    @Nonnull
    private Constant rightShift(@Nonnull Term value, @Nonnull Term shift, @Nonnull SymbolsTable symbols) {
        BigInteger number = value.evaluate(symbols).asBigInteger();
        int by = shift.evaluate(symbols).asInt();

        return Constant.of(number.shiftRight(by));
    }

                //*** String Ops ***//

    @Override
    @Nonnull
    public Term concatenate(@Nonnull Term first, @Nonnull Term second) {
        // Optimise: Concatenate any text by constant empty text always returns the original text

        Term result;

        if (isEmpty(first)) {
            result = second;
        } else if (isEmpty(second)) {
            result = first;
        } else if (first.isConstant() && second.isConstant()) {
            result = concatenate(first, second, SymbolsTable.EMPTY);
        } else {
            result = s -> concatenate(first, second, s);
        }

        return result;
    }

    @Nonnull
    private Constant concatenate(@Nonnull Term first, @Nonnull Term second, @Nonnull SymbolsTable symbols) {
        String firstValue = first.evaluate(symbols).asText();
        String secondValue = second.evaluate(symbols).asText();

        return Constant.of(firstValue + secondValue);
    }


    @Nonnull
    @Override
    public Term callText(@Nonnull Term value) {
        // Optimise: All values can be converted to text

        Term result;

        if (value.isConstant()) {
            result = callText(value, SymbolsTable.EMPTY);
        } else {
            result = s -> callText(value, s);
        }

        return result;
    }

    @Nonnull
    private static Constant callText(@Nonnull Term operand, @Nonnull SymbolsTable symbols) {
        String text = operand.evaluate(symbols).asText();

        return Constant.of(text);
    }


    @Nonnull
    @Override
    public Term callNumber(@Nonnull Term value) {
        Term result = null;

        if (value.isConstant()) {
            try {
                result = callNumber(value, SymbolsTable.EMPTY);
            } catch (RuntimeException e) {
                // do nothing - to be consistent with unoptimised code, generate a term that will fail when evaluated
            }
        }

        if (result == null) {
            result = s -> callNumber(value, s);
        }

        return result;
    }

    @Nonnull
    private static Constant callNumber(@Nonnull Term operand, @Nonnull SymbolsTable symbols) {
        BigDecimal number = operand.evaluate(symbols).asNumber();

        return Constant.of(number);
    }


    @Nonnull
    @Override
    public Term callLogic(@Nonnull Term value) {
        Term result = null;

        if (value.isConstant()) {
            try {
                result = callLogic(value, SymbolsTable.EMPTY);
            } catch (RuntimeException e) {
                // do nothing - to be consistent with unoptimised code, generate a term that will fail when evaluated
            }
        }

        if (result == null) {
            result = s -> callLogic(value, s);
        }

        return result;
    }

    @Nonnull
    private static Constant callLogic(@Nonnull Term operand, @Nonnull SymbolsTable symbols) {
        boolean logic = operand.evaluate(symbols).asLogic();

        return Constant.of(logic);
    }


    @Nonnull
    @Override
    public Term callDate(@Nonnull Term value) {
        Term result = null;

        if (value.isConstant()) {
            try {
                result = callDate(value, SymbolsTable.EMPTY);
            } catch (RuntimeException e) {
                // do nothing - to be consistent with unoptimised code, generate a term that will fail when evaluated
            }
        }

        if (result == null) {
            result = s -> callDate(value, s);
        }

        return result;
    }

    @Nonnull
    private static Constant callDate(@Nonnull Term operand, @Nonnull SymbolsTable symbols) {
        ZonedDateTime date = operand.evaluate(symbols).asDate();

        return Constant.of(date);
    }


                //*** Helper methods ***//

    private boolean isEmpty(@Nonnull Term value) {
        return (value.isConstant() && value.evaluate(SymbolsTable.EMPTY).asText().isEmpty());
    }

    private boolean is(@Nonnull Term value, boolean test) {
        return (value.isConstant() && (test == value.evaluate(SymbolsTable.EMPTY).asLogic()));
    }

    private boolean is(@Nonnull Term value, @Nonnull BigDecimal test) {
        return (value.isConstant() && test.equals(value.evaluate(SymbolsTable.EMPTY).asNumber()));
    }
}
