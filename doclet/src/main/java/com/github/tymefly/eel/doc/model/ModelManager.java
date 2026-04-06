package com.github.tymefly.eel.doc.model;

import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.github.tymefly.eel.doc.utils.EelType;

/**
 * Entry point for managing documentation models
 */
public class ModelManager {
    private final Map<String, Group> groups;
    private final Map<String, Function> bySignature;        // Function signature => Model
    private final Set<String> names;                        // EEL Function names

    /** Constructor */
    public ModelManager() {
        groups = new TreeMap<>();
        names = new HashSet<>();
        bySignature = new HashMap<>();
    }


    /**
     * Add a new Function to the model.
     * @param eelName   EEL name of the function, which includes a group name
     * @param type      the EEL type that the function returns.
     * @param signature fully qualified signature of the source method
     * @return A mutator class used to further describe the EEL function
     */
    @Nonnull
    public FunctionGenerator addFunction(@Nonnull String eelName,
                                         @Nullable EelType type,
                                         @Nonnull String signature) {
        int index = eelName.indexOf('.');
        String groupName = (index ==  -1 ? "" : eelName.substring(0, index));
        Group group = groups.computeIfAbsent(groupName, Group::new);
        Function function = new Function(eelName, group, type);

        names.add(eelName);
        bySignature.put(signature, function);

        group.add(function);

        return function;
    }


    /**
     * Returns a new, unbound, text block
     * @return a new, unbound, text block
     */
    @Nonnull
    public TextBlockGenerator textBlock() {
        return new TextBlock();
    }


    /**
     * Returns the GroupGenerator for the {@code groupName}. Create a new group if one does not already exist
     * @param groupName     the name of a group.
     * @return the GroupGenerator for the {@code groupName}
     */
    @Nonnull
    public GroupGenerator group(@Nonnull String groupName) {
        return groups.computeIfAbsent(groupName, Group::new);
    }


    /**
     * Returns all the function groups in the order they should be rendered
     * @return all the function groups in the order they should be rendered
     */
    @Nonnull
    public Collection<GroupModel> groups() {
        return groups.values()
            .stream()
            .sorted(Comparator.comparing(Group::displayOrder)           // First, sort by display order...
                .thenComparing(Group::name))                            // ... then sort by name
            .map(g -> (GroupModel) g)
            .toList();
    }


    /**
     * Returns {@literal true} only if the {@code eelName} is a valid function
     * @param eelName   name of an EEL function
     * @return {@literal true} only if the {@code eelName} is a valid function
     */
    public boolean hasFunction(@Nonnull String eelName) {
        return names.contains(eelName);
    }


    /**
     * Returns a function model by its signature. {@literal null} is returned if the function is not modelled
     * @param signature     a function signature
     * @return a function model
     * @see #addFunction(String, EelType, String)
     */
    @Nullable
    public FunctionModel bySignature(@Nonnull String signature) {
        return bySignature.get(signature);
    }
}
