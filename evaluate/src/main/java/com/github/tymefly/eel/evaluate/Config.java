package com.github.tymefly.eel.evaluate;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.time.DayOfWeek;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
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
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
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
@SuppressWarnings({"PMD.UnusedPrivateMethod", "PMD.ImmutableField"})            // accessed by reflection
class Config {
    private static final int SCREEN_WIDTH_CHARACTERS = 120;
    private static final int DEFAULT_MIN_DAYS_FIRST_WEEK = 4;                   // See java.time.temporal.WeekFields.ISO
    private static final DayOfWeek DEFAULT_START_OF_WEEK = DayOfWeek.MONDAY;    // See java.time.temporal.WeekFields.ISO


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

    @Option(name = "--io-limit",
        metaVar = "number",
        usage = "Maximum number of bytes an EEL function can read")
    private int ioLimit = EelContext.DEFAULT_IO_LIMIT;

    private DayOfWeek startOfWeek = DEFAULT_START_OF_WEEK;

    @Option(name = "--days-in-first-week",
        metaVar = "[1 -> 7]",
        usage = "Sets the minimal number of days in the first week of the year")
    private int daysInFirstWeek = DEFAULT_MIN_DAYS_FIRST_WEEK;

    @Option(name = "--script",
        metaVar = "script",
        usage = "File containing an EEL expression to evaluate")
    private File scriptFile;


    @Option(name = "--version",
        usage = "Write EEL version and exit")
    private boolean version = false;


    // EEL Symbol table options
    private final List<Class<?>> functionList = new ArrayList<>();
    private final List<Package> packageList = new ArrayList<>();

    // Expression to evaluate.
    // This is not annotated "required = true" because user could ask for --version or --help
    @Argument(metaVar = "Expression", usage = "The expression to evaluate")
    private String expression;


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
            isValid = (help || version);
            isValid |= (expression != null) ^ (scriptFile != null);
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
            FileInputStream source = new FileInputStream(propertiesFile)
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

    @SuppressFBWarnings(value = "UPM_UNCALLED_PRIVATE_METHOD", justification = "Called by args4J via reflection")
    @Option(name = "--start-of-week",
        metaVar = "day",
        usage = "Sets the first day of the week (default: MONDAY)")
    private void setStartOfWeek(@Nonnull String name) throws CmdLineException {
        try {
            this.startOfWeek = DayOfWeek.valueOf(name.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new CmdLineException("Invalid DayOfWeek: " + name);
        }
    }

    @SuppressFBWarnings(value = "UPM_UNCALLED_PRIVATE_METHOD", justification = "Called by args4J via reflection")
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
        stream.println();
        stream.println("Exit status:");
        Arrays.stream(State.values())
            .sorted(Comparator.comparing(State::getReturnCode))
            .forEach(s -> stream.printf("  %2d    %s%n", s.getReturnCode(), s.description()));

        stream.println();
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
     * Returns {@literal true} only if the user asked to see the version information
     * @return {@literal true} only if the user asked to see the version information
     */
    boolean requestVersion() {
        return version;
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
     * Returns the maximum number of bytes that an EEL function can read.
     * If undefined the default is {@link EelContext#DEFAULT_IO_LIMIT}
     * @return the maximum number of bytes that an EEL function can read.
     */
    int ioLimit() {
        return ioLimit;
    }

    /**
     * Returns the first day of the week, which is used in date-based calculations.
     * @return the first day of the week
     */
    @Nonnull
    DayOfWeek startOfWeek() {
        return startOfWeek;
    }

    /**
     * Sets the minimal number of days in the first week. This must be in the range from 1 to 7
     * By default, this is {@literal 4} to match ISO-8601
     * @return the minimal number of days in the first week.
     */
    int daysInFirstWeek() {
        return daysInFirstWeek;
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
     * Returns an optional file containing the expression to evaluate
     * @return an optional file containing the expression to evaluate
     */
    @Nullable
    public File getScriptFile() {
        return scriptFile;
    }

    /**
     * Returns the optional expression to evaluate
     * @return the optional expression to evaluate
     */
    @Nullable
    String getExpression() {
        return expression;
    }
}
