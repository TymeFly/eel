package com.github.tymefly.eel.doc.report;

import java.io.File;
import java.nio.charset.Charset;
import java.util.List;

import javax.annotation.Nonnull;

import com.github.tymefly.eel.annotation.VisibleForTesting;
import com.github.tymefly.eel.doc.config.Config;
import com.github.tymefly.eel.doc.context.EelDocContext;
import com.github.tymefly.eel.doc.utils.FileUtils;

/**
 * The class that writes the HTML pages describing the EEL documentation to the file system.
 */
public class Report {
    private static final List<String> RESOURCES = List.of(
        "main.css",
        "eel-doc.js",
        "up.png",
        "up-hover.png",
        "icon.png");

    private final EelDocContext context;


    /**
     * Constructor
     * @param context       The documentation context
     */
    public Report(@Nonnull EelDocContext context) {
        this.context = context;
    }


    /**
     * Write all the required files to the file system. This includes the index page, the overview page,
     * pages for each group of functions and any additional "resources" (such and CSS, images or JavaScript)
     * that are required.
     */
    public void writeReport() {
        context.note("Writing to %s", Config.getInstance().targetDirectory().getAbsolutePath());

        writeOverview();
        writeGroups();
        writeIndex();

        copyResources();
    }


    private void writeOverview() {
        AbstractPage builder = new OverviewPage(context, "Overview");

        writePage(builder, "index.html");
    }


    private void writeGroups() {
        for (var group : context.modelManager().groups()) {
            String name = group.name();
            AbstractPage builder = new GroupPage(context, name, group);
            String targetFile = group.fileName();

            writePage(builder, targetFile);
        }
    }


    private void writeIndex() {
        AbstractPage builder = new IndexPage(context, "Index");

        writePage(builder, "_index.html");
    }


    @VisibleForTesting
    void writePage(@Nonnull AbstractPage page, @Nonnull String targetFile) {
        String html = page.buildPage();
        String fileEncoding = Config.getInstance().docEncoding();
        File file = new File(Config.getInstance().targetDirectory(), targetFile);

        if (html != null) {
            FileUtils.write(file, html, Charset.forName(fileEncoding));
        }
    }


    private void copyResources() {
        File root = Config.getInstance().targetDirectory();
        File resources = new File(root, "resource");

        RESOURCES.forEach(resource -> {
            File target = new File(resources, resource);

            FileUtils.copyResource(resource, target);
        });
    }
}


