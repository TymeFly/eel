package com.github.tymefly.eel.function.log;

import java.util.function.BiConsumer;

import javax.annotation.Nonnull;

import com.github.tymefly.eel.Value;
import com.github.tymefly.eel.udf.DefaultArgument;
import com.github.tymefly.eel.udf.EelFunction;
import com.github.tymefly.eel.udf.PackagedEelFunction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * EEL Logging Functions
 */
@PackagedEelFunction
public class EelLogger {
    /** The name of the logger used by EEL to log data */
    public static final String EEL_LOGGER = "com.github.tymefly.eel.log";

    private static final Logger LOGGER = LoggerFactory.getLogger(EEL_LOGGER);
    private static final String PREFIX = "Logged EEL Message: ";


    /**
     * Writes a message to the logger at the ERROR level.
     * @param first     the value to be logged or the formatting string.
     * @param others    additional arguments for logging.
     * @return          the last {@code value} logged.
     */
    @EelFunction("log.error")
    public Value error(@Nonnull Value first, @DefaultArgument("") Value... others) {
        return log(LOGGER::error, first, others);
    }


    /**
     * Writes a message to the logger at the WARN level.
     * @param first     the value to be logged or the formatting string.
     * @param others    additional arguments for logging.
     * @return          the last {@code value} logged.
     */
    @EelFunction("log.warn")
    public Value warn(@Nonnull Value first, @DefaultArgument("") Value... others) {
        return log(LOGGER::warn, first, others);
    }


    /**
     * Writes a message to the logger at the INFO level.
     * @param first     the value to be logged or the formatting string.
     * @param others    additional arguments for logging.
     * @return          the last {@code value} logged.
     */
    @EelFunction("log.info")
    public Value info(@Nonnull Value first, @DefaultArgument("") Value... others) {
        return log(LOGGER::info, first, others);
    }


    /**
     * Writes a message to the logger at the DEBUG level.
     * @param first     the value to be logged or the formatting string.
     * @param others    additional arguments for logging.
     * @return          the last {@code value} logged.
     */
    @EelFunction("log.debug")
    public Value debug(@Nonnull Value first, @DefaultArgument("") Value... others) {
        return log(LOGGER::debug, first, others);
    }


    /**
     * Writes a message to the logger at the TRACE level.
     * @param first     the value to be logged or the formatting string.
     * @param others    additional arguments for logging.
     * @return          the last {@code value} logged.
     */
    @EelFunction("log.trace")
    public Value trace(@Nonnull Value first, @DefaultArgument("") Value... others) {
        return log(LOGGER::trace, first, others);
    }


    @Nonnull
    private Value log(@Nonnull BiConsumer<String, Object[]> logger, @Nonnull Value first, Value... others) {
        String format = sanitise(first);
        String[] arguments = sanitise(others);
        int length = others.length;
        Value result = (length == 0 ? first : others[length - 1]);

        logger.accept(PREFIX + format, arguments);

        return result;
    }


    @Nonnull
    private String[] sanitise(@Nonnull Value[] arguments) {
        String[] sanitised = new String[arguments.length];

        for (int index = 0; index != arguments.length; index++) {
            sanitised[index] = sanitise(arguments[index]);
        }

        return sanitised;
    }

    @Nonnull
    private String sanitise(@Nonnull Value value) {
        return value.asText()
            .replaceAll("[^\\p{Print}\\t]", "");
    }
}
