package com.github.tymefly.eel.doc.scanner;

import java.util.List;

import javax.annotation.Nonnull;

import com.github.tymefly.eel.annotation.VisibleForTesting;
import com.github.tymefly.eel.doc.model.TextBlockGenerator;
import com.github.tymefly.eel.doc.source.Source;
import com.sun.source.doctree.DocTree;
import com.sun.source.util.DocTreePath;

/**
 * Scanner for blocks of text.
 * The entry point is {@link #scanText(List)}
 */
class TextBlockScanner extends InlineScanner {
    private final Source source;
    private final TextBlockGenerator model;

    @VisibleForTesting
    TextBlockScanner(@Nonnull Source source, @Nonnull TextBlockGenerator model) {
        super(source, model);

        this.source = source;
        this.model = model;
    }


    @Nonnull
    static TextBlockGenerator run(@Nonnull Source source, @Nonnull List<? extends DocTree> nodes) {
        TextBlockGenerator textBlock = source.context()
            .modelManager()
            .textBlock();
        TextBlockScanner scanner = new TextBlockScanner(source, textBlock);

        return scanner.scanText(nodes);
    }


    @VisibleForTesting
    @Nonnull
    TextBlockGenerator scanText(@Nonnull List<? extends DocTree> nodes) {
        for (var node : nodes) {
            scan(new DocTreePath(source.docTreePath(), node), null);
        }

        return model;
    }
}
