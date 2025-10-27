package com.github.tymefly.eel.doc.model;

import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.github.tymefly.eel.doc.source.Parameter;
import com.github.tymefly.eel.doc.utils.EelType;

/**
 * The model for a single method
 */
class Function extends Element<FunctionGenerator> implements FunctionModel, FunctionGenerator {
    private static final Map<String, Integer> NAME_MAP = new HashMap<>();       // Count number of times 'name' is used

    private final String name;
    private final GroupModel group;
    private final EelType type;
    private final String uniqueId;
    private final Map<String, Param> parameters;


    Function(@Nonnull String eelName, @Nonnull GroupModel group, @Nullable EelType type) {
        this.name = eelName.trim();
        this.group = group;
        this.type = type;
        this.parameters = new LinkedHashMap<>();            // Stored in order they are defined

        int count = NAME_MAP.computeIfAbsent(eelName, n -> 1);
        NAME_MAP.put(eelName, count + 1);

        this.uniqueId = eelName + (count == 1 ? "" : "_" + count);
    }


    @Override
    @Nonnull
    public Param addParameter(@Nonnull String identifier, @Nullable Parameter parameter) {
        return parameters.computeIfAbsent(identifier, k -> new Param(identifier, parameter));
    }


    @Override
    @Nonnull
    public String name() {
        return name;
    }

    @Override
    @Nonnull
    public GroupModel group() {
        return group;
    }

    @Override
    @Nonnull
    public Optional<EelType> type() {
        return Optional.ofNullable(type);
    }


    @Override
    @Nonnull
    public String uniqueId() {
        return uniqueId;
    }


    @Override
    @Nonnull
    public List<ParamModel> parameters() {
        return parameters.values()
            .stream()
            .filter(ParamModel::isParameter)                        // Filter out documented but not declared
            .filter(p -> p.type().isPresent())                      // Filter out EelContext/FunctionalResource
            .sorted(Comparator.comparingInt(ParamModel::order))     // Sort into the order they are passed
            .collect(Collectors.toList());
    }

    @Nonnull
    @Override
    public String eelSignature() {
        StringBuilder builder = new StringBuilder(name())
            .append('(');
        String delimiter = "";

        for (var parameter : parameters()) {
            builder.append(delimiter)
                .append(parameter.identifier());

            if (parameter.isVarArgs()) {
                builder.append("...");
            }

            delimiter = ", ";
        }

        builder.append(')');

        return builder.toString();
    }


    @Override
    public String toString() {
        return "FunctionModel{name='" + name + '\'' + '}';
    }
}
