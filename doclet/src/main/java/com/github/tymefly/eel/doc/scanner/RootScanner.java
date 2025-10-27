package com.github.tymefly.eel.doc.scanner;

import javax.annotation.Nonnull;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.PackageElement;

import com.github.tymefly.eel.doc.context.Context;
import com.github.tymefly.eel.doc.model.FunctionGenerator;
import com.github.tymefly.eel.doc.model.GroupGenerator;
import com.github.tymefly.eel.doc.model.ModelManager;
import com.github.tymefly.eel.doc.model.TextBlockGenerator;
import com.github.tymefly.eel.doc.source.Source;
import com.github.tymefly.eel.udf.EelFunction;
import com.github.tymefly.eel.udf.GroupDescription;

/**
 * Root level
 */
public class RootScanner {
    private final Context context;


    private RootScanner(@Nonnull Context context) {
        this.context = context;
    }


    /**
     * Entry point for scanning the Java source code
     * @param context   EelDoc context object
     * @param element   The root element in the Java source code
     */
    public static void run(@Nonnull Context context, @Nonnull Element element) {
        new RootScanner(context)
            .scan(element);
    }

    private void scan(@Nonnull Element element) {
        ElementKind kind = element.getKind();

        if (element instanceof PackageElement packageElement) {
            GroupDescription description = packageElement.getAnnotation(GroupDescription.class);

            if (description != null) {
                processPackage(packageElement, description);
            }
        } else if ((kind == ElementKind.CLASS) || (kind == ElementKind.RECORD)) {
            for (var enclosed : element.getEnclosedElements()) {
                scan(enclosed);
            }
        } else if (element instanceof ExecutableElement exe) {
            EelFunction eelFunction = element.getAnnotation(EelFunction.class);

            if (eelFunction != null) {
                processFunction(exe, eelFunction);
            }
        } else {
            // Do Nothing - EelDoc doesn't report on other element types
        }
    }

    private void processPackage(@Nonnull PackageElement packageElement, @Nonnull GroupDescription description) {
        Source source = Source.forPackage(context, packageElement);

        if (source.hasDocCommentTree()) {
            String name = description.value();
            GroupGenerator group = context.modelManager().group(name);

            if (group.hasDescription()) {
                context.error("Multiple descriptions for group " + name);
            } else {
                GroupGenerator model = context.modelManager()
                    .group(name)
                    .withDescription();

                PackageScanner.run(source, model);

                // If there is no summary tag generate a summary based on the first sentence
                if (!model.hasSummary()) {
                    TextBlockGenerator summary =
                        TextBlockScanner.run(source, source.docCommentTree().getFirstSentence());

                    model.addSummary(summary);
                }
            }
        }
    }

    private void processFunction(@Nonnull ExecutableElement element, @Nonnull EelFunction eelFunction) {
        String name = eelFunction.value();
        ModelManager manager = context.modelManager();

        if (!manager.hasFunction(name)) {           // Ignore functions that have already been processed
            Source source = Source.forMethod(context, element);
            FunctionGenerator functionModel = manager.addFunction(name, source.eelType(), source.signature());

            if (source.hasDocCommentTree()) {
                documented(source, functionModel);
            } else {
                undocumented(source, functionModel);
            }
        }
    }


    private void undocumented(@Nonnull Source source, @Nonnull FunctionGenerator functionModel) {
        source.parameters()
            .stream()
            .forEach(param -> functionModel.addParameter(param.name(), param));
    }


    private void documented(@Nonnull Source source, @Nonnull FunctionGenerator functionModel) {
        FunctionScanner.run(source, functionModel);

        // Make sure all parameters have been defined.
        source.parameters()
            .stream()
            .forEach(param -> functionModel.addParameter(param.name(), param));

        // If there is no summary tag generate a summary based on the first sentence
        if (!functionModel.hasSummary()) {
            TextBlockGenerator summary = TextBlockScanner.run(source, source.docCommentTree().getFirstSentence());

            functionModel.addSummary(summary);
        }
    }
}
