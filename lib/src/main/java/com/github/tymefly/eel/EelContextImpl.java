package com.github.tymefly.eel;

import java.math.MathContext;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.concurrent.atomic.AtomicLong;

import javax.annotation.Nonnull;

import com.github.tymefly.eel.builder.EelContextBuilder;
import com.github.tymefly.eel.validate.Preconditions;

/**
 * The only implementation of the EelContext interface
 */
class EelContextImpl implements EelContext {
    private static final RoundingMode ROUNDING = RoundingMode.HALF_UP;

    /**
     * Builder for {@link EelContext} objects
     */
    static class Builder implements EelContextBuilder {
        private static final MathContext DEFAULT_MATH_CONTEXT = new MathContext(EelContext.DEFAULT_PRECISION, ROUNDING);

        private final FunctionManager.Builder functionManager;
        private MathContext mathContext = DEFAULT_MATH_CONTEXT;
        private int maxLength = DEFAULT_MAX_EXPRESSION_LENGTH;
        private Duration timeout = DEFAULT_TIMEOUT;


        Builder() {
            functionManager = new FunctionManager.Builder();
        }

        @Nonnull
        @Override
        public EelContextBuilder withMaxExpressionSize(int maxLength) {
            Preconditions.checkArgument((maxLength >= 0), "Invalid length: %d", maxLength);

            this.maxLength = maxLength;

            return this;
        }

        @Nonnull
        @Override
        public EelContextBuilder withTimeout(@Nonnull Duration timeout) {
            Preconditions.checkArgument(!timeout.isNegative(), "Invalid timeout: %s", timeout);

            this.timeout = timeout;

            return this;
        }


        @Nonnull
        @Override
        public EelContextBuilder withPrecision(int precision) {
            this.mathContext = new MathContext(precision, ROUNDING);

            return this;
        }

        @Nonnull
        @Override
        public EelContextBuilder withUdfPackage(@Nonnull Package location) {
            Preconditions.checkNotNull(location, "Can not set a null location");

            functionManager.withUdfPackage(location);

            return this;
        }

        @Nonnull
        @Override
        public EelContextBuilder withUdfClass(@Nonnull Class<?> udfClass) {
            Preconditions.checkNotNull(udfClass, "Can not set a null function class");

            functionManager.withUdfClass(udfClass);

            return this;
        }

        @Nonnull
        @Override
        public EelContextImpl build() {
            return new EelContextImpl(this);
        }
    }

    private static final AtomicLong CONTEXT_COUNT = new AtomicLong();


    private final String id;
    private final ZonedDateTime startTime;
    private final MathContext mathContext;
    private final int maxLength;
    private final Duration timeout;
    private final FunctionManager functionManager;



    private EelContextImpl(@Nonnull Builder builder) {
        this.id = "_id" + CONTEXT_COUNT.incrementAndGet();
        this.startTime = ZonedDateTime.now(ZoneId.of("UTC"));
        this.mathContext = builder.mathContext;
        this.maxLength = builder.maxLength;
        this.timeout = builder.timeout;
        this.functionManager = builder.functionManager.build();
    }

    @Override
    @Nonnull
    public MathContext getMathContext() {
        return mathContext;
    }

    @Override
    @Nonnull
    public String contextId() {
        return id;
    }

    @Override
    @Nonnull
    public ZonedDateTime getStartTime() {
        return startTime;
    }

    @Nonnull
    FunctionManager getFunctionManager() {
        return functionManager;
    }

    int maxExpressionLength() {
        return maxLength;
    }

    @Nonnull
    Duration getTimeout() {
        return timeout;
    }
}
