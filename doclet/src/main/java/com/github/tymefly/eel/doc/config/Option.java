package com.github.tymefly.eel.doc.config;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import javax.annotation.Nonnull;

import com.github.tymefly.eel.Eel;
import com.github.tymefly.eel.EelContext;
import com.github.tymefly.eel.validate.Preconditions;
import jdk.javadoc.doclet.Doclet;

/**
 * Class that describes a single EelDoc command line option
 */
final class Option implements Doclet.Option {
    /** Builder class for Options. */
    static class Builder {
        private final List<String> names;
        private String description;
        private String parameter;
        private String defaultValue;
        private Kind kind = Kind.STANDARD;
        private boolean isExpression = false;

        private Builder(String... names) {
            this.names = Arrays.asList(names);
        }

        Builder withDescription(@Nonnull String description) {
            this.description = description;
            return this;
        }

        Builder withParameter(@Nonnull String parameter) {
            this.parameter = parameter;
            return this;
        }

        Builder withDefault(@Nonnull String defaultValue) {
            this.defaultValue = defaultValue;
            return this;
        }

        Builder asExtended() {
            kind = Kind.EXTENDED;
            return this;
        }

        Builder asExpression() {
            isExpression = true;
            return this;
        }

        Option build() {
            return new Option(this);
        }
    }

    private static final EelContext CONTEXT = EelContext.factory()
        .build();

    private final List<String> names;
    private final String description;
    private final String parameter;
    private final Kind kind;
    private final boolean isExpression;
    private final boolean isFlag;
    private String value;
    private boolean isSet;


    private Option(@Nonnull Builder builder) {
        this.names = builder.names;
        this.description = Preconditions.checkNotNull(builder.description, "Description not set");
        this.parameter = builder.parameter;
        this.kind = builder.kind;
        this.isExpression = builder.isExpression;
        this.isFlag = (parameter == null);
        this.value = builder.defaultValue;
        this.isSet = false;

        Preconditions.checkState(!(isExpression && isFlag), "Missing parameter for expression");
    }

    @Nonnull
    static Builder builder(String... names) {
        return new Builder(names);
    }


    @Override
    public int getArgumentCount() {
        return (parameter == null ? 0 : 1);
    }

    @Override
    @Nonnull
    public String getDescription() {
        return description;
    }

    @Override
    @Nonnull
    public Kind getKind() {
        return kind;
    }

    @Override
    @Nonnull
    public List<String> getNames() {
        return names;
    }

    @Override
    @Nonnull
    public String getParameters() {
        return (isFlag ? "" : parameter);
    }

    @Override
    public boolean process(@Nonnull String option, @Nonnull List<String> arguments) {
        isSet = true;

        if (isExpression) {
            value = Eel.compile(CONTEXT, arguments.get(0))
                .evaluate()
                .asText();
        } else if (!isFlag) {
            value = arguments.get(0);
        } else {
            // no special action for flags
        }

        return true;
    }

    boolean isSet() {
        return isSet;
    }

    @Nonnull
    String value() {
        Preconditions.checkState(!isFlag, "Can't read value for a flag");
        Preconditions.checkState((value != null), description + " not set");

        return value;
    }

    @Nonnull
    Optional<String> optionalValue() {
        return optionalValue(Function.identity());
    }

    @Nonnull
    <T> Optional<T> optionalValue(@Nonnull Function<String, T> builder) {
        Preconditions.checkState(!isFlag, "Can't read value for a flag");

        return (isSet && !value.isEmpty() ? Optional.of(builder.apply(value)) : Optional.empty());
    }
}
