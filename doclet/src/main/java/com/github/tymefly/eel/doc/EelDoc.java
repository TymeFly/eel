package com.github.tymefly.eel.doc;

import java.util.Locale;
import java.util.Set;

import javax.annotation.Nonnull;
import javax.lang.model.SourceVersion;

import com.github.tymefly.eel.Eel;
import com.github.tymefly.eel.Metadata;
import com.github.tymefly.eel.doc.config.Config;
import com.github.tymefly.eel.doc.context.EelDocContext;
import com.github.tymefly.eel.doc.report.Report;
import com.github.tymefly.eel.doc.scanner.RootScanner;
import jdk.javadoc.doclet.Doclet;
import jdk.javadoc.doclet.DocletEnvironment;
import jdk.javadoc.doclet.Reporter;

/**
 * Entry point for EelDoc Doclets. This is called by the Javadoc tool.
 * @see <a href="https://openjdk.org/groups/compiler/using-new-doclet.html">Doclets architecture overview</a>
 * @see <a
 *     href="https://docs.oracle.com/en/java/javase/17/docs/api/jdk.javadoc/jdk/javadoc/doclet/package-summary.html">
 *     Overview on how this Doclet will be called</a>
 */
public class EelDoc implements Doclet {
    private Reporter reporter;


    @Override
    public void init(@Nonnull Locale locale, @Nonnull Reporter reporter) {
        this.reporter = reporter;
    }

    @Override
    public String getName() {
        return getClass().getSimpleName();
    }

    @Override
    public Set<? extends Option> getSupportedOptions() {
        return Config.getInstance().options();
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latest();
    }

    @Override
    public boolean run(@Nonnull DocletEnvironment environment) {
        EelDocContext context = new EelDocContext(environment, reporter);

        if (Config.getInstance().version()) {
            showVersion(context);
        } else {
            execute(context, environment);
        }

        return true;
    }

    private void showVersion(@Nonnull EelDocContext context) {
        Metadata metadata = Eel.metadata();

        context.note("Eel Doclet version %s, built on %s",
            metadata.version(),
            metadata.buildDate());
    }


    private void execute(@Nonnull EelDocContext context, @Nonnull DocletEnvironment environment) {
        for (var element : environment.getIncludedElements()) {
            RootScanner.run(context, element);
        }

        new Report(context)
            .writeReport();

        int groups = context.modelManager()
            .groups()
            .size();
        long functions = context.modelManager()
            .groups()
            .stream()
            .mapToLong(group -> group.getFunctions().size())
            .sum();

        context.note("Generated documentation for " + functions + " functions in " + groups + " groups");
    }
}