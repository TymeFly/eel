package com.github.tymefly.eel.doc.scanner;

import javax.annotation.Nonnull;

import com.github.tymefly.eel.doc.model.GroupGenerator;
import com.github.tymefly.eel.doc.source.Source;
import com.sun.source.util.DocTreePath;

/**
 * Scan a {@literal package-info.java} page to build up the description of an EEL group
 */
public class PackageScanner extends BlockScanner {
    private PackageScanner(@Nonnull Source source, @Nonnull GroupGenerator model) {
        super(source, model);
    }


    static void run(@Nonnull Source source, @Nonnull GroupGenerator model) {
        DocTreePath docTreePath = source.docTreePath();
        PackageScanner scanner = new PackageScanner(source, model);

        scanner.scan(docTreePath, null);
    }
}
