package com.github.tymefly.eel.doc.source;

import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.PackageElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;

import com.github.tymefly.eel.doc.context.EelDocContext;
import com.github.tymefly.eel.doc.utils.EelType;
import com.sun.source.doctree.DocCommentTree;
import com.sun.source.util.DocTreePath;
import com.sun.source.util.TreePath;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

/**
 * Unit test for {@link Source}
 */
public class SourceTest {
    private EelDocContext context;

    private PackageElement packageElement;
    private ExecutableElement methodElement;

    private DocCommentTree methodDocTree;
    private TreePath methodTreePath;


    @BeforeEach
    public void setUp() {
        TypeMirror returnType = mock();

        context = mock(EelDocContext.class, RETURNS_DEEP_STUBS);
        packageElement = mock(PackageElement.class, RETURNS_DEEP_STUBS);
        methodElement = mock(ExecutableElement.class, RETURNS_DEEP_STUBS);
        methodDocTree = mock();
        methodTreePath = mock();

        when(context.docTrees().getDocCommentTree(packageElement))
            .thenReturn(null);
        when(context.docTrees().getDocCommentTree(methodElement))
            .thenReturn(methodDocTree);

        when(methodElement.getReturnType())
            .thenReturn(returnType);
        when(packageElement.getQualifiedName().toString())
            .thenReturn("my.package");

        when(context.docTrees().getPath(methodElement))
            .thenReturn(methodTreePath);

        when(returnType.getKind())
            .thenReturn(TypeKind.INT);
    }


    /**
     * Unit test {@link Source#context()}
     */
    @Test
    public void test_context() {
        assertSame(context, Source.forPackage(context, packageElement).context(), "For package");
        assertSame(context, Source.forMethod(context, methodElement).context(), "For method");
    }

    /**
     * Unit test {@link Source#parameters()}
     */
    @Test
    public void test_parameters() {
        assertSame(ParameterList.EMPTY, Source.forPackage(context, packageElement).parameters(), "For package");

        ParameterList parameters = mock();

        try (
            MockedStatic<ParameterList> parameterList = mockStatic(ParameterList.class)
        ) {
            parameterList.when(() -> ParameterList.build(methodElement))
                .thenReturn(parameters);


            assertSame(parameters, Source.forMethod(context, methodElement).parameters(), "For method");
        }
    }

    /**
     * Unit test {@link Source#docTreePath()}
     */
    @Test
    public void test_docTreePath() {
        final TreePath[] treePath = new TreePath[1];
        final DocCommentTree[] tree = new DocCommentTree[1];

        try (
            MockedConstruction<DocTreePath> docTreePathConstructor =
                mockConstruction(DocTreePath.class, (mock, context) -> {
                    treePath[0] = (TreePath) context.arguments().get(0);
                    tree[0] = (DocCommentTree) context.arguments().get(1);
                })
        ) {
            Source.forMethod(context, methodElement).docTreePath();

            assertSame(methodTreePath, treePath[0], "Unexpected treePath");
            assertSame(methodDocTree, tree[0], "Unexpected tree");
        }
    }

    /**
     * Unit test {@link Source#hasDocCommentTree()}
     */
    @Test
    public void test_hasDocCommentTree() {
        assertFalse(Source.forPackage(context, packageElement).hasDocCommentTree(), "For package");
        assertTrue(Source.forMethod(context, methodElement).hasDocCommentTree(), "For method");
    }

    /**
     * Unit test {@link Source#docCommentTree()}
     */
    @Test
    public void test_docCommentTree() {
        assertThrows(IllegalStateException.class,
            () -> Source.forPackage(context, packageElement).docCommentTree(),
            "For package");

        assertSame(methodDocTree, Source.forMethod(context, methodElement).docCommentTree(), "For method");
    }

    /**
     * Unit test {@link Source#resolveElement(String)}
     */
    @Test
    public void test_resolveElement_null() {
        assertNull(Source.forMethod(context, methodElement).resolveElement(null), "null signature");
    }

    /**
     * Unit test {@link Source#resolveElement(String)}
     */
    @Test
    public void test_resolveElement_nonnull() {
        String[] signature = { null };
        Element[] root = { null };
        Element expected = mock();

        try (
            MockedConstruction<Resolver> resolver =
                mockConstruction(Resolver.class, (mock, context) -> {
                    root[0] = (Element) context.arguments().get(1);

                    when(mock.resolve(anyString()))
                        .thenAnswer(i -> {
                            signature[0] = i.getArgument(0);
                            return expected;
                        });
                })
        ) {
            assertSame(expected, Source.forMethod(context, methodElement).resolveElement("#local"), "nonnull");
            assertEquals(methodElement, root[0], "unexpected root element");
            assertEquals("#local", signature[0], "unexpected signature");
        }
    }

    /**
     * Unit test {@link Source#resolveSignature(String)}
     */
    @Test
    public void test_resolveSignature_null() {
        assertNull(Source.forMethod(context, methodElement).resolveSignature(null), "null signature");
    }

    /**
     * Unit test {@link Source#resolveSignature(String)}
     */
    @Test
    public void test_resolveSignature_notFound() {
        try (
            MockedConstruction<Resolver> resolver =
                mockConstruction(Resolver.class, (mock, context) ->
                    when(mock.resolve(anyString()))
                        .thenReturn(null))
        ) {
            assertNull(Source.forMethod(context, methodElement).resolveSignature("#local"), "not found");
        }
    }

    /**
     * Unit test {@link Source#resolveSignature(String)}
     */
    @Test
    public void test_resolveSignature_found() {
        Element local = mock();

        try (
            MockedConstruction<Resolver> resolver =
                mockConstruction(Resolver.class, (mock, context) ->
                    when(mock.resolve(anyString()))
                        .thenReturn(local));
            MockedStatic<Signature> signatureMock = mockStatic(Signature.class)
        ) {
            signatureMock.when(() -> Signature.of(local))
                .thenReturn("my.class.local");

            assertEquals("my.class.local",
                Source.forMethod(context, methodElement).resolveSignature("#local"),
                "found");
        }
    }

    /**
     * Unit test {@link Source#signature()}
     */
    @Test
    public void test_signature() {
        try (
            MockedStatic<Signature> signatureMock = mockStatic(Signature.class)
        ) {
            signatureMock.when(() -> Signature.of(packageElement))
                .thenReturn("my.package");
            signatureMock.when(() -> Signature.of(methodElement))
                .thenReturn("my.package.class");

            assertSame("my.package", Source.forPackage(context, packageElement).signature(), "For package");
            assertSame("my.package.class", Source.forMethod(context, methodElement).signature(), "For method");
        }
    }

    /**
     * Unit test {@link Source#eelType()}
     */
    @Test
    public void test_eelType() {
        assertNull(Source.forPackage(context, packageElement).eelType(), "For package");
        assertEquals(EelType.NUMBER, Source.forMethod(context, methodElement).eelType(), "For method");
    }
}