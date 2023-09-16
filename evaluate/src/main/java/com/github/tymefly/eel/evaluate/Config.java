package com.github.tymefly.eel.evaluate;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.github.tymefly.eel.EelContext;
import com.github.tymefly.eel.builder.EelContextSettingBuilder;
import com.github.tymefly.eel.integration.EelProperties;
import com.github.tymefly.eel.udf.PackagedEelFunction;
import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;
import org.kohsuke.args4j.OptionHandlerFilter;
import org.kohsuke.args4j.ParserProperties;
import org.reflections.Reflections;
import org.reflections.scanners.Scanners;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;


/**
 * Command Line Argument parser.
 */
class Config {
    private static final int SCREEN_WIDTH_CHARACTERS = 120;

    // Evaluate application options
    @Option(name = "-?", aliases = {"--help", "-h"},
        usage = "Displays the help page",
        help = true)
    private boolean help;

    @Option(name = "-v", aliases = {"--verbose"},
        usage = "Verbose output")
    private boolean verbose;

    // EEL Context options
    @Option(name = "-e", aliases = {"--env", "--environment"},
        usage = "Add all the environment variable to the symbols table")
    private boolean environment;

    @Option(name = "-p", aliases = {"--props", "--properties"},
        usage = "Add all the Java properties to the symbols table")
    private boolean properties;

    private final Map<String, String> definitions = new HashMap<>();

    @Option(name = "--default",
        metaVar = "value",
        usage = "Set a default value that will be used if the symbols table does not contain a required key")
    private String defaultValue;

    @Option(name = "--timeout",
        metaVar = "number",
        usage = "timeout, in seconds, of a EEL expression or 0 to disable timeouts")
    private long timeout = EelContextSettingBuilder.DEFAULT_TIMEOUT.toSeconds();

    @Option(name = "--precision",
        metaVar = "number",
        usage = "Default precision used in calculations")
    private int precision = EelContext.DEFAULT_PRECISION;


    // EEL Symbol table options
    private final List<Class<?>> functionList = new ArrayList<>();

    private final List<Package> packageList = new ArrayList<>();

    // Evaluate Arguments
    @Argument(required = true, metaVar = "Expression",
        usage = "The expression to evaluate")
    private String expression = "";


    private final CmdLineParser parser;
    private boolean isValid;


    private Config() {
        ParserProperties parserProperties = ParserProperties.defaults()
                .withUsageWidth(SCREEN_WIDTH_CHARACTERS)
                .withAtSyntax(false)
                .withShowDefaults(true);

        this.parser = new CmdLineParser(this, parserProperties);
    }


    /**
     * Parse the command line arguments. This must be done before calling any other methods in this class
     * @param args          The command line arguments
     * @return              The singleton instance of this class
     */
    @Nonnull
    static Config parse(String... args) {
        return new Config()
            .parseArgs(args);
    }


    @Nonnull
    private Config parseArgs(@Nonnull String[] args) {
        try {
            parser.parseArgument(args);
            isValid = true;
        } catch (CmdLineException e) {
            isValid = false;
            System.err.println("Error: " + e.getMessage());
        }

        if (!isValid) {
            System.err.println();
            displayUsage(System.err);
        }

        return this;
    }


    @Option(name = "--defs", aliases = {"--definitions"},
        metaVar = "propertiesFile",
        usage = "Add all the definitions in a properties file to the symbols table")
    private void loadDefinitions(@Nonnull File propertiesFile) throws CmdLineException {
        Properties extra;

        try (
            FileInputStream source = new FileInputStream(propertiesFile);
        ) {
            extra = new EelProperties().load(source);
        } catch (IOException e) {
            throw new CmdLineException(parser, "Can not read file '" + propertiesFile.getAbsolutePath() + "'", e);
        }

        for (var entry : extra.entrySet()) {
            Object key = entry.getKey();
            Object value = entry.getValue();

            setDefinition(key.toString(), value.toString());
        }
    }

    @Option(name = "-D", aliases = {"--define"},
        metaVar = "key=value",
        usage = "Add a definition to the symbols table")
    private void setDefinition(@Nonnull String input) throws CmdLineException {
        String[] parts = input.split("=", 2);
        String key = parts[0];
        String value = parts.length == 1 ? "" : parts[1];
        setDefinition(key, value);
    }

    private void setDefinition(@Nonnull String key, @Nonnull String value) throws CmdLineException {
        String old = definitions.put(key, value);

        if (old != null) {
            throw new CmdLineException(parser, "'" + key + "' is already in the symbols table");
        }
    }

    @Option(name = "--udf-class",
        metaVar = "class-name",
        usage = "Add a class containing UDFs that EEL can call")
    private void setFunction(@Nonnull String className) throws CmdLineException {
        try {
            Class<?> target = Class.forName(className);

            functionList.add(target);
        } catch (Exception e) {
            throw new CmdLineException(parser, "Invalid function '" + className + "'", e);
        }
    }

    @Option(name = "--udf-package",
        metaVar = "package-name",
        usage = "Add a package containing UDFs that EEL can call")
    private void setFunctions(@Nonnull String packageName) throws CmdLineException {
        String packagePath = packageName.replace('.', '/');             // make sure package is known to class loader
        ConfigurationBuilder configurationBuilder = new ConfigurationBuilder()
            .addUrls(ClasspathHelper.forJavaClassPath())
            .filterInputsBy(x -> x.startsWith(packagePath))
            .addScanners(Scanners.TypesAnnotated);

        new Reflections(configurationBuilder)
            .getTypesAnnotatedWith(PackagedEelFunction.class);

        Package target = getClass().getClassLoader().getDefinedPackage(packageName);

        if (target == null) {
            throw new CmdLineException(parser, "Invalid package '" + packageName + "'");
        } else {
            packageList.add(target);
        }
    }

    /**
     * Dumps the command line syntax to {@link System#out}
     */
    public void displayUsage() {
        displayUsage(System.out);
    }


    private void displayUsage(@Nonnull PrintStream stream) {
        stream.println("Usage:");
        stream.println("  java -jar evaluate-<version>.jar" + parser.printExample(OptionHandlerFilter.ALL));
        stream.println();

        this.parser.printUsage(stream);
    }

    /**
     * Returns {@literal true} only of the command line was valid
     * @return {@literal true} only of the command line was valid
     */
    boolean isValid() {
        return isValid;
    }

    /**
     * Returns {@literal true} only if the user asked to see the help page
     * @return {@literal true} only if the user asked to see the help page
     */
    boolean requestHelp() {
        return help;
    }

    /**
     * Returns {@literal true} only if the Evaluate tool in is verbose mode
     * @return {@literal true} only if the Evaluate tool in is verbose mode
     */
    boolean verbose() {
        return verbose;
    }

    /**
     * Returns {@literal true} only if Environment Variables are made available to the EEL expression
     * @return {@literal true} only if Environment Variables are made available to the EEL expression
     */
    boolean useEnvironmentVariables() {
        return environment;
    }

    /**
     * Returns {@literal true} only if JVM properties are made available to the EEL expression
     * @return {@literal true} only if JVM properties are made available to the EEL expression
     */
    boolean useProperties() {
        return properties;
    }

    /**
     * Returns an immutable map of all Symbols table entries defined on the command line
     * @return an immutable map of all Symbols table entries defined on the command line
     */
    @Nonnull
    Map<String, String> definitions() {
        return Collections.unmodifiableMap(definitions);
    }


    /**
     * Returns the default value returned by the symbols table if a required key is not present
     * @return the default value returned by the symbols table if a required key is not present
     */
    @Nullable
    String defaultValue() {
        return defaultValue;
    }


    /**
     * Returns the maximum allowed duration of an EEL expression
     * @return the maximum allowed duration of an EEL expression
     */
    @Nonnull
    public Duration timeout() {
        return Duration.ofSeconds(timeout);
    }

    /**
     * Returns the numeric precision used in calculations.
     * If undefined the default is {@link EelContext#DEFAULT_PRECISION}
     * @return the numeric precision used in calculations.
     */
    int precision() {
        return precision;
    }

    /**
     * Returns an immutable list of all the ad-hoc functions added to EEL.
     * This is in addition to packages that have been added
     * @return an immutable list of all the ad-hoc functions added to EEL.
     * @see #packageList()
     */
    @Nonnull
    List<Class<?>> functionList() {
        return Collections.unmodifiableList(functionList);
    }

    /**
     * Returns an immutable list of all the additional packages that contain EEL functions.
     * This is in addition to ad-hoc functions
     * @return an immutable list of all the additional packages that contain EEL functions
     * @see #functionList()
     */
    @Nonnull
    List<Package> packageList() {
        return Collections.unmodifiableList(packageList);
    }

    /**
     * Returns the expression to evaluate
     * @return the expression to evaluate
     */
    @Nonnull
    String getExpression() {
        return expression;
    }
}
