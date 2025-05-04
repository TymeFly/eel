package com.github.tymefly.eel;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.time.ZonedDateTime;
import java.util.Properties;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.Nonnull;

import com.github.tymefly.eel.annotation.VisibleForTesting;
import com.github.tymefly.eel.exception.EelRuntimeException;

/**
 * Manages build time information.
 * This class is an accessor to the {@literal eel.properties} file which Maven writes at build time.
 */
class BuildTime implements Metadata {
    private static class Loader {
        private String version;
        private ZonedDateTime buildDate;

        @Nonnull
        Loader load(@Nonnull String fileName) {
            URL url = Thread.currentThread()
                .getContextClassLoader()
                .getResource(fileName);

            if (url == null) {
                throw new EelRuntimeException("Can not find build time information");
            } else {
                try (
                    InputStream stream = url.openStream()
                ) {
                    Properties properties = new Properties();

                    properties.load(stream);

                    version = read("version", properties, "build.version", VERSION_PATTERN);
                    buildDate = read("date", properties, "build.date", DATE_PATTERN, Convert::toDate);
                } catch (EelRuntimeException e) {
                    throw e;
                } catch (RuntimeException | IOException e) {
                    throw new EelRuntimeException("Failed to load EEL build time information", e);
                }
            }

            return this;
        }

        @Nonnull
        private <T> T read(@Nonnull String message,
                           @Nonnull Properties properties,
                           @Nonnull String key,
                           @Nonnull Pattern pattern,
                           @Nonnull Function<String, T> convert) {
            String raw = read(message, properties, key, pattern);
            T value = convert.apply(raw);

            return value;
        }

        @Nonnull
        private String read(@Nonnull String message,
                            @Nonnull Properties properties,
                            @Nonnull String key,
                            @Nonnull Pattern pattern) {
            String value = properties.getProperty(key);
            Matcher matcher = pattern.matcher(value);

            if (!matcher.matches()) {
                throw new EelRuntimeException("Build time information for %s is malformed ('%s')", message, value);
            }

            return matcher.group(1);
        }
    }


    private static final Pattern VERSION_PATTERN = Pattern.compile("^(\\d+\\.\\d+)\\..*");
    private static final Pattern DATE_PATTERN =
        Pattern.compile("(\\d{4}-\\d{2}-\\d{2}[ T]\\d{2}:\\d{2}:\\d{2}(.*)?)");
    private static final BuildTime INSTANCE = new BuildTime();

    private final String version;               // In the format 'major.minor' so that it can be converted to a number
    private final ZonedDateTime buildDate;


    /** Hide singleton constructor */
    private BuildTime() {
        this("com/github/tymefly/eel/eel.properties");
    }

    @VisibleForTesting
    BuildTime(@Nonnull String fileName) {
        Loader loader = new Loader()
            .load(fileName);

        version = loader.version;
        buildDate = loader.buildDate;
    }


    @Nonnull
    static BuildTime getInstance() {
        return INSTANCE;
    }

    @Override
    @Nonnull
    public String version() {
        return version;
    }

    @Override
    @Nonnull
    public ZonedDateTime buildDate() {
        return buildDate;
    }
}
