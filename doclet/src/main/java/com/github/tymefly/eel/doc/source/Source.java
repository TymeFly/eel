package com.github.tymefly.eel.doc.source;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.PackageElement;

import com.github.tymefly.eel.doc.context.Context;
import com.github.tymefly.eel.doc.utils.EelType;
import com.github.tymefly.eel.validate.Preconditions;
import com.sun.source.doctree.DocCommentTree;
import com.sun.source.util.DocTreePath;

/**
 * Describes the source code for a method or package
 */
public class Source {
    private final Context context;
    private final Element element;
    private final ParameterList parameters;
    private final EelType eelType;
    private final String signature;
    private final DocCommentTree docCommentTree;
    private final Resolver resolver;


    private Source(@Nonnull Context context,
                   @Nonnull Element element,
                   @Nonnull ParameterList parameters,
                   @Nullable EelType eelType) {
        this.context = context;
        this.element = element;
        this.parameters = parameters;
        this.eelType = eelType;

        this.signature = Signature.of(element);
        this.docCommentTree = context.docTrees()
            .getDocCommentTree(element);
        this.resolver = new Resolver(context, element);
    }


    /**
     * Factory method for package sources
     * @param context   the EelDoc context object
     * @param element   the element being described
     * @return          a fluent interface
     */
    @Nonnull
    public static Source forPackage(@Nonnull Context context, @Nonnull PackageElement element) {
        return new Source(context, element, ParameterList.EMPTY, null);
    }


    /**
     * Factory method for method sources
     * @param context   the EelDoc context object
     * @param element   the element being described
     * @return          a fluent interface
     */
    @Nonnull
    public static Source forMethod(@Nonnull Context context, @Nonnull ExecutableElement element) {
        return new Source(context,
            element,
            ParameterList.build(element),
            TranslateType.toEel(element.getReturnType()));
    }

    /**
     * Returns the EelDoc context
     * @return the EelDoc context
     */
    @Nonnull
    public Context context() {
        return context;
    }


    /**
     * Returns the parameter list for the method. An empty list is returned for a package
     * @return the parameter list for the method
     */
    @Nonnull
    public ParameterList parameters() {
        return parameters;
    }

    /**
     * Returns the DocTreePath for the method
     * @return the DocTreePath for the method
     */
    @Nonnull
    public DocTreePath docTreePath() {
        return new DocTreePath(context.docTrees().getPath(element), docCommentTree());
    }

    /**
     * Returns {@literal true} only if the source code has documentation for the method
     * @return {@literal true} only if the source code has documentation for the method
     */
    public boolean hasDocCommentTree() {
        return (docCommentTree != null);
    }

    /**
     * Returns the Java documentation for the method.
     * @return the Java documentation for the method.
     * @throws IllegalStateException if there is no documentation for this method
     * @see #hasDocCommentTree()
     */
    @Nonnull
    public DocCommentTree docCommentTree() throws IllegalStateException {
        Preconditions.checkState(docCommentTree != null, "Element has no comments");

        return docCommentTree;
    }

    /**
     * Returns another element in the source tree based on the signature used by this source method. If no
     * such element exists, or the signature is {@literal null} then {@literal null} is returned
     * @param signature     the signature of the other element in the source code, as it is seen by this source method
     * @return              another element in the source tree, or {@literal null} if there is no such element
     */
    @Nullable
    public Element resolveElement(@Nullable String signature) {
        return (signature == null ? null : resolver.resolve(signature));
    }

    /**
     * Returns the fully qualified signature of another element in the source tree. If no
     * such element exists, or the signature is {@literal null} then {@literal null} is returned
     * @param signature     the signature of the other element in the source code, as it is seen by this source method
     * @return              the fully qualified signature of another element in the source tree, or {@literal null}
     *                       if there is no such element
     */
    @Nullable
    public String resolveSignature(@Nullable String signature) {
        Element resolved = resolveElement(signature);
        String full = (resolved == null ? null : Signature.of(resolved));

        return full;
    }

    /**
     * Returns the fully qualified signature of the source method
     * @return the fully qualified signature of the source method
     */
    @Nonnull
    public String signature() {
        return signature;
    }

    /**
     * Returns the EEL type that the source method returns.
     * {@literal null} is returned if the method doesn't return an Eel type or the source is for a package
     * @return the EEL type that the source method returns
     */
    @Nullable
    public EelType eelType() {
        return eelType;
    }
}
