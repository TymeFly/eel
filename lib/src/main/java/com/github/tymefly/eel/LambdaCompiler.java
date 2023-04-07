package com.github.tymefly.eel;


import java.util.WeakHashMap;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import ch.obermuhlner.math.big.BigDecimalMath;
import com.github.tymefly.eel.exception.EelUnknownSymbolException;
import com.github.tymefly.eel.utils.BigDecimals;

/**
 * Class that creates {@link Executor} objects for the Parser.
 */
class LambdaCompiler implements Compiler {
    // Cache the executors for constants - we might well use them again
    private static final WeakHashMap<String, Executor> TEXT_POOL = new WeakHashMap<>();
    private static final WeakHashMap<Boolean, Executor> LOGIC_POOL = new WeakHashMap<>();
    private static final WeakHashMap<Number, Executor> NUMBER_POOL = new WeakHashMap<>();

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

    @Override
    @Nonnull
    public Executor readVariable(@Nonnull String identifier,
                                 @Nullable Executor defaultValue,
                                 @Nullable CompileVariableOp caseOp,
                                 boolean takeLength) {
        Executor result = s -> {
            String text = s.read(identifier);
            Value value;

            if (text != null){
                text = (caseOp == null ? text : caseOp.apply(text));
                value = (takeLength ? Value.of(text.length()) : Value.of(text));
            } else if (defaultValue == null) {
                throw new EelUnknownSymbolException("Unknown variable '%s'", identifier);
            } else {
                value = defaultValue.execute(s);
            }

            return value;
        };

        return result;
    }

                //*** Constants ***//


    @Override
    @Nonnull
    public Executor textConstant(@Nonnull String value) {
        return TEXT_POOL.computeIfAbsent(value, v -> (s -> Value.of(v)));
    }

    @Override
    @Nonnull
    public Executor logicConstant(boolean value) {
        return LOGIC_POOL.computeIfAbsent(value, v -> (s -> Value.of(v)));
    }

    @Override
    @Nonnull
    public Executor numericConstant(@Nonnull Number value) {
        return NUMBER_POOL.computeIfAbsent(value, v -> (s -> Value.of(v)));
    }

    @Nonnull
    @Override
    public Executor pi() {
        return numericConstant(Math.PI);
    }

    @Nonnull
    @Override
    public Executor e() {
        return numericConstant(Math.E);
    }

                //*** ternary Op ***//

    @Override
    @Nonnull
    public Executor condition(@Nonnull Executor condition, @Nonnull Executor first, @Nonnull Executor second) {
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

        if (leftType == rightType) {
            equal = leftValue.equals(rightValue);
        } else if ((leftType == Type.TEXT) || (rightType == Type.TEXT)) {       // All values can be converted to text
            equal = leftValue.asText().equals(rightValue.asText());
        } else { // Logic and Date can be converted to numbers
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

                //*** Shift Ops ***//

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
        return s -> Value.of(left.execute(s).asNumber().divide(right.execute(s).asNumber(), context.getMathContext()));
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
        return s -> Value.of(left.execute(s).asLogic() & right.execute(s).asLogic());
    }

    @Nonnull
    @Override
    public Executor shortCircuitAnd(@Nonnull Executor left, @Nonnull Executor right) {
        return s -> Value.of(left.execute(s).asLogic() && right.execute(s).asLogic());
    }

    @Override
    @Nonnull
    public Executor logicalOr(@Nonnull Executor left, @Nonnull Executor right) {
        return s -> Value.of(left.execute(s).asLogic() | right.execute(s).asLogic());
    }

    @Nonnull
    @Override
    public Executor shortCircuitOr(@Nonnull Executor left, @Nonnull Executor right) {
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

                //*** String Ops ***//

    @Override
    @Nonnull
    public Executor concatenate(@Nonnull Executor first, @Nonnull Executor second) {
        return s -> Value.of(first.execute(s).asText() + second.execute(s).asText());
    }


                //*** Cast Ops ***//

    @Override
    @Nonnull
    public Executor toText(@Nonnull Executor value) {
        return s -> Value.of(value.execute(s).asText());
    }

    @Override
    @Nonnull
    public Executor toNumber(@Nonnull Executor value) {
        return s -> Value.of(value.execute(s).asNumber());
    }

    @Override
    @Nonnull
    public Executor toLogic(@Nonnull Executor value) {
        return s -> Value.of(value.execute(s).asLogic());
    }

    @Nonnull
    @Override
    public Executor toDate(@Nonnull Executor value) {
        return s -> Value.of(value.execute(s).asDate());
    }
}
