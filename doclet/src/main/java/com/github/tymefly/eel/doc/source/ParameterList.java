package com.github.tymefly.eel.doc.source;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.type.TypeKind;

import com.github.tymefly.eel.doc.utils.EelType;
import com.github.tymefly.eel.udf.DefaultArgument;

/**
 * Class that defines a Parameter list for the underlying Java method.
 */
public class ParameterList {
    static final ParameterList EMPTY = new ParameterList(Collections.emptyMap(), Collections.emptyList());

    private final Map<String, Parameter> byName;
    private final List<Parameter> byIndex;

    private ParameterList(@Nonnull Map<String, Parameter> byName, @Nonnull List<Parameter> byIndex) {
        this.byName = byName;
        this.byIndex = byIndex;
    }


    /**
     * Factory method for creating ParameterList objects.
     * @param element   A description of the method
     * @return          A new ParameterList
     */
    @Nonnull
    static ParameterList build(@Nonnull ExecutableElement element) {
        Map<String, Parameter> byName = new HashMap<>();
        List<Parameter> byIndex = new LinkedList<>();

        int order = 0;

        for (var parameter : element.getParameters()) {
            String name = parameter.getSimpleName().toString();
            EelType type = TranslateType.toEel(parameter.asType());
            boolean isVarArgs = parameter.asType().getKind() == TypeKind.ARRAY;
            DefaultArgument defaultAnnotation = parameter.getAnnotation(DefaultArgument.class);
            String defaultDescription;

            // The default description comes from the optional description field in the annotation
            // If it's not set then the mandatory "value" field is used instead.
            if (defaultAnnotation == null) {
                defaultDescription = null;
            } else if (!defaultAnnotation.description().isEmpty()) {
                defaultDescription = defaultAnnotation.description();
            } else {
                defaultDescription = defaultAnnotation.value();
            }

            Parameter model = new Parameter(name, type, order++, defaultDescription, isVarArgs);

            byName.put(name, model);
            byIndex.add(model);
        }

        return new ParameterList(byName, byIndex);
    }


    /**
     * Returns the parameter by its name, or {@literal null} if the name is invalid.
     * This could happen if the {@code @param} tag references an invalid parameter
     * @param name      name of the parameter
     * @return          the parameter in the list
     */
    @Nullable
    public Parameter get(@Nonnull String name) {
        return byName.get(name);
    }


    /**
     * Returns a stream of {@link Parameter} objects, in the order they are defined in the implementing method
     * @return a stream of {@link Parameter} objects
     */
    @Nonnull
    public Stream<Parameter> stream() {
        return byIndex.stream();
    }
}
