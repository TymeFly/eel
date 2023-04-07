package com.github.tymefly.eel.integration;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.Properties;

import javax.annotation.Nonnull;

import com.github.tymefly.eel.Eel;
import com.github.tymefly.eel.EelContext;
import com.github.tymefly.eel.exception.EelUnknownSymbolException;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

/**
 * A simple class that loads a properties file which contains Eel expressions.
 * <br>
 * <b>Note:</b> Forward references are not supported.
 */
public class EelProperties {
    private static class Lazy {
        private static final EelContext CONTEXT = EelContext.factory().build();
    }

    /**
     * Class that uses Properties to parse the contents of a Properties InputStream or Reader.
     * The results are stored in a Properties backing object rather than this object. This so that
     * Properties object returned to the client doesn't have the overridden method
     */
    @SuppressFBWarnings(value = "EQ_DOESNT_OVERRIDE_EQUALS",
        justification = ".equals() wont be called as class is thrown away after the data is loaded")
    private static class Loader extends Properties {
        private final EelContext context;
        private final Properties backing;

        Loader(@Nonnull EelContext context) {
            this.context = context;
            this.backing = new Properties();
        }

        @Override
        public synchronized Object put(Object key, Object value) {
            String resolved;

            try {
                resolved = Eel.compile(context, value.toString())
                    .evaluate(backing::getProperty)
                    .asText();
            } catch (EelUnknownSymbolException e) {
                throw new UnknownEelPropertyException(key.toString());
            }

            return backing.put(key, resolved);
        }
    }


    private final EelContext context;

    /**
     * Constructor that parses EEL properties with a default constructor
     */
    public EelProperties() {
        this(Lazy.CONTEXT);
    }

    /**
     * Constructor
     * @param context       Custom Context for parsing EEL expressions
     */
    public EelProperties(@Nonnull EelContext context) {
        this.context = context;
    }


    /**
     * Load a properties file from the {@code inStream} resolving any {@link Eel} expressions in the value
     * @param inputStream   the input character stream.
     * @return The Properties object
     * @throws IOException if an error occurred when reading from the input stream.
     * @throws IllegalArgumentException if a malformed Unicode escape appears in the input.
     * @throws NullPointerException if reader is null.
     * @throws UnknownEelPropertyException if an expression EEL references a property that does not exist.
     *                              This may be caused by an illegal forwards reference
     * @see Properties#load(Reader)
     */
    @Nonnull
    public Properties load(@Nonnull InputStream inputStream) throws UnknownEelPropertyException, IOException {
        Loader loader = new Loader(context);

        loader.load(inputStream);

        return loader.backing;
    }


    /**
     * Load a properties file from the {@code reader} resolving any {@link Eel} expressions in the value
     * @param reader    the input character stream.
     * @return The Properties object
     * @throws IOException if an error occurred when reading from the input stream.
     * @throws IllegalArgumentException if a malformed Unicode escape appears in the input.
     * @throws NullPointerException if reader is null.
     * @throws UnknownEelPropertyException if an expression EEL references a property that does not exist.
     *                              This may be caused by an illegal forwards reference
     * @see Properties#load(Reader)
     */
    @Nonnull
    public Properties load(@Nonnull Reader reader) throws UnknownEelPropertyException, IOException {
        Loader loader = new Loader(context);

        loader.load(reader);

        return loader.backing;
    }
}
