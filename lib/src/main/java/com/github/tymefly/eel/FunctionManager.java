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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.github.tymefly.eel.exception.EelArithmeticException;
import com.github.tymefly.eel.exception.EelFunctionException;
import com.github.tymefly.eel.exception.EelRuntimeException;
import com.github.tymefly.eel.function.date.DateFactory;
import com.github.tymefly.eel.function.format.FormatDate;
import com.github.tymefly.eel.function.math.Abs;
import com.github.tymefly.eel.function.system.FileSystem;
import com.github.tymefly.eel.function.util.Text;
import com.github.tymefly.eel.udf.DefaultArgument;
import com.github.tymefly.eel.udf.EelFunction;
import com.github.tymefly.eel.udf.PackagedEelFunction;
import com.github.tymefly.eel.utils.Convert;
import org.reflections.Reflections;
import org.reflections.scanners.Scanners;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class that handles calls to registered functions
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

        private static final List<Package> STANDARD_FUNCTIONS = List.of(
            Text.class.getPackage(),                            // Utils
            FileSystem.class.getPackage(),                      // System
            Abs.class.getPackage(),                             // Maths
            DateFactory.class.getPackage(),                     // Date
            FormatDate.class.getPackage());                     // Format

        private static final List<String> RESERVED_PREFIX = List.of(
            "",
            "system",
            "date",
            "format",
            "log",
            "eel");

        private final Map<String, Description> functions;


        Builder() {
            this.functions = new HashMap<>();

            STANDARD_FUNCTIONS.forEach(location -> addPackage(location, false));
        }

        @Nonnull
        Builder withUdfClass(@Nonnull Class<?> implementation) {
            boolean done = addClass(implementation, true);

            if (!done) {
                throw new EelFunctionException("Invalid function class %s", implementation.getName());
            }

            return this;
        }

        @Nonnull
        Builder withUdfPackage(@Nonnull Package location) {
            addPackage(location, true);

            return this;
        }

        @Nonnull
        FunctionManager build() {
            return new FunctionManager(this);
        }


        private void addPackage(@Nonnull Package location, boolean validateName) {
            Set<Class<?>> implementations = PACKAGE_CACHE.computeIfAbsent(location,
                  l -> new Reflections(l.getName(), Scanners.TypesAnnotated)
                        .getTypesAnnotatedWith(PackagedEelFunction.class));

            implementations.forEach(implementation -> addClass(implementation, validateName));
        }


        private boolean addClass(@Nonnull Class<?> implementation, boolean validateName) {
            // validateName is true for UDF not will be false for standard functions
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
        private List<Description> findEntryPoints(@Nonnull Class<?> function, boolean validateName) {
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

        private void validateName(@Nonnull String name) {
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

        Class<?> implementation = description.entryPoint().getDeclaringClass();
        Method entryPoint = description.entryPoint;
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
    private Value compileCall(@Nonnull String functionName,
                              @Nonnull Object instance,
                              @Nonnull Method entryPoint,
                              @Nonnull SymbolsTable symbols,
                              @Nonnull EelContext context,
                              @Nonnull List<Executor> argumentList) {
        Object[] arguments = evaluateArguments(functionName, entryPoint, symbols, context, argumentList);
        Object result;
        Value value;

        try {
            result = entryPoint.invoke(instance, arguments);
        } catch (RuntimeException | ReflectiveOperationException e) {
            Throwable cause = e.getCause();

            if (cause instanceof EelRuntimeException eel) {
                throw eel;
            } else if (cause instanceof ArithmeticException a) {
                throw new EelArithmeticException("Failed to execute function '" + functionName + "'", a);
            } else {
                throw new EelFunctionException("Failed to execute function '" + functionName + "'", cause);
            }
        }

        value = convertResult(functionName, result);

        return value;
    }


    @Nonnull
    private Object[] evaluateArguments(@Nonnull String functionName,
            @Nonnull Method entryPoint,
            @Nonnull SymbolsTable symbols,
            @Nonnull EelContext context,
            @Nonnull List<Executor> argumentList) {
        Parameter[] params = entryPoint.getParameters();
        List<Value> passed = evaluateArguments(symbols, argumentList);
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
            } else if (isVarArgs) {
                actual[paramIndex] = varArgs(functionName, argumentIndex, passed, paramType.componentType());
                argumentIndex++;
            } else if (argumentIndex < passed.size()) {
                actual[paramIndex] = convertArgument(functionName, argumentIndex, passed.get(argumentIndex), paramType);
                argumentIndex++;
            } else {
                actual[paramIndex] = defaultArgument(functionName, paramIndex, parameter, paramType);
            }
        }

        if (!isVarArgs && (paramIndex < passed.size())) {
            throw new EelFunctionException("Expected %d argument(s) for function '%s' but %d were passed",
                params.length, functionName, argumentList.size());
        }

        return actual;
    }

    @Nonnull
    private List<Value> evaluateArguments(@Nonnull SymbolsTable symbols, @Nonnull List<Executor> argumentList) {
        List<Value> evaluated = new ArrayList<>(argumentList.size());

        for (Executor argument : argumentList) {
            evaluated.add(argument.execute(symbols));
        }

        return evaluated;
    }

    @Nonnull
    private Object convertArgument(@Nonnull String functionName,
            int index,
            @Nonnull Value value,
            @Nonnull Class<?> targetType) {
        Object result;

        if (targetType == String.class) {
            result = value.asText();
        } else if ((targetType == Boolean.class) || (targetType == boolean.class)) {
            result = value.asLogic();
        } else if ((targetType == byte.class) || (targetType == Byte.class)) {
            result = value.asNumber().byteValue();
        } else if ((targetType == short.class) || (targetType == Short.class)) {
            result = value.asNumber().shortValue();
        } else if ((targetType == int.class) || (targetType == Integer.class)) {
            result = value.asNumber().intValue();
        } else if ((targetType == long.class) || (targetType == Long.class)) {
            result = value.asNumber().longValue();
        } else if ((targetType == float.class) || (targetType == Float.class)) {
            result = value.asNumber().floatValue();
        } else if ((targetType == double.class) || (targetType == Double.class)) {
            result = value.asNumber().doubleValue();
        } else if (targetType == BigInteger.class) {
            result = value.asNumber().toBigInteger();
        } else if (targetType == BigDecimal.class) {
            result = value.asNumber();
        } else if (targetType == ZonedDateTime.class) {
            result = value.asDate();
        } else {
            throw new EelFunctionException("Argument %d for function '%s' is of unsupported type %s",
                index, functionName, targetType.getName());
        }

        return result;
    }

    @Nonnull
    private Object defaultArgument(@Nonnull String functionName,
                                   int index,
                                   @Nonnull Parameter parameter,
                                   @Nonnull Class<?> targetType) {
        Object value;
        DefaultArgument annotation = parameter.getAnnotation(DefaultArgument.class);

        if (annotation == null) {
            throw new EelFunctionException("Argument %d for function '%s' is missing and no default exists",
                index, functionName, targetType.getName());
        }

        String to = annotation.of();
        value = Convert.to(to, targetType);

        return value;
    }


    private Object varArgs(@Nonnull String functionName,
                         int passedIndex,
                         @Nonnull List<Value> passed,
                         @Nonnull Class<?> targetType) {
        int size = passed.size() - passedIndex;
        Object varArgs = Array.newInstance(targetType, size);
        int varArgIndex = 0;

        while (passedIndex != passed.size()) {
            Value value = passed.get(passedIndex);
            Object converted = convertArgument(functionName, passedIndex, value, targetType);

            Array.set(varArgs, varArgIndex++, converted);
            passedIndex++;
        }

        return varArgs;
    }


    @Nonnull
    private Value convertResult(@Nonnull String functionName, @Nullable Object result) {
        Value value;

        if (result == null) {
            throw new EelFunctionException("Function '%s' returned null", functionName);
        } else if (result instanceof String str) {
            value = Value.of(str);
        } else if (result instanceof Number num) {
            value = Value.of(num);
        } else if (result instanceof Boolean bool) {
            value = Value.of(bool);
        } else if (result instanceof ZonedDateTime date) {
            value = Value.of(date);
        } else {
            throw new EelFunctionException("Function '%s' returned unexpected type '%s'",
                functionName, result.getClass().getName());
        }

        return value;
    }
}
