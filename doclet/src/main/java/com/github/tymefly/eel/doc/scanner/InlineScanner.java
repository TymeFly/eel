package com.github.tymefly.eel.doc.scanner;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.lang.model.element.Element;
import javax.lang.model.element.VariableElement;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;

import com.github.tymefly.eel.doc.model.ParagraphGenerator;
import com.github.tymefly.eel.doc.source.Source;
import com.sun.source.doctree.AttributeTree;
import com.sun.source.doctree.DocRootTree;
import com.sun.source.doctree.DocTree;
import com.sun.source.doctree.EndElementTree;
import com.sun.source.doctree.EntityTree;
import com.sun.source.doctree.ErroneousTree;
import com.sun.source.doctree.InheritDocTree;
import com.sun.source.doctree.LinkTree;
import com.sun.source.doctree.LiteralTree;
import com.sun.source.doctree.ReferenceTree;
import com.sun.source.doctree.StartElementTree;
import com.sun.source.doctree.SystemPropertyTree;
import com.sun.source.doctree.TextTree;
import com.sun.source.doctree.UnknownInlineTagTree;
import com.sun.source.doctree.ValueTree;
import com.sun.source.util.DocTreePathScanner;


/**
 * Class that scans the in-line documentation such as blocks of text. The following methods are not overridden
 * <ul>
 *  <li>visitDocComment - This is used to process the entire source tree. We have no use for this </li>
 *  <li>visitComment - we will ignore html comments in the JavaDoc </li>
 *  <li>visitProvides and visitUses - EEL functions are not module based </li>
 *  <li>visitSerial, visitSerialData and visitSerialField - EEL can not access Java fields </li>
 *  <li>visitIdentifier - visitParam handles the identifiers </li>
 *  <li>visitIndex - EEL Doc does not support custom indexes </li>
 *  <li>visitReference - references (LINK, LINK_PLAIN, THROWS, EXCEPTION and VALUE) have their own visit functions </li>
 * </ul>
 */
abstract class InlineScanner extends DocTreePathScanner<Void, Void> {
    private final Source source;
    private final ParagraphGenerator<?> model;

    @SuppressWarnings("VisibilityModifier")
    ParagraphGenerator<?> current;

    InlineScanner(@Nonnull Source source, @Nonnull ParagraphGenerator<?> model) {
        this.source = source;
        this.model = model;

        this.current = model;
    }



    // Tag - {@docRoot}
    // Inserts the relative path to the root of the documentation. Useful in custom links or image paths.
    @Override
    public Void visitDocRoot(@Nonnull DocRootTree node, @Nullable Void unused) {
        current = current.withText(".");                        // All HTML pages are generated at the root level.

        return super.visitDocRoot(node, unused);
    }

    // Tag - HTML entities (e.g., &nbsp;)
    // Represents HTML character entities in Javadoc content.
    @Override
    public Void visitEntity(@Nonnull EntityTree node, @Nullable Void unused) {
        current = current.withText(node.toString());            // HTML entity references are generated 'as-is'

        return super.visitEntity(node, unused);
    }

    // Represents unrecognised or syntactically incorrect content in Javadoc. Used for error recovery.
    @Override
    public Void visitErroneous(@Nonnull ErroneousTree node, @Nullable Void unused) {
        Diagnostic<JavaFileObject> diagnostic = node.getDiagnostic();
        String fileName = diagnostic.getSource().getName();
        String tagName = extractTagName(node.toString());
        StringBuilder message = new StringBuilder("[").append(diagnostic.getKind()).append("] ");

        if (fileName != null) {
            message.append(fileName).append(": ");
        }

        message.append("Invalid JavaDoc ");

        if (tagName != null) {
            message.append("for tag ").append(tagName).append(' ');
        }

        message.append("- ").append(diagnostic.getMessage(null));

        source.context()
            .error(message.toString());

        return super.visitErroneous(node, unused);
    }

    @Nullable
    private String extractTagName(@Nonnull String text) {
        String tagName;
        int firstSpace = text.indexOf(' ');

        if (!text.startsWith("@")) {
            tagName = null;
        } else if (firstSpace > 0) {
            tagName = text.substring(0, firstSpace);
        } else {
            tagName = text;
        }

        return tagName;
    }


    // {@inheritDoc}
    // Inherits the corresponding documentation from a superclass or interface method.
    @Override
    public Void visitInheritDoc(@Nonnull InheritDocTree node, @Nullable Void unused) {
        source.context()
            .warn("@inheritDoc is not supported");

        return super.visitInheritDoc(node, unused);
    }


    // Tag - {@link}, {@linkplain}
    // Creates an inline hyperlink to another documented element (formatted with or without code style).
    @Override
    public Void visitLink(@Nonnull LinkTree node, @Nullable Void unused) {
        ReferenceTree ref = node.getReference();
        String signature = source.resolveSignature(ref.getSignature());
        DocTree.Kind kind = (signature == null ? null : node.getKind());

        if (kind == DocTree.Kind.LINK) {
            current = current.withLink(signature, signature);
        } else if (kind == DocTree.Kind.LINK_PLAIN) {
            current = current.withPlainLink(signature, signature);
        } else {
            current = current.withCode()
                .withText(ref.getSignature());
        }

        return super.visitLink(node, unused);
    }

    // Tag - {@literal}
    // Displays literal text by escaping HTML special characters (e.g., <, >).
    @Override
    public Void visitLiteral(@Nonnull LiteralTree node, @Nullable Void unused) {
        String tagName = node.getTagName();

        if ("code".equals(tagName)) {
            current = current.withCode();
        } else if ("literal".equals(tagName)) {
            current = current.withLiteral();
        } else {
            current = current.withErrorHighlight();
        }

        return super.visitLiteral(node, unused);
    }



    // HTML tag start (e.g. <p>)
    // Represents the start of an embedded HTML element.
    @Override
    public Void visitStartElement(@Nonnull StartElementTree node, @Nullable Void unused) {
        current = current.withHtml(node.toString());

        return super.visitStartElement(node, unused);
    }

    //HTML attributes (e.g., alt="desc")
    // Represents an attribute in a start HTML element (visitStartElement).
    @Override
    public Void visitAttribute(@Nonnull AttributeTree node, @Nullable Void unused) {
        current = current.withIgnoredText();

        return super.visitAttribute(node, unused);
    }

    // HTML tag end (e.g., </p>)
    // Represents the end of an embedded HTML element.
    @Override
    public Void visitEndElement(@Nonnull EndElementTree node, @Nullable Void unused) {
        current = current.withHtml(node.toString());

        return super.visitEndElement(node, unused);
    }


    // Tag - {@systemProperty}
    // Documents a system property (like my.app.config) in inline form.
    @Override
    public Void visitSystemProperty(@Nonnull SystemPropertyTree node, @Nullable Void unused) {
        current = current.withLiteral()
            .withText(node.getPropertyName().toString());

        return super.visitSystemProperty(node, unused);
    }


    // Represents regular textual content inside Javadoc comments.
    @Override
    public Void visitText(@Nonnull TextTree node, @Nullable Void unused) {
        String text = node.getBody()                // Remove any indenting in the comment text
            .replaceAll("[\\r\\n]+ *", " ");

        current = current.withText(text);

        return super.visitText(node, unused);
    }


    // Custom inline tag (e.g., {@custom})
    // Handles unrecognised or user-defined inline tags.
    @Override
    public Void visitUnknownInlineTag(@Nonnull UnknownInlineTagTree node, @Nullable Void unused) {
        current = current.withErrorHighlight();

        source.context()
            .warn("Unexpected tag %s", node.getTagName());

        return super.visitUnknownInlineTag(node, unused);
    }

    // Tag - {@value}
    // Displays the value of a constant field (e.g., public static final).
    @Override
    public Void visitValue(@Nonnull ValueTree valueTree, Void currentElement) {
        ReferenceTree referenceTree = valueTree.getReference();
        String signature = (referenceTree != null ? referenceTree.getSignature() : null);
        Element resolved = source.resolveElement(signature);
        String text;

        if (resolved instanceof VariableElement variable) {
            Object value = variable.getConstantValue();

            text = (value == null ? null : value.toString());
        } else {
            text = null;
        }

        if (text == null) {
            current = current.withErrorHighlight()
                .withText((signature != null ? signature : valueTree.toString()));
        } else {
            current = current.withText(text);
        }

        return super.visitValue(valueTree, currentElement);
    }

    // Called for any node type not specifically handled. Default fallback implementation.
    @Override
    public Void visitOther(@Nonnull DocTree node, @Nullable Void unused) {
        source.context()
            .error("[ERROR] Unexpected element in JavaDoc comment %s", node);

        current = model.addIgnoredBlock();

        return super.visitOther(node, unused);
    }
}
