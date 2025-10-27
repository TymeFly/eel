package com.github.tymefly.eel.doc.scanner;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.github.tymefly.eel.annotation.VisibleForTesting;
import com.github.tymefly.eel.doc.model.FunctionGenerator;
import com.github.tymefly.eel.doc.model.TagType;
import com.github.tymefly.eel.doc.source.Parameter;
import com.github.tymefly.eel.doc.source.Source;
import com.sun.source.doctree.ParamTree;
import com.sun.source.doctree.ReferenceTree;
import com.sun.source.doctree.ReturnTree;
import com.sun.source.doctree.ThrowsTree;
import com.sun.source.util.DocTreePath;


/**
 * Scanner for scanning a function.
 * The entry point is {@link #run(Source, FunctionGenerator)}
 */
class FunctionScanner extends BlockScanner {
    private final Source source;
    private final FunctionGenerator functionModel;

    @VisibleForTesting
    FunctionScanner(@Nonnull Source source, @Nonnull FunctionGenerator functionModel) {
        super(source, functionModel);

        this.source = source;
        this.functionModel = functionModel;
    }


    static void run(@Nonnull Source source, @Nonnull FunctionGenerator functionModel) {
        DocTreePath docTreePath = source.docTreePath();
        FunctionScanner scanner = new FunctionScanner(source, functionModel);

        scanner.scan(docTreePath, null);
    }


    // tag: @param
    // Documents a parameter or type parameter.
    @Override
    public Void visitParam(@Nonnull ParamTree node, @Nullable Void unused) {
        String name = node.getName().toString();
        Parameter parameter = source.parameters().get(name);

        current = functionModel.addParameter(name, parameter);

        return super.visitParam(node, unused);
    }


    // Tag - @return
    // description of data returned
    @Override
    public Void visitReturn(@Nonnull ReturnTree node, @Nullable Void unused) {
        current = functionModel.addTag(TagType.RETURN);

        return super.visitReturn(node, unused);
    }

    // Tag - @throws / @exception
    // Describes exceptions a method can throw.
    @Override
    public Void visitThrows(@Nonnull ThrowsTree node, @Nullable Void unused) {
        ReferenceTree reference = node.getExceptionName();

        current = functionModel.addTag(TagType.THROWS)
            .withReference(reference.toString(), reference.getSignature());

        return super.visitThrows(node, unused);
    }
}
