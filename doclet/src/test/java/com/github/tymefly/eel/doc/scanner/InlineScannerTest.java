package com.github.tymefly.eel.doc.scanner;

import javax.annotation.Nonnull;
import javax.lang.model.element.Element;
import javax.lang.model.element.Name;
import javax.lang.model.element.VariableElement;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;

import com.github.tymefly.eel.doc.context.EelDocContext;
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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.contains;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit Test {@link InlineScanner}
 */
public class InlineScannerTest {
    private Source source;
    private EelDocContext context;
    private ParagraphGenerator<?> model;
    private ParagraphGenerator<?> nextModel;

    private InlineScanner scanner;


    @BeforeEach
    public void setUp() {
        source = mock();
        context = mock();
        model = mock();
        nextModel = mock();

        when(source.context())
            .thenReturn(context);
        when(model.withText(anyString()))
            .thenAnswer(i -> nextModel);
        when(model.withLiteral())
            .thenAnswer(i -> nextModel);
        when(model.withCode())
            .thenAnswer(i -> nextModel);
        when(model.withErrorHighlight())
            .thenAnswer(i -> nextModel);
        when(model.withLink(anyString(), anyString()))
            .thenAnswer(i -> nextModel);
        when(model.withPlainLink(anyString(), anyString()))
            .thenAnswer(i -> nextModel);
        when(model.withHtml(anyString()))
            .thenAnswer(i -> nextModel);
        when(model.withIgnoredText())
            .thenAnswer(i -> nextModel);
        when(model.addIgnoredBlock())
            .thenAnswer(i -> nextModel);

        scanner = new InlineScanner(source, model) {                    // Use a concrete type
            @Override
            public Void scan(@Nonnull DocTree tree, @Nonnull Void p) {
                return null;
            }
        };
    }


    /**
     * Unit test {@link InlineScanner#visitDocRoot(DocRootTree, Void)}
     */
    @Test
    public void test_visitDocRoot() {
        DocRootTree node = mock(DocRootTree.class);

        scanner.visitDocRoot(node, null);

        verify(model).withText(".");
    }


    /**
     * Unit test {@link InlineScanner#visitEntity(EntityTree, Void)}
     */
    @Test
    public void test_visitEntity() {
        EntityTree node = mock(EntityTree.class);

        when(node.toString())
            .thenReturn("&nbsp;");

        scanner.visitEntity(node, null);

        verify(model).withText("&nbsp;");
    }



    /**
     * Unit test {@link InlineScanner#visitErroneous(ErroneousTree, Void)}
     */
    @Test
    public void test_visitErroneous_tagWithSpace() {
        ErroneousTree node = mock(ErroneousTree.class);
        Diagnostic<JavaFileObject> diagnostic = mock();
        JavaFileObject file = mock(JavaFileObject.class);

        when(node.toString())
            .thenReturn("@param x wrong");
        when(node.getDiagnostic())
            .thenReturn(diagnostic);
        when(diagnostic.getKind())
            .thenReturn(Diagnostic.Kind.ERROR);
        when(diagnostic.getSource())
            .thenReturn(file);
        when(file.getName())
            .thenReturn("Test.java");
        when(diagnostic.getMessage(null))
            .thenReturn("syntax error");

        scanner.visitErroneous(node, null);

        verify(context).error(contains("for tag @param"));
    }

    /**
     * Unit test {@link InlineScanner#visitErroneous(ErroneousTree, Void)}
     */
    @Test
    public void test_visitErroneous_tagWithoutSpace() {
        ErroneousTree node = mock(ErroneousTree.class);
        Diagnostic<JavaFileObject> diagnostic = mock();
        JavaFileObject file = mock(JavaFileObject.class);

        when(node.toString())
            .thenReturn("@deprecated");
        when(node.getDiagnostic())
            .thenReturn(diagnostic);
        when(diagnostic.getKind())
            .thenReturn(Diagnostic.Kind.WARNING);
        when(diagnostic.getSource())
            .thenReturn(file);
        when(file.getName())
            .thenReturn("Test.java");
        when(diagnostic.getMessage(null))
            .thenReturn("some warning");

        scanner.visitErroneous(node, null);

        verify(context).error(contains("for tag @deprecated"));
    }

    /**
     * Unit test {@link InlineScanner#visitErroneous(ErroneousTree, Void)}
     */
    @Test
    public void test_visitErroneous_textWithoutAtSymbol() {
        ErroneousTree node = mock(ErroneousTree.class);
        Diagnostic<JavaFileObject> diagnostic = mock();
        JavaFileObject file = mock(JavaFileObject.class);

        when(node.toString())
            .thenReturn("invalid text");
        when(node.getDiagnostic())
            .thenReturn(diagnostic);
        when(diagnostic.getKind())
            .thenReturn(Diagnostic.Kind.ERROR);
        when(diagnostic.getSource())
            .thenReturn(file);
        when(file.getName())
            .thenReturn("Test.java");
        when(diagnostic.getMessage(null))
            .thenReturn("syntax error");

        scanner.visitErroneous(node, null);

        verify(context).error(contains("Invalid JavaDoc"));
        verify(context, never()).error(contains("for tag"));    // tagName = null => should NOT append "for tag"
    }


    /**
     * Unit test {@link InlineScanner#visitInheritDoc(InheritDocTree, Void)}
     */
    @Test
    public void test_visitInheritDoc() {
        InheritDocTree node = mock(InheritDocTree.class);

        scanner.visitInheritDoc(node, null);

        verify(context).warn(contains("@inheritDoc"));
    }


    /**
     * Unit test {@link InlineScanner#visitLink(LinkTree, Void)}
     */
    @Test
    public void test_visitLink_normalLink() {
        LinkTree node = mock(LinkTree.class);
        ReferenceTree ref = mock(ReferenceTree.class);

        when(ref.getSignature())
            .thenReturn("MyClass#method");
        when(node.getReference())
            .thenReturn(ref);
        when(node.getKind())
            .thenReturn(DocTree.Kind.LINK);
        when(source.resolveSignature("MyClass#method"))
            .thenReturn("MyClass#method");

        scanner.visitLink(node, null);

        verify(model).withLink("MyClass#method", "MyClass#method");
    }


    /**
     * Unit test {@link InlineScanner#visitLink(LinkTree, Void)}
     */
    @Test
    public void test_visitLink_plainLink() {
        LinkTree node = mock(LinkTree.class);
        ReferenceTree ref = mock(ReferenceTree.class);

        when(ref.getSignature())
            .thenReturn("MyClass#plain");
        when(node.getReference())
            .thenReturn(ref);
        when(node.getKind())
            .thenReturn(DocTree.Kind.LINK_PLAIN);
        when(source.resolveSignature("MyClass#plain"))
            .thenReturn("MyClass#plain");

        scanner.visitLink(node, null);

        verify(model).withPlainLink("MyClass#plain", "MyClass#plain");
    }

    /**
     * Unit test {@link InlineScanner#visitLink(LinkTree, Void)}
     */
    @Test
    public void test_visitLink_unexpectedType() {
        LinkTree node = mock(LinkTree.class);
        ReferenceTree ref = mock(ReferenceTree.class);

        when(ref.getSignature())
            .thenReturn("MyClass#broken");
        when(node.getReference())
            .thenReturn(ref);
        when(source.resolveSignature("MyClass#broken"))                     // unresolved signature
            .thenReturn(null);

        scanner.visitLink(node, null);

        verify(model).withCode();
        verify(nextModel).withText("MyClass#broken");
    }


    /**
     * Unit test {@link InlineScanner#visitLiteral(LiteralTree, Void)}
     */
    @Test
    public void test_visitLiteral_codeTag() {
        LiteralTree node = mock(LiteralTree.class);

        when(node.getTagName())
            .thenReturn("code");

        scanner.visitLiteral(node, null);

        verify(model).withCode();
    }

    /**
     * Unit test {@link InlineScanner#visitLiteral(LiteralTree, Void)}
     */
    @Test
    public void test_visitLiteral_literalTag() {
        LiteralTree node = mock(LiteralTree.class);

        when(node.getTagName())
            .thenReturn("literal");

        scanner.visitLiteral(node, null);

        verify(model).withLiteral();
    }

    /**
     * Unit test {@link InlineScanner#visitLiteral(LiteralTree, Void)}
     */
    @Test
    public void test_visitLiteral_unexpectedTag() {
        LiteralTree node = mock(LiteralTree.class);

        when(node.getTagName())
            .thenReturn("somethingElse");

        scanner.visitLiteral(node, null);

        verify(model).withErrorHighlight();
    }


    /**
     * Unit test {@link InlineScanner#visitStartElement(StartElementTree, Void)} 
     */
    @Test
    public void test_visitStartElement() {
        StartElementTree node = mock(StartElementTree.class);

        when(node.toString())
            .thenReturn("<p>");
        scanner.visitStartElement(node, null);

        verify(model).withHtml("<p>");
    }

    
    /**
     * Unit test {@link InlineScanner#visitAttribute(AttributeTree, Void)} 
     */
    @Test
    public void test_visitAttribute() {
        AttributeTree node = mock(AttributeTree.class);

        scanner.visitAttribute(node, null);

        verify(model).withIgnoredText();
    }

    
    /**
     * Unit test {@link InlineScanner#visitEndElement(EndElementTree, Void)} 
     */
    @Test
    public void test_visitEndElement() {
        EndElementTree node = mock(EndElementTree.class);

        when(node.toString())
            .thenReturn("</p>");
        scanner.visitEndElement(node, null);

        verify(model).withHtml("</p>");
    }

    
    /**
     * Unit test {@link InlineScanner#visitSystemProperty(SystemPropertyTree, Void)} 
     */
    @Test
    public void test_visitSystemProperty() {
        SystemPropertyTree node = mock(SystemPropertyTree.class);
        Name name = mock(Name.class);

        when(name.toString())
            .thenReturn("my.app.config");
        when(node.getPropertyName())
            .thenReturn(name);

        scanner.visitSystemProperty(node, null);

        verify(model).withLiteral();
        verify(nextModel).withText("my.app.config");
    }

    
    /**
     * Unit test {@link InlineScanner#visitText(TextTree, Void)} 
     */
    @Test
    public void test_visitText() {
        TextTree node = mock(TextTree.class);

        when(node.getBody())
            .thenReturn("Line1\n  Line2");

        scanner.visitText(node, null);

        verify(model).withText("Line1 Line2");
    }

    
    /**
     * Unit test {@link InlineScanner#visitUnknownInlineTag(UnknownInlineTagTree, Void)} 
     */
    @Test
    public void test_visitUnknownInlineTag() {
        UnknownInlineTagTree node = mock(UnknownInlineTagTree.class);

        when(node.getTagName())
            .thenReturn("custom");

        scanner.visitUnknownInlineTag(node, null);

        verify(model).withErrorHighlight();
        verify(context).warn(contains("Unexpected tag"), eq("custom"));
    }

    
    /**
     * Unit test {@link InlineScanner#visitValue(ValueTree, Void)} 
     */
    @Test
    public void test_visitValue_constant() {
        ValueTree node = mock(ValueTree.class);
        ReferenceTree ref = mock(ReferenceTree.class);
        VariableElement element = mock(VariableElement.class);

        when(ref.getSignature())
            .thenReturn("MyClass.CONST");
        when(node.getReference())
            .thenReturn(ref);
        when(source.resolveElement("MyClass.CONST"))
            .thenReturn(element);
        when(element.getConstantValue())
            .thenReturn("42");

        scanner.visitValue(node, null);

        verify(model).withText("42");
    }

    /**
     * Unit test {@link InlineScanner#visitValue(ValueTree, Void)} 
     */
    @Test
    public void test_visitValue_notConstant() {
        ValueTree node = mock(ValueTree.class);
        ReferenceTree ref = mock(ReferenceTree.class);
        VariableElement element = mock(VariableElement.class);

        when(ref.getSignature())
            .thenReturn("MyClass.NULLCONST");
        when(node.getReference())
            .thenReturn(ref);
        when(source.resolveElement("MyClass.NULLCONST"))
            .thenReturn(element);
        when(element.getConstantValue())
            .thenReturn(null);

        scanner.visitValue(node, null);

        verify(model).withErrorHighlight();
        verify(nextModel).withText("MyClass.NULLCONST");
    }

    /**
     * Unit test {@link InlineScanner#visitValue(ValueTree, Void)} 
     */
    @Test
    public void test_visitValue_notVariable() {
        ValueTree node = mock(ValueTree.class);
        ReferenceTree ref = mock(ReferenceTree.class);
        Element element = mock(Element.class);                          // not a VariableElement

        when(ref.getSignature())
            .thenReturn("MyClass.NOTVAR");
        when(node.getReference())
            .thenReturn(ref);
        when(source.resolveElement("MyClass.NOTVAR"))
            .thenReturn(element);

        scanner.visitValue(node, null);

        verify(model).withErrorHighlight();
        verify(nextModel).withText("MyClass.NOTVAR");
    }

    /**
     * Unit test {@link InlineScanner#visitValue(ValueTree, Void)} 
     */
    @Test
    public void test_visitValue_noReferenceTree() {
        ValueTree node = mock(ValueTree.class);

        when(node.getReference())
            .thenReturn(null);
        when(node.toString())
            .thenReturn("{@value something}");

        scanner.visitValue(node, null);             // no element resolved, signature is null

        verify(model).withErrorHighlight();
        verify(nextModel).withText("{@value something}");
    }

    
    /**
     * Unit test {@link InlineScanner#visitOther(DocTree, Void)}
     */
    @Test
    public void test_visitOther() {
        DocTree node = mock(DocTree.class);

        when(node.toString())
            .thenReturn("something");

        scanner.visitOther(node, null);

        verify(context).error(contains("Unexpected element"), eq(node));
        verify(model).addIgnoredBlock();
    }
}
