package com.github.tymefly.eel;

import java.util.function.Function;

import javax.annotation.Nonnull;

import com.github.tymefly.eel.validate.Preconditions;

/**
 * Implementation of the {@link FunctionalResource}
 */
class FunctionalResourceImpl implements FunctionalResource {
    private final EelContextImpl context;
    private final Class<?> owner;

    FunctionalResourceImpl(@Nonnull EelContext context, @Nonnull Class<?> owner) {
        this.context = (EelContextImpl) context;
        this.owner = owner;
    }

    @Nonnull
    @Override
    public <T> T getResource(@Nonnull String name, @Nonnull Function<String, T> constructor) {
        Preconditions.checkNotNull(name, "The resource does not have a name");
        Preconditions.checkNotNull(constructor, "The resource does not have a constructor");

        return context.getResource(owner, name, constructor);
    }
}
