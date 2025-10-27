package com.github.tymefly.eel.doc.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Map;

import javax.annotation.Nonnull;

import static java.util.Map.entry;

/**
 * The Group model. This is a container for functions
 */
class Group extends Element<GroupGenerator> implements GroupModel, GroupGenerator {
    private static final Map<String, Integer> ORDER = Map.ofEntries(
        entry("", 0),
        entry("eel", 1),
        entry("system", 2),
        entry("text", 3),
        entry("number", 4),
        entry("logic", 5),
        entry("date", 6),
        entry("log", 7),
        entry("format", 8),
        entry("io", 9)
    );

    private static final int ORDER_ALPHABETICAL = 999;

    private final String name;
    private final String fileName;
    private final int displayOrder;
    private final Collection<Function> functions = new ArrayList<>();

    private boolean hasDescription = false;


    Group(@Nonnull String name) {
        boolean unnamed = name.isEmpty();

        this.name = (unnamed ? "General utilities" : name);
        this.fileName = (unnamed ? "_$.html" :  "_" + name + ".html");
        this.displayOrder = ORDER.getOrDefault(name, ORDER_ALPHABETICAL);
    }

    void add(@Nonnull Function function) {
        functions.add(function);
    }

    // Hidden functions are filtered out
    @Override
    @Nonnull
    public Collection<FunctionModel> getFunctions() {
        return functions.stream()
            .filter(f -> !f.isHidden())
            .sorted(Comparator.comparing(Function::name))
            .map(f -> (FunctionModel) f)
            .toList();
    }

    @Override
    @Nonnull
    public String name() {
        return name;
    }

    @Override
    @Nonnull
    public String fileName() {
        return fileName;
    }

    int displayOrder() {
        return displayOrder;
    }


    @Override
    public boolean hasDescription() {
        return hasDescription;
    }

    @Nonnull
    @Override
    public GroupGenerator withDescription() {
        this.hasDescription = true;

        return this;
    }
}
