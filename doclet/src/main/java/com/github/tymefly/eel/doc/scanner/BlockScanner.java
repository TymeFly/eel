package com.github.tymefly.eel.doc.scanner;

import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.github.tymefly.eel.doc.model.ElementGenerator;
import com.github.tymefly.eel.doc.model.TagGenerator;
import com.github.tymefly.eel.doc.model.TagType;
import com.github.tymefly.eel.doc.model.TextBlockGenerator;
import com.github.tymefly.eel.doc.source.Source;
import com.sun.source.doctree.AuthorTree;
import com.sun.source.doctree.DeprecatedTree;
import com.sun.source.doctree.DocTree;
import com.sun.source.doctree.HiddenTree;
import com.sun.source.doctree.ReferenceTree;
import com.sun.source.doctree.SeeTree;
import com.sun.source.doctree.SinceTree;
import com.sun.source.doctree.SummaryTree;
import com.sun.source.doctree.UnknownBlockTagTree;
import com.sun.source.doctree.VersionTree;

/**
 * JavaDoc scanner that handles all the text elements from a {@link InlineScanner} plus additional elements that only
 * apply to Java program elements
 */
abstract class BlockScanner extends InlineScanner {
    private final Source source;
    private final ElementGenerator<?> model;

    BlockScanner(@Nonnull Source source, @Nonnull ElementGenerator<?> model) {
        super(source, model);

        this.source = source;
        this.model = model;
    }


    // Tag - @summary
    // Provides a short summary to be used in the description
    @Override
    public Void visitSummary(@Nonnull SummaryTree node, @Nullable Void unused) {
        TextBlockGenerator section = TextBlockScanner.run(source, node.getSummary());

        model.addSummary(section);

        return super.visitSummary(node, unused);
    }


    // Tag - @see
    // cross-references to internal or external documentation
    @Override
    public Void visitSee(@Nonnull SeeTree node, @Nullable Void unused) {
        TagGenerator tag = model.addTag(TagType.SEE);
        List<? extends DocTree> nodes = node.getReference();

        if (!nodes.isEmpty() && nodes.get(0) instanceof ReferenceTree reference) {
            String signature = reference.getSignature();
            String fullSignature = source.resolveSignature(signature);

            tag.withReference(signature, fullSignature);
        }

        current = tag;

        return super.visitSee(node, unused);
    }


    // Tag: @author
    // Documents the author(s) of a class or method.
    @Override
    public Void visitAuthor(@Nonnull AuthorTree node, @Nullable Void unused) {
        current = model.addTag(TagType.AUTHOR);

        return super.visitAuthor(node, unused);
    }


    // Tag - @since
    // document when a class, method, or field was first introduced,
    @Override
    public Void visitSince(@Nonnull SinceTree node, @Nullable Void unused) {
        current = model.addTag(TagType.SINCE);

        return super.visitSince(node, unused);
    }

    // Tag - @version
    // Documents the version of the element.
    @Override
    public Void visitVersion(@Nonnull VersionTree node, @Nullable Void unused) {
        current = model.addTag(TagType.VERSION);

        return super.visitVersion(node, unused);
    }


    // Tag @deprecated
    // Notes that an element is deprecated.
    @Override
    public Void visitDeprecated(@Nonnull DeprecatedTree node, @Nullable Void unused) {
        current = model.addTag(TagType.DEPRECATED);

        return super.visitDeprecated(node, unused);
    }


    // tag: @hidden
    // Prevents an element from appearing in generated docs.
    @Override
    public Void visitHidden(@Nonnull HiddenTree node, @Nullable Void unused) {
        model.hide();

        return super.visitHidden(node, unused);
    }

    // For unrecognised @custom tags.
    @Override
    public Void visitUnknownBlockTag(@Nonnull UnknownBlockTagTree node, @Nullable Void unused) {
        current = model.addIgnoredBlock();

        source.context()
            .warn("Unexpected tag %s", node.getTagName());

        return super.visitUnknownBlockTag(node, unused);
    }
}
