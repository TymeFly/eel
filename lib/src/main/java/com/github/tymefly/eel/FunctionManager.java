package com.github.tymefly.eel;

import java.io.File;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.regex.Pattern;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.github.tymefly.eel.exception.EelRuntimeException;
import com.github.tymefly.eel.exception.EelUnknownFunctionException;
import com.github.tymefly.eel.function.date.DateFactory;
import com.github.tymefly.eel.function.eel.EelMetadata;
import com.github.tymefly.eel.function.format.FormatDate;
import com.github.tymefly.eel.function.general.Text;
import com.github.tymefly.eel.function.io.FileIo;
import com.github.tymefly.eel.function.log.EelLogger;
import com.github.tymefly.eel.function.number.Constants;
import com.github.tymefly.eel.function.system.FileSystem;
import com.github.tymefly.eel.function.text.RandomText;
import com.github.tymefly.eel.udf.DefaultArgument;
import com.github.tymefly.eel.udf.EelFunction;
import com.github.tymefly.eel.udf.FunctionalResource;
import com.github.tymefly.eel.udf.PackagedEelFunction;
import org.reflections.Reflections;
import org.reflections.scanners.Scanners;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;
import org.reflections.util.FilterBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static java.util.Map.entry;

/**
 * Manager that is used to call any of the registered EEL functions.
 * This is a mini IoC container
 */
class FunctionManager {
    /**
     * A Description of an external function
     * @param name              Name of the function as seen in the EEL expression
     * @param entryPoint        method that the FunctionManager needs to call to invoke the function
     */
    private record Description(@Nonnull String name, @Nonnull Method entryPoint) {
    }


    /**
     * Builder for {@link FunctionManager} instances. Use this to register functions.
     */
    static class Builder {
        private static final Pattern UDF_NAME = Pattern.compile("[A-Za-z_][A-Za-z0-9_]*([.][A-Za-z0-9_]+)*");
        private static final Map<Package, Set<Class<?>>> PACKAGE_CACHE = new HashMap<>();
        private static final Map<Class<?>, List<Description>> DESCRIPTION_CACHE = new HashMap<>();

        private static final Collection<String> RESERVED_PREFIX = Set.of(
            "",
            "eel",
            "system",
            "io",
            "text",
            "number",
            "logic",
            "date",
            "log",
            "format"
            );

        private static final Collection<Package> FUNCTION_PACKAGES = Set.of(
            Text.class.getPackage(),                            // General utility functions
            EelMetadata.class.getPackage(),                     // EEL system functions
            FileSystem.class.getPackage(),                      // Host System
            FileIo.class.getPackage(),                          // Io functions
            RandomText.class.getPackage(),                      // Text functions
            Constants.class.getPackage(),                       // Number functions
            DateFactory.class.getPackage(),                     // Date functions
            EelLogger.class.getPackage(),                       // Log functions
            FormatDate.class.getPackage()                       // Formatting
        );

        private static final Map<String, Description> STANDARD_FUNCTIONS;

        private final Map<String, Description> functions;                       // STANDARD_FUNCTIONS + UDF's


        static {
            Map<String, Description> standard = new HashMap<>();

            FUNCTION_PACKAGES.forEach(location -> addPackage(standard, location, false));

            STANDARD_FUNCTIONS = Collections.unmodifiableMap(standard);
        }


        Builder() {
            this.functions = new HashMap<>(STANDARD_FUNCTIONS);
        }


        @Nonnull
        Builder withUdfClass(@Nonnull Class<?> implementation) {
            boolean done = addClass(functions, implementation, true);

            if (!done) {
                throw new EelFunctionException("Invalid function class %s", implementation.getName());
            }

            return this;
        }

        @Nonnull
        Builder withUdfPackage(@Nonnull Package location) {
            addPackage(functions, location, true);

            return this;
        }

        @Nonnull
        FunctionManager build() {
            return new FunctionManager(this);
        }


        /**
         * Scan the {@code location} for classes annotated with {@link PackagedEelFunction} and add their
         * EEL functions to the {@code functions} map
         * @param functions     Function map that will be updated
         * @param location      package that should contain EEL functions.
         * @param validateName  {@literal false} for standard functions and {@literal true} for UDFs
         */
        private static void addPackage(@Nonnull Map<String, Description> functions,
                @Nonnull Package location,
                boolean validateName) {
            Set<Class<?>> implementations = PACKAGE_CACHE.computeIfAbsent(location,
                p -> {
                    String name = location.getName();
                    String regEx = name.replace(".", "\\.")
                        .replace("$", "\\$") +
                            "\\.[^.]+\\.class$";
                    FilterBuilder filter = new FilterBuilder()
                        .includePattern(regEx);
                    ConfigurationBuilder configuration = new ConfigurationBuilder()
                        .filterInputsBy(filter)
                        .setUrls(ClasspathHelper.forPackage(name))
                        .setScanners(Scanners.TypesAnnotated);
                    Set<Class<?>> classes = new Reflections(configuration)
                        .getTypesAnnotatedWith(PackagedEelFunction.class);

                    return classes;
                });

            implementations.forEach(implementation -> addClass(functions, implementation, validateName));
        }

        /**
         * Add all the functions in a class to the {@code #functions} map
         * @param functions         Function map that will be updated
         * @param implementation    A class that should contain at least one EEL function
         * @param validateName      {@literal false} for standard functions and {@literal true} for UDFs
         * @return                  {@literal true} only if the class contained at least one function
         */
        private static boolean addClass(@Nonnull Map<String, Description> functions,
                @Nonnull Class<?> implementation,
                boolean validateName) {
            List<Description> descriptions =
                DESCRIPTION_CACHE.computeIfAbsent(implementation, i -> findEntryPoints(implementation, validateName));
            boolean done = !descriptions.isEmpty();

            if (!done) {
                LOGGER.error("Function '{}' contains no EEL functions", implementation.getSimpleName());
            }

            for (Description d : descriptions) {
                Description old = functions.put(d.name(), d);

                if ((old !=  null) && !d.equals(old)) {
                    throw new EelFunctionException("Function '%s' has multiple implementations", d.name());
                }
            }

            return done;
        }

        @Nonnull
        private static List<Description> findEntryPoints(@Nonnull Class<?> function, boolean validateName) {
            Method[] potential = function.getDeclaredMethods();
            List<Description> found = new ArrayList<>(potential.length);

            for (var entryPoint : potential) {
                EelFunction annotation = entryPoint.getAnnotation(EelFunction.class);

                if (annotation == null) {
                    // Do nothing - EEL don't care about this method
                } else if ((entryPoint.getModifiers() & Modifier.PUBLIC) == 0) {
                    LOGGER.error("Method '{}.{}' is not public", function.getName(), entryPoint.getName());
                } else {
                    String name = annotation.value();

                    if (validateName && !hasValidName(name)) {
                        throw new EelFunctionException("Invalid UDF name '%s'", name);
                    }

                    Description description = new Description(name, entryPoint);

                    found.add(description);
                }
            }

            return found;
        }

        private static boolean hasValidName(@Nonnull String name) {
            int index = name.indexOf('.');
            String prefix = (index < 1 ? "" : name.substring(0, index));
            boolean valid = !RESERVED_PREFIX.contains(prefix);

            valid = valid && UDF_NAME.matcher(name).matches();

            return valid;
        }
    }



    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private static final Map<Class<?>, Object> INSTANCE_CACHE = new ConcurrentHashMap<>();
    private static final Map<Class<?>, Function<Value, Object>> ARGUMENT_CONVERSIONS = Map.ofEntries(
        entry(Value.class, v -> v),
        entry(String.class, Value::asText),
        entry(Boolean.class, Value::asLogic),
        entry(boolean.class, Value::asLogic),
        entry(Byte.class, v -> v.asNumber().byteValue()),
        entry(byte.class, v -> v.asNumber().byteValue()),
        entry(Short.class, v -> v.asNumber().shortValue()),
        entry(short.class, v -> v.asNumber().shortValue()),
        entry(Integer.class, Value::asInt),
        entry(int.class, Value::asInt),
        entry(Long.class, Value::asLong),
        entry(long.class, Value::asLong),
        entry(Float.class, v -> v.asNumber().floatValue()),
        entry(float.class, v -> v.asNumber().floatValue()),
        entry(Double.class, Value::asDouble),
        entry(double.class, Value::asDouble),
        entry(BigInteger.class, Value::asBigInteger),
        entry(BigDecimal.class, Value::asNumber),
        entry(ZonedDateTime.class, Value::asDate),
        entry(Character.class, Value::asChar),
        entry(char.class, Value::asChar),
        entry(File.class, FunctionManager::asFile)
    );


    private final Map<String, Description> descriptions;


    private FunctionManager(@Nonnull Builder builder) {
        descriptions = Map.copyOf(builder.functions);
    }


    @Nonnull
    Term compileCall(@Nonnull String functionName,
                     @Nonnull EelContext context,
                     @Nonnull List<Term> argumentList) {
        Description description = descriptions.get(functionName);

        if (description == null) {
            throw new EelUnknownFunctionException("Undefined function '%s'", functionName);
        }

        Method entryPoint = description.entryPoint();
        Class<?> implementation = entryPoint.getDeclaringClass();
        Object instance = INSTANCE_CACHE.computeIfAbsent(implementation, this::createInstance);

        return s -> invokeFunction(functionName, instance, entryPoint, s, context, argumentList);
    }


    @Nonnull
    private Object createInstance(@Nonnull Class<?> function) {
        Object instance;

        try {
            instance = function.getConstructor()
                .newInstance();
        } catch (ReflectiveOperationException e) {
            throw new EelFunctionException("Failed to execute default constructor for '" + function.getName() +  "'",
                e);
        }

        return instance;
    }


    @Nonnull
    private Value invokeFunction(@Nonnull String name,
                                 @Nonnull Object instance,
                                 @Nonnull Method entryPoint,
                                 @Nonnull SymbolsTable symbols,
                                 @Nonnull EelContext context,
                                 @Nonnull List<Term> argumentList) {
        Object[] arguments = buildArguments(name, entryPoint, symbols, context, argumentList);
        Object returned;

        try {
            returned = entryPoint.invoke(instance, arguments);
        } catch (RuntimeException | ReflectiveOperationException e) {
            Throwable cause = e.getCause();

            if (cause instanceof EelRuntimeException eelException) {        // Don't wrap Eel Exceptions
                throw eelException;
            }

            if (cause == null) {
                cause = e;
            }

            throw new EelFunctionException("Failed to execute function '" + name + "'", cause);
        }

        return convertReturned(name, returned);
    }


    @Nonnull
    private Object[] buildArguments(@Nonnull String name,
                                    @Nonnull Method entryPoint,
                                    @Nonnull SymbolsTable symbols,
                                    @Nonnull EelContext context,
                                    @Nonnull List<Term> argumentList) {
        Parameter[] params = entryPoint.getParameters();
        int actualSize = entryPoint.getParameterCount();
        Object[] actual = new Object[actualSize];
        int paramIndex = 0;
        int argumentIndex = 0;
        boolean isVarArgs = false;

        for (int paramsLength = params.length; paramIndex < paramsLength; paramIndex++) {
            Parameter parameter = params[paramIndex];
            Class<?> paramType = parameter.getType();

            isVarArgs = (paramIndex == params.length - 1) && paramType.isArray();

            if (parameter.getType() == EelContext.class) {
                actual[paramIndex] = context;
            } else if (parameter.getType() == FunctionalResource.class) {
                actual[paramIndex] = new FunctionalResourceImpl(context, entryPoint.getDeclaringClass());
            } else if (isVarArgs) {
                actual[paramIndex] = varArgs(name, symbols, argumentList, argumentIndex, paramType.getComponentType());
                argumentIndex++;
            } else if (argumentIndex < argumentList.size()) {
                actual[paramIndex] = convertArgument(name, symbols, argumentList, argumentIndex, paramType);
                argumentIndex++;
            } else {
                actual[paramIndex] = defaultArgument(name, parameter, paramIndex, paramType);
            }
        }

        if (!isVarArgs && (paramIndex < argumentList.size())) {
            throw new EelFunctionException("Expected %d argument(s) for function '%s' but %d were passed",
                params.length, name, argumentList.size());
        }

        return actual;
    }

    @Nonnull
    private Object convertArgument(@Nonnull String name,
                                   @Nonnull SymbolsTable symbols,
                                   @Nonnull List<Term> argumentList,
                                   int index,
                                   @Nonnull Class<?> targetType) {
        Term argument = argumentList.get(index);
        Object converted;

        if (targetType == Value.class) {            // Don't execute the argument - we may never need its value
            converted = new ValueArgument(argument, symbols);
        } else {
            Value value = argument.evaluate(symbols);

            converted = convert(name, index, targetType, value);
        }

        return converted;
    }

    @Nonnull
    private Object convert(@Nonnull String name, int index, @Nonnull Class<?> targetType, @Nonnull Value value) {
        return ARGUMENT_CONVERSIONS.getOrDefault(targetType, (k) -> {
                throw new EelFunctionException("Argument %d for function '%s' is of unsupported type %s",
                    index, name, targetType.getName());
            }
        ).apply(value);
    }


    @Nonnull
    private static File asFile(@Nonnull Value fileName) {
        String path = fileName.asText();
        File result;

        try {
            result = FileFactory.from(path);
        } catch (IOException e) {
            throw new EelFunctionException("File '" + path + "' accesses a sensitive part of the filesystem", e);
        }

        return result;
    }



    @Nonnull
    private Object defaultArgument(@Nonnull String name,
                                   @Nonnull Parameter parameter,
                                   int index,
                                   @Nonnull Class<?> targetType) {
        DefaultArgument annotation = parameter.getAnnotation(DefaultArgument.class);

        if (annotation == null) {
            throw new EelFunctionException("Argument %d for function '%s' is missing and no default exists",
                index, name, targetType.getName());
        }

        String to = annotation.value();
        Value value = Value.of(to);
        Object argument = convert(name, index, targetType, value);

        return argument;
    }


    @Nonnull
    private Object varArgs(@Nonnull String name,
                           @Nonnull SymbolsTable symbols,
                           @Nonnull List<Term> argumentList,
                           int passedIndex,
                           @Nonnull Class<?> targetType) {
        int size = argumentList.size() - passedIndex;
        Object varArgs = Array.newInstance(targetType, size);
        int varArgIndex = 0;

        while (passedIndex != argumentList.size()) {
            Object converted = convertArgument(name, symbols, argumentList, passedIndex, targetType);

            Array.set(varArgs, varArgIndex++, converted);
            passedIndex++;
        }

        return varArgs;
    }

    @Nonnull
    private Value convertReturned(@Nonnull String name, @Nullable Object returned) {
        Value result;

        if (returned == null) {
            throw new EelFunctionException("Function '%s' returned null", name);
        } else if (returned instanceof Value value) {
            result = value;
        } else if (returned instanceof String str) {
            result = Constant.of(str);
        } else if (returned instanceof Number num) {
            result = Constant.of(num);
        } else if (returned instanceof Boolean bool) {
            result = Constant.of(bool);
        } else if (returned instanceof ZonedDateTime date) {
            result = Constant.of(date);
        } else if (returned instanceof Character character) {
            result = Constant.of(Character.toString(character));
        } else {
            throw new EelFunctionException("Function '%s' returned unexpected type '%s'",
                name, returned.getClass().getName());
        }

        return result;
    }
}
