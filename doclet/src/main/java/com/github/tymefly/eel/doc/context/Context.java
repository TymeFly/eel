package com.github.tymefly.eel.doc.context;

import javax.annotation.Nonnull;
import javax.lang.model.util.Elements;
import javax.tools.Diagnostic;

import com.github.tymefly.eel.doc.config.Config;
import com.github.tymefly.eel.doc.model.ModelManager;
import com.sun.source.util.DocTrees;
import jdk.javadoc.doclet.DocletEnvironment;
import jdk.javadoc.doclet.Reporter;

/**
 * The EelDoc context which provides access to global objects, settings and functions
 */
public class Context {
    private final DocletEnvironment environment;
    private final Reporter reporter;


    /**
     * Constructor for the context.
     * @param environment       The Doclet environment
     * @param reporter          The Doclet message reporter
     */
    public Context(@Nonnull DocletEnvironment environment, @Nonnull Reporter reporter) {
        this.environment = environment;
        this.reporter = reporter;
    }


    /**
     * Returns the JavaDoc {@link Elements} utility class.
     * This class provides methods for operating on {@link javax.lang.model.element.Element elements}.
     * @return a utility class to operate on elements.
     */
    @Nonnull
    public Elements elementUtils() {
        return environment.getElementUtils();
    }

    /**
     * Returns the JavaDoc {@link DocTrees} utility class.
     * This class provides methods to access {@code TreePath}s, {@code DocCommentTree}s etc
     * @return a utility class that operates on doc trees
     */
    @Nonnull
    public DocTrees docTrees() {
        return environment.getDocTrees();
    }

    /**
     * Returns the singleton instance of the model manager.
     * @return the singleton instance of the model manager.
     */
    @Nonnull
    public ModelManager modelManager() {
        return ModelManager.getInstance();
    }


    /**
     * Write a note level message via the Doclet reporter.
     * By default, Maven will filter out messages at this level
     * @param message   the message to report
     */
    public void note(@Nonnull String message) {
        reporter.print(Diagnostic.Kind.NOTE, message);
    }

    /**
     * Write a formatted note level message via the Doclet reporter.
     * By default, Maven will filter out messages at this level
     * @param message   the message to report
     * @param args      Arguments referenced by the {@code message}
     * @see String#format(String, Object...)
     */
    public void note(@Nonnull String message, Object... args) {
        note(message.formatted(args));
    }


    /**
     * Write a warning message via the Doclet reporter.
     * @param message   the message to report
     */
    public void warn(@Nonnull String message) {
        reporter.print(Diagnostic.Kind.WARNING, message);
    }

    /**
     * Write a formatted warning message via the Doclet reporter.
     * @param message   the message to report
     * @param args      Arguments referenced by the {@code message}
     * @see String#format(String, Object...)
     */
    public void warn(@Nonnull String message, Object... args) {
        warn(message.formatted(args));
    }



    /**
     * Write an error message via the Doclet reporter.
     * This will be reported at error level unless the {@link Config#ignoreErrors()} option has been enabled.
     * @param message   the message to report
     */
    public void error(@Nonnull String message) {
        boolean ignoreErrors = Config.getInstance().ignoreErrors();
        Diagnostic.Kind kind = (ignoreErrors ? Diagnostic.Kind.WARNING : Diagnostic.Kind.ERROR);

        reporter.print(kind, message);
    }

    /**
     * Write a formatted error message via the Doclet reporter.
     * This will be reported at error level unless the {@link Config#ignoreErrors()} option has been enabled.
     * @param message   the message to report
     * @param args      Arguments referenced by the {@code message}
     * @see String#format(String, Object...)
     */
    public void error(@Nonnull String message, Object... args) {
        error(message.formatted(args));
    }
}
