package com.github.tymefly.eel.doc.model;

import java.util.Optional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.github.tymefly.eel.doc.source.Parameter;
import com.github.tymefly.eel.doc.utils.EelType;

/**
 * Model for a single parameter passed to a {@link Function}
 */
class Param extends Paragraph<ParamGenerator> implements ParamModel, ParamGenerator {
    private final String identifier;
    private final Parameter parameter;

    /**
     * Constructor
     * @param identifier    name of the parameter as found in the Javadoc
     * @param parameter     Parameter as seen in the method signature.
     *                      This could be {@literal null} if a non-existent parameter is documented
     */
    Param(@Nonnull String identifier, @Nullable Parameter parameter) {
        this.parameter = parameter;
        this.identifier = identifier;
    }

    @Override
    @Nonnull
    public String identifier() {
        return identifier;
    }

    @Override
    public boolean isParameter() {
        return (parameter != null);
    }

    @Override
    public boolean isVarArgs() {
        return ((parameter != null) && parameter.isVarArgs());
    }

    @Override
    @Nonnull
    public Optional<EelType> type() {
        EelType type = (parameter == null ? null : parameter.type());

        return Optional.ofNullable(type);
    }

    @Override
    public int order() {
        return (parameter != null ? parameter.index() : -1);
    }

    @Override
    public boolean isDefaulted() {
        return (parameter != null) && (parameter.defaultDescription() != null);
    }

    @Override
    @Nonnull
    public Optional<String> defaultDescription() {
        String description = (parameter == null ? null : parameter.defaultDescription());

        return Optional.ofNullable(description);
    }
}
