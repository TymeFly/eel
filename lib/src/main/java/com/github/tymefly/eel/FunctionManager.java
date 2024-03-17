package com.github.tymefly.eel;

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
import java.util.function.Function;
import java.util.regex.Pattern;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.github.tymefly.eel.exception.EelRuntimeException;
import com.github.tymefly.eel.function.date.DateFactory;
import com.github.tymefly.eel.function.eel.EelMetadata;
import com.github.tymefly.eel.function.format.FormatDate;
import com.github.tymefly.eel.function.number.Abs;
import com.github.tymefly.eel.function.system.FileSystem;
import com.github.tymefly.eel.function.util.Text;
import com.github.tymefly.eel.udf.DefaultArgument;
import com.github.tymefly.eel.udf.EelFunction;
import com.github.tymefly.eel.udf.EelLambda;
import com.github.tymefly.eel.udf.PackagedEelFunction;
import com.github.tymefly.eel.utils.Convert;
import org.reflections.Reflections;
import org.reflections.scanners.Scanners;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static java.util.Map.entry;

/**
 * Manager that is used to call any of the registered EEL functions.
 * Essentially, this is a mini IoC container
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
            "format",
            "log",
            "text",
            "logic",
            "number",
            "date"
            );

        private static final Collection<Class<?>> FUNCTION_CLASSES = Set.of(
            ConvertEelValue.class
        );
        private static final Collection<Package> FUNCTION_PACKAGES = Set.of(
            EelMetadata.class.getPackage(),                     // EEL
            Text.class.getPackage(),                            // Utils
            FileSystem.class.getPackage(),                      // System
            FormatDate.class.getPackage(),                      // Format
            Abs.class.getPackage(),                             // Number
            DateFactory.class.getPackage()                      // Date
        );

        private static final Map<String, Description> STANDARD_FUNCTIONS;

        private final Map<String, Description> functions;                       // STANDARD_FUNCTIONS + UDF's


        static {
            Map<String, Description> standard = new HashMap<>();

            FUNCTION_CLASSES.forEach(location -> addClass(standard, location, false));
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
                  l -> new Reflections(l.getName(), Scanners.TypesAnnotated)
                        .getTypesAnnotatedWith(PackagedEelFunction.class));

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

                if (old != null) {
                    throw new EelFunctionException("Function '%s' has multiple implementations", old.name());
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
                    String name = annotation.name();

                    if (validateName) {
                        validateName(name);
                    }

                    Description description = new Description(name, entryPoint);

                    found.add(description);
                }
            }

            return found;
        }

        private static void validateName(@Nonnull String name) {
            int index = name.indexOf('.');
            String prefix = (index < 1 ? "" : name.substring(0, index));
            boolean valid = !RESERVED_PREFIX.contains(prefix);

            valid = valid && UDF_NAME.matcher(name).matches();

            if (!valid) {
                throw new EelFunctionException("Invalid UDF name '%s'", name);
            }
        }
    }



    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private static final Map<Class<?>, Object> INSTANCE_CACHE = new HashMap<>();
    private static final Map<Class<?>, Function<Value, Object>> ARGUMENT_CONVERSIONS = Map.ofEntries(
        entry(EelValue.class, v -> v),
        entry(String.class, Value::asText),
        entry(Boolean.class, Value::asLogic),
        entry(boolean.class, Value::asLogic),
        entry(Byte.class, v -> v.asNumber().byteValue()),
        entry(byte.class, v -> v.asNumber().byteValue()),
        entry(Short.class, v -> v.asNumber().shortValue()),
        entry(short.class, v -> v.asNumber().shortValue()),
        entry(Integer.class, v -> v.asNumber().intValue()),
        entry(int.class, v -> v.asNumber().intValue()),
        entry(Long.class, v -> v.asNumber().longValue()),
        entry(long.class, v -> v.asNumber().longValue()),
        entry(Float.class, v -> v.asNumber().floatValue()),
        entry(float.class, v -> v.asNumber().floatValue()),
        entry(Double.class, v -> v.asNumber().doubleValue()),
        entry(double.class, v -> v.asNumber().doubleValue()),
        entry(BigInteger.class, v -> v.asNumber().toBigInteger()),
        entry(BigDecimal.class, Value::asNumber),
        entry(ZonedDateTime.class, Value::asDate),
        entry(Character.class, Convert::toChar),
        entry(char.class, Convert::toChar)
    );


    private final Map<String, Description> descriptions;


    private FunctionManager(@Nonnull Builder builder) {
        descriptions = Map.copyOf(builder.functions);
    }


    @Nonnull
    Executor compileCall(@Nonnull String functionName,
                         @Nonnull EelContext context,
                         @Nonnull List<Executor> argumentList) {
        Description description = descriptions.get(functionName);

        if (description == null) {
            throw new EelFunctionException("Undefined function '%s'", functionName);
        }

        Method entryPoint = description.entryPoint();
        Class<?> implementation = entryPoint.getDeclaringClass();
        Object instance = INSTANCE_CACHE.computeIfAbsent(implementation, this::createInstance);

        return s -> compileCall(functionName, instance, entryPoint, s, context, argumentList);
    }


    @Nonnull
    private Object createInstance(@Nonnull Class<?> function) {
        Object instance;

        try {
            instance = function.getConstructor()
                .newInstance();
        } catch (ReflectiveOperationException e) {
            throw new EelFunctionException("Failed to execute default constructor for '" + function.getName()+  "'", e);
        }

        return instance;
    }


    @Nonnull
    private Value compileCall(@Nonnull String name,
                              @Nonnull Object instance,
                              @Nonnull Method entryPoint,
                              @Nonnull SymbolsTable symbols,
                              @Nonnull EelContext context,
                              @Nonnull List<Executor> argumentList) {
        Object[] arguments = evaluateArguments(name, entryPoint, symbols, context, argumentList);
        Object result;
        Value value;

        try {
            result = entryPoint.invoke(instance, arguments);
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

        value = convertResult(name, result);

        return value;
    }


    @Nonnull
    private Object[] evaluateArguments(@Nonnull String name,
            @Nonnull Method entryPoint,
            @Nonnull SymbolsTable symbols,
            @Nonnull EelContext context,
            @Nonnull List<Executor> argumentList) {
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
                actual[paramIndex] = varArgs(name, symbols, argumentList, argumentIndex, paramType.componentType());
                argumentIndex++;
            } else if (argumentIndex < argumentList.size()) {
                actual[paramIndex] = convertArgument(name, symbols, argumentList, argumentIndex, paramType);
                argumentIndex++;
            } else {
                actual[paramIndex] = defaultArgument(name,  symbols, parameter, paramIndex, paramType);
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
                                   SymbolsTable symbols,
                                   List<Executor> argumentList,
                                   int index,
                                   @Nonnull Class<?> targetType) {
        Executor argument = argumentList.get(index);
        Object result;

        if (targetType == EelLambda.class) {            // Don't execute the argument - it may be a fail() function
            result = new EelLambdaImpl(argument, symbols);
        } else {
            Value value = argument.execute(symbols);

            result = ARGUMENT_CONVERSIONS.getOrDefault(targetType, (k) -> {
                    throw new EelFunctionException("Argument %d for function '%s' is of unsupported type %s",
                        index, name, targetType.getName());
                }
            ).apply(value);
        }

        return result;
    }

    @Nonnull
    private Object defaultArgument(@Nonnull String name,
                                   @Nonnull SymbolsTable symbols,
                                   @Nonnull Parameter parameter,
                                   int index,
                                   @Nonnull Class<?> targetType) {
        Object argument;
        DefaultArgument annotation = parameter.getAnnotation(DefaultArgument.class);

        if (annotation == null) {
            throw new EelFunctionException("Argument %d for function '%s' is missing and no default exists",
                index, name, targetType.getName());
        }

        String to = annotation.value();

        if (parameter.getType() == EelLambda.class) {
            argument = new EelLambdaImpl(s -> Value.of(to), symbols);
        } else {
            argument = Convert.to(to, targetType);
        }

        return argument;
    }


    @Nonnull
    private Object varArgs(@Nonnull String name,
                           @Nonnull SymbolsTable symbols,
                           @Nonnull List<Executor> argumentList,
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
    private Value convertResult(@Nonnull String name, @Nullable Object result) {
        Value value;

        if (result == null) {
            throw new EelFunctionException("Function '%s' returned null", name);
        } else if (result instanceof Value val) {
            value = val;
        } else if (result instanceof String str) {
            value = Value.of(str);
        } else if (result instanceof Number num) {
            value = Value.of(num);
        } else if (result instanceof Boolean bool) {
            value = Value.of(bool);
        } else if (result instanceof ZonedDateTime date) {
            value = Value.of(date);
        } else if (result instanceof Character character) {
            value = Value.of(Character.toString(character));
        } else {
            throw new EelFunctionException("Function '%s' returned unexpected type '%s'",
                name, result.getClass().getName());
        }

        return value;
    }
}
