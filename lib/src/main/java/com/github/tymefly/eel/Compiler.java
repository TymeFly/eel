package com.github.tymefly.eel;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Defines the contract of a Compiler in the EEL language. The root operations are
 * {@link #readVariable(String, Executor, CompileVariableOp, boolean)}, {@link #textConstant(String)},
 * {@link #numericConstant(Number)} and {@link #logicConstant(boolean)}. The remaining operations are created by
 * combining and encapsulating previously generated operations.
 * @see CompileUnaryOp
 * @see CompileBinaryOp
 */
interface Compiler {
    @Nonnull
    Executor isDefined(@Nonnull String identifier);

    @Nonnull
    Executor readVariable(@Nonnull String identifier,
                          @Nullable Executor defaultValue,
                          @Nullable CompileVariableOp caseOp,
                          boolean takeLength);

    @Nonnull
    Executor textConstant(@Nonnull String value);

    @Nonnull
    Executor logicConstant(boolean value);

    @Nonnull
    Executor numericConstant(@Nonnull Number value);


    @Nonnull
    Executor condition(@Nonnull Executor condition, @Nonnull Executor first, @Nonnull Executor second);

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
    Executor leftShift(@Nonnull Executor value, @Nonnull Executor shift);

    @Nonnull
    Executor rightShift(@Nonnull Executor value, @Nonnull Executor shift);

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
    Executor modulus(@Nonnull Executor left, @Nonnull Executor right);

    @Nonnull
    Executor power(@Nonnull Executor left, @Nonnull Executor right);

    @Nonnull
    Executor logicalNot(@Nonnull Executor value);

    @Nonnull
    Executor logicalAnd(@Nonnull Executor left, @Nonnull Executor right);

    @Nonnull
    Executor shortCircuitAnd(@Nonnull Executor left, @Nonnull Executor right);

    @Nonnull
    Executor logicalOr(@Nonnull Executor left, @Nonnull Executor right);

    @Nonnull
    Executor shortCircuitOr(@Nonnull Executor left, @Nonnull Executor right);

    @Nonnull
    Executor bitwiseNot(@Nonnull Executor value);

    @Nonnull
    Executor bitwiseAnd(@Nonnull Executor left, @Nonnull Executor right);

    @Nonnull
    Executor bitwiseOr(@Nonnull Executor left, @Nonnull Executor right);

    @Nonnull
    Executor bitwiseXor(@Nonnull Executor left, @Nonnull Executor right);

    @Nonnull
    Executor concatenate(@Nonnull Executor first, @Nonnull Executor second);

    @Nonnull
    Executor toText(@Nonnull Executor value);

    @Nonnull
    Executor toNumber(@Nonnull Executor value);

    @Nonnull
    Executor toLogic(@Nonnull Executor value);

    @Nonnull
    Executor toDate(@Nonnull Executor value);
}
