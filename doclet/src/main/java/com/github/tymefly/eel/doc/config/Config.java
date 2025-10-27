package com.github.tymefly.eel.doc.config;


import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.Set;

import javax.annotation.Nonnull;

import com.github.tymefly.eel.annotation.VisibleForTesting;
import jdk.javadoc.doclet.Doclet;

/**
 * A singleton class that manages all the JavaDoc options for configuring the generated documentation.
 */
public class Config {
    private static final Config INSTANCE = new Config();

    private final Option version = Option.builder("-version")
        .withDescription("Print EEL Doc version information")
        .asExtended()
        .build();
    private final Option directory = Option.builder("-d")
        .withDescription("Destination directory for output files")
        .withParameter("directory")
        .build();
    private final Option title = Option.builder("-windowtitle")
        .withDescription("Browser window title")
        .withParameter("eel-expression")
        .withDefault("EEL-Doc")
        .asExpression()
        .build();
    private final Option docTitle = Option.builder("-doctitle")
        .withDescription("Include title for the overview page")
        .withParameter("eel-expression")
        .withDefault("EEL Functions")
        .asExpression()
        .build();
    private final Option overview = Option.builder("-doc-overview")
        .withDescription("Include overview documentation from a HTML file")
        .withParameter("file")
        .build();
    private final Option top = Option.builder("-top")
        .withDescription("Include top text on each page")
        .withParameter("eel-expression")
        .asExpression()
        .build();
    private final Option bottom = Option.builder("-bottom")
        .withDescription("Include bottom text on each page")
        .withParameter("eel-expression")
        .asExpression()
        .build();
    private final Option charSet =  Option.builder("-charset")
        .withDescription("Charset for cross-platform viewing of generated documentation")
        .withParameter("charset")
        .withDefault(StandardCharsets.UTF_8.displayName())
        .build();
    private final Option docEncoding =  Option.builder("-docencoding")
        .withDescription("Specify the character encoding for the output")
        .withParameter("charset")
        .withDefault(StandardCharsets.UTF_8.displayName())
        .build();
    private final Option author = Option.builder("-author")
        .withDescription("Include @author paragraphs")
        .build();
    private final Option ignoreErrors = Option.builder("-Ewarn")
        .withDescription("Report errors as warnings")
        .asExtended()
        .build();
    private final Option allReferences = Option.builder("-allrefs")
        .withDescription("Include all @see references, even if they do not refer to EEL functions")
        .asExtended()
        .build();

    private final Option use = Option.builder("-use")                   // Included to keep maven happy
        .withDescription("Not used")
        .build();
    private final Option noFonts = Option.builder("--no-fonts")         // Included to keep maven happy
        .withDescription("Not used")
        .build();
    private final Option docLint = Option.builder("-Xdoclint:")         // Included to keep maven happy
        .withDescription("Not used")
        .asExtended()
        .build();




    @VisibleForTesting
    Config() {
    }

    /**
     * Returns the singleton instance of this class
     * @return the singleton instance of this class
     */
    @Nonnull
    public static Config getInstance() {
        return INSTANCE;
    }


    /**
     * Returns all the supported Doclet options
     * @return all the supported Doclet options
     */
    @Nonnull
    public Set<Doclet.Option> options() {
        return Set.of(version, directory, title, top, bottom, docTitle, overview,
                      charSet, docEncoding, author, ignoreErrors, allReferences,
                      use, docLint, noFonts);
    }


    /**
     * Returns the destination directory for output files
     * @return the destination directory for output files
     */
    @Nonnull
    public File targetDirectory() {
        return new File(directory.value());
    }

    /**
     * Returns the browser window title
     * @return the browser window title
     */
    @Nonnull
    public String title() {
        return title.value();
    }

    /**
     * Returns the title displayed on the overview page
     * @return the title displayed on the overview page
     */
    @Nonnull
    public String docTitle() {
        return docTitle.value();
    }

    /**
     * Returns the optional text to display on the top of each generated page
     * @return the optional text to display on the top of each generated page
     */
    @Nonnull
    public Optional<String> top() {
        return top.optionalValue();
    }

    /**
     * Returns the optional text to display on the bottom of each generated page
     * @return the optional text to display on the bottom of each generated page
     */
    @Nonnull
    public Optional<String> bottom() {
        return bottom.optionalValue();
    }

    /**
     * Returns the Charset used for cross-platform viewing of generated documentation
     * @return the Charset used for cross-platform viewing of generated documentation
     */
    @Nonnull
    public String charSet() {
        return charSet.value();
    }

    /**
     * Returns the name of the character set used to encode the generated files
     * @return the name of the character set used to encode the generated files
     */
    @Nonnull
    public String docEncoding() {
        return docEncoding.value();
    }

    /**
     * Returns {@literal true} only if the option to display @author tags was set
     * @return {@literal true} only if the option to display @author tags was set
     */
    public boolean author() {
        return author.isSet();
    }

    /**
     * Returns {@literal true} only if the option to display the version was set
     * @return {@literal true} only if the option to display the version was set
     */
    public boolean version() {
        return version.isSet();
    }

    /**
     * Returns {@literal true} only if error messages should be downgraded to warnings
     * @return {@literal true} only if error messages should be downgraded to warnings
     */
    public boolean ignoreErrors() {
        return ignoreErrors.isSet();
    }

    /**
     * Returns {@literal true} only if all references, including broken references, are to be documented
     * @return {@literal true} only if all references, including broken references, are to be documented
     */
    public boolean allReferences() {
        return allReferences.isSet();
    }

    /**
     * Returns the optional overview file containing additional information for the index page
     * @return the optional overview file containing additional information for the index page
     */
    @Nonnull
    public Optional<File> overview() {
        return overview.optionalValue(File::new);
    }
}
