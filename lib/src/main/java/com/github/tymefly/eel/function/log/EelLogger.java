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
 * These are the EEL Functions that support logging. This could be used by the client
 * to help debug problematic expressions or monitor the values generated by an expression. For example, the
 * EEL expression:
 * <br>
 * <pre> "${ Log.debug(1 + 2 + 3) }"</pre>
 * will calculate the value {@literal 6}, log it at debug level, and then return it as
 * {@link com.github.tymefly.eel.Type#NUMBER} to the client application.
 * <br>
 * Logging hardcoded values probably isn't that valuable; however, EEL can log values read from the symbols table.
 * For example:
 * <br>
 * <pre> "Hello: $( Log.info( ${userName} ))"</pre>
 * will read a value from the symbols table, log it and then used it as part of a friendly message.
 * <br>
 * EEL also supports conditional logging. For example:
 * <br>
 * <pre>"The Result is: ${key-$( log.info("defaulting to {}", "defaultValue") )}"</pre>
 *
 * In this case if {@literal key} is in the symbols table then its associated value will be returned without any
 * logging. However, if the value cannot be read then a warning is logged and the {@literal defaultValue} is
 * returned instead. This works because the default value for a variable is a complete EEL expression in its own right,
 * as such it can support embedded expressions that include function calls
 * <p>
 * <b>NOTE:</b> As expressions can be set after applications have been developed and deployed then there is a
 * need to guard against log repudiation attacks. Consequently:
 * <ul>
 *  <li>All logged messages are prefixed by the literal text "{@code Logged EEL Message: }" to make it obvious
 *      they are generated by EEL and not from some other source</li>
 *  <li>Only printable ASCII characters are logged and tabs. Control characters, including new lines and Unicode
 *      characters outside the ASCII range will be filtered out</li>
 * </ul>
 * <p>
 * <b>IMPORTANT NOTE:</b> The client's logging framework is responsible for enabling logging.
 * EEL writes all its messages to the {@literal com.github.tymefly.eel.log} logger, so it is recommended that this
 * is configured with {@link org.slf4j.Logger#trace(String)} level logging enabled.
 */
@PackagedEelFunction
public class EelLogger {
    /** The name of the logger used by EEL to log data */
    public static final String EEL_LOGGER = "com.github.tymefly.eel.log";

    private static final Logger LOGGER = LoggerFactory.getLogger(EEL_LOGGER);
    private static final String PREFIX = "Logged EEL Message: ";


    /**
     * Entry point for the {@code log.error} function which will write a message to the logger at
     * the ERROR level.
     * The EEL syntax for this function is:
     * <ul>
     *   <li><code>Log.error( value )</code> - write a value to the logger</li>
     *   <li><code>Log.error( message, value... )</code> - write a formatted message to the logger</li>
     * </ul>
     * @param first     Either the value to be logged or the formatting string
     * @param others    arguments for logging
     * @return the last {@code value} logged
     */
    @EelFunction("log.error")
    public Value error(@Nonnull Value first, @DefaultArgument("") Value... others) {
        return log(LOGGER::error, first, others);
    }


    /**
     * Entry point for the {@code log.warn} function which will write a message to the logger at
     * the WARN level.
     * <br>
     * The EEL syntax for this function is:
     * <ul>
     *   <li><code>Log.warn( value )</code> - write a value to the logger</li>
     *   <li><code>Log.warn( message, value... )</code> - write a formatted message to the logger</li>
     * </ul>
     * @param first     Either the value to be logged or the formatting string
     * @param others    arguments for logging
     * @return the last {@code value} logged
     */
    @EelFunction("log.warn")
    public Value warn(@Nonnull Value first, @DefaultArgument("") Value... others) {
        return log(LOGGER::warn, first, others);
    }

    /**
     * Entry point for the {@code log.info} function which will write a message to the logger at
     * the INFO level.
     * <br>
     * The EEL syntax for this function is:
     * <ul>
     *   <li><code>Log.info( value )</code> - write a value to the logger</li>
     *   <li><code>Log.info( message, value... )</code> - write a formatted message to the logger</li>
     * </ul>
     * @param first     Either the value to be logged or the formatting string
     * @param others    arguments for logging
     * @return the last {@code value} logged
     */
    @EelFunction("log.info")
    public Value info(@Nonnull Value first, @DefaultArgument("") Value... others) {
        return log(LOGGER::info, first, others);
    }


    /**
     * Entry point for the {@code log.debug} function which will write a message to the logger at
     * the DEBUG level.
     * <br>
     * The EEL syntax for this function is:
     * <ul>
     *   <li><code>Log.debug( value )</code> - write a value to the logger</li>
     *   <li><code>Log.debug( message, value... )</code> - write a formatted message to the logger</li>
     * </ul>
     * @param first     Either the value to be logged or the formatting string
     * @param others    arguments for logging
     * @return the last {@code value} logged
     */
    @EelFunction("log.debug")
    public Value debug(@Nonnull Value first, @DefaultArgument("") Value... others) {
        return log(LOGGER::debug, first, others);
    }


    /**
     * Entry point for the {@code log.trace} function which will write a message to the logger at
     * the TRACE level.
     * <br>
     * The EEL syntax for this function is:
     * <ul>
     *   <li><code>Log.trace( value )</code> - write a value to the logger</li>
     *   <li><code>Log.trace( message, value... )</code> - write a formatted message to the logger</li>
     * </ul>
     * @param first     Either the value to be logged or the formatting string
     * @param others    arguments for logging
     * @return the last {@code value} logged
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
