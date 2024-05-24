package com.github.tymefly.eel;


import java.math.BigDecimal;
import java.math.RoundingMode;

import javax.annotation.Nonnull;

import ch.obermuhlner.math.big.BigDecimalMath;
import com.github.tymefly.eel.exception.EelUnknownSymbolException;
import com.github.tymefly.eel.utils.BigDecimals;

/**
 * Class that creates {@link Executor} objects for the Parser.
 */
class LambdaCompiler implements Compiler {
    private static class LambdaSymbolBuilder implements SymbolBuilder {
        private final String identifier;
        private Executor defaultValue;
        private SymbolTransformation transformations = SymbolTransformation.IDENTITY;

        private LambdaSymbolBuilder(@Nonnull String identifier) {
            this.identifier = identifier;
        }

        @Nonnull
        @Override
        public SymbolBuilder withDefault(@Nonnull Executor defaultValue) {
            this.defaultValue = defaultValue;

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
        public Executor build() {
            Executor result = s -> {
                String text = s.read(identifier);
                Value value;

                if (text != null) {
                    text = transformations.transform(s, text);
                    value = Value.of(text);
                } else if (defaultValue != null) {
                    value = defaultValue.execute(s);
                } else {
                    throw new EelUnknownSymbolException("Unknown variable '%s'", identifier);
                }

                return value;
            };

            return result;
        }
    }


    private final EelContext context;


    LambdaCompiler(@Nonnull EelContext context) {
        this.context = context;
    }


                //*** Variables ***//

    @Nonnull
    @Override
    public Executor isDefined(@Nonnull String identifier) {
        Executor result = s -> Value.of(s.read(identifier) != null);

        return result;
    }

    @Nonnull
    @Override
    public SymbolBuilder readSymbol(@Nonnull String identifier) {
        return new LambdaSymbolBuilder(identifier);
    }

                //*** Constants ***//

    @Override
    @Nonnull
    public Executor textConstant(@Nonnull String value) {
        return s -> Value.of(value);
    }

    @Override
    @Nonnull
    public Executor logicConstant(boolean value) {
        return s -> Value.of(value);
    }

    @Override
    @Nonnull
    public Executor numericConstant(@Nonnull Number value) {
        return s -> Value.of(value);
    }


                //*** ternary Op ***//

    @Override
    @Nonnull
    public Executor conditional(@Nonnull Executor condition, @Nonnull Executor first, @Nonnull Executor second) {
        Executor result = s -> condition.execute(s).asLogic() ? first.execute(s) : second.execute(s);

        return result;
    }

                //*** Relational Ops ***//

    @Override
    @Nonnull
    public Executor equal(@Nonnull Executor left, @Nonnull Executor right) {
        return s -> Value.of(equal(left, right, s));
    }

    private boolean equal(@Nonnull Executor left, @Nonnull Executor right, @Nonnull SymbolsTable s) {
        boolean equal;
        Value leftValue = left.execute(s);
        Value rightValue = right.execute(s);
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
                This is required so that the =, <, <=, => and > operators all give consistent results.
             */

            equal = BigDecimals.eq(leftValue.asNumber(), rightValue.asNumber());
        }

        return equal;
    }

    @Override
    @Nonnull
    public Executor notEqual(@Nonnull Executor left, @Nonnull Executor right) {
        return s -> Value.of(!equal(left, right, s));
    }

    @Override
    @Nonnull
    public Executor greaterThan(@Nonnull Executor left, @Nonnull Executor right) {
        return s -> Value.of(BigDecimals.gt(left.execute(s).asNumber(), right.execute(s).asNumber()));
    }

    @Override
    @Nonnull
    public Executor greaterThenEquals(@Nonnull Executor left, @Nonnull Executor right) {
        return s -> Value.of(BigDecimals.ge(left.execute(s).asNumber(), right.execute(s).asNumber()));
    }

    @Override
    @Nonnull
    public Executor lessThan(@Nonnull Executor left, @Nonnull Executor right) {
        return s -> Value.of(BigDecimals.lt(left.execute(s).asNumber(), right.execute(s).asNumber()));
    }

    @Override
    @Nonnull
    public Executor lessThanEquals(@Nonnull Executor left, @Nonnull Executor right) {
        return s -> Value.of(BigDecimals.le(left.execute(s).asNumber(), right.execute(s).asNumber()));
    }

    @Nonnull
    @Override
    public Executor isBefore(@Nonnull Executor left, @Nonnull Executor right) {
        return s -> Value.of(left.execute(s).asDate().isBefore(right.execute(s).asDate()));
    }

    @Nonnull
    @Override
    public Executor isAfter(@Nonnull Executor left, @Nonnull Executor right) {
        return s -> Value.of(left.execute(s).asDate().isAfter(right.execute(s).asDate()));
    }

                //*** Numeric Ops ***//

    @Override
    @Nonnull
    public Executor negate(@Nonnull Executor value) {
        return s -> Value.of(value.execute(s).asNumber().negate());
    }


    @Override
    @Nonnull
    public Executor plus(@Nonnull Executor left, @Nonnull Executor right) {
        return s -> Value.of(left.execute(s).asNumber().add(right.execute(s).asNumber()));
    }

    @Override
    @Nonnull
    public Executor minus(@Nonnull Executor left, @Nonnull Executor right) {
        return s -> Value.of(left.execute(s).asNumber().subtract(right.execute(s).asNumber()));
    }

    @Override
    @Nonnull
    public Executor multiply(@Nonnull Executor left, @Nonnull Executor right) {
        return s -> Value.of(left.execute(s).asNumber().multiply(right.execute(s).asNumber()));
    }

    @Override
    @Nonnull
    public Executor divide(@Nonnull Executor left, @Nonnull Executor right) {
        return s -> Value.of(
            left.execute(s).asNumber().divide(right.execute(s).asNumber(), context.getMathContext()));
    }

    @Nonnull
    @Override
    public Executor divideFloor(@Nonnull Executor left, @Nonnull Executor right) {
        return s -> divideHelper(s, left, right, RoundingMode.FLOOR);
    }

    @Nonnull
    @Override
    public Executor divideTruncate(@Nonnull Executor left, @Nonnull Executor right) {
        return s -> divideHelper(s, left, right, RoundingMode.DOWN);
    }

    @Nonnull
    private Value divideHelper(@Nonnull SymbolsTable symbols,
                               @Nonnull Executor left,
                               @Nonnull Executor right,
                               @Nonnull RoundingMode mode) {
        BigDecimal divisor = right.execute(symbols)
            .asNumber();
        BigDecimal quotient = left.execute(symbols)
            .asNumber()
            .divide(divisor, context.getMathContext())
            .setScale(0, mode);
        Value result = Value.of(quotient);

        return result;
    }


    @Override
    @Nonnull
    public Executor modulus(@Nonnull Executor left, @Nonnull Executor right) {
        return s -> Value.of(left.execute(s).asBigInteger().mod(right.execute(s).asBigInteger()));
    }

    @Override
    @Nonnull
    public Executor power(@Nonnull Executor left, @Nonnull Executor right) {
        return s -> Value.of(
            BigDecimalMath.pow(left.execute(s).asNumber(), right.execute(s).asNumber(), context.getMathContext()));
    }


                //*** Logic Ops ***//

    @Override
    @Nonnull
    public Executor logicalNot(@Nonnull Executor value) {
        return s -> Value.of(!value.execute(s).asLogic());
    }

    @Override
    @Nonnull
    public Executor logicalAnd(@Nonnull Executor left, @Nonnull Executor right) {
        return s -> Value.of(left.execute(s).asLogic() && right.execute(s).asLogic());
    }

    @Override
    @Nonnull
    public Executor logicalOr(@Nonnull Executor left, @Nonnull Executor right) {
        return s -> Value.of(left.execute(s).asLogic() || right.execute(s).asLogic());
    }

                //*** Bitwise Ops ***//

    @Override
    @Nonnull
    public Executor bitwiseNot(@Nonnull Executor value) {
        return s -> Value.of(value.execute(s).asBigInteger().not());
    }

    @Override
    @Nonnull
    public Executor bitwiseAnd(@Nonnull Executor left, @Nonnull Executor right) {
        return s -> Value.of(left.execute(s).asBigInteger().and(right.execute(s).asBigInteger()));
    }

    @Override
    @Nonnull
    public Executor bitwiseOr(@Nonnull Executor left, @Nonnull Executor right) {
        return s -> Value.of(left.execute(s).asBigInteger().or(right.execute(s).asBigInteger()));
    }

    @Override
    @Nonnull
    public Executor bitwiseXor(@Nonnull Executor left, @Nonnull Executor right) {
        return s -> Value.of(left.execute(s).asBigInteger().xor(right.execute(s).asBigInteger()));
    }

    @Override
    @Nonnull
    public Executor leftShift(@Nonnull Executor value, @Nonnull Executor shift) {
        return s -> Value.of(value.execute(s).asBigInteger().shiftLeft(shift.execute(s).asInteger()));
    }

    @Override
    @Nonnull
    public Executor rightShift(@Nonnull Executor value, @Nonnull Executor shift) {
        return s -> Value.of(value.execute(s).asBigInteger().shiftRight(shift.execute(s).asInteger()));
    }

                //*** String Ops ***//

    @Override
    @Nonnull
    public Executor concatenate(@Nonnull Executor first, @Nonnull Executor second) {
        return s -> Value.of(first.execute(s).asText() + second.execute(s).asText());
    }
}
