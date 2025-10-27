package com.github.tymefly.eel.doc.source;

import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.PackageElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;

import com.github.tymefly.eel.doc.context.Context;
import com.github.tymefly.eel.doc.utils.EelType;
import com.sun.source.doctree.DocCommentTree;
import com.sun.source.util.DocTreePath;
import com.sun.source.util.TreePath;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;

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
    private Context context;

    private PackageElement packageElement;
    private ExecutableElement methodElement;

    private DocCommentTree methodDocTree;
    private TreePath methodTreePath;


    @Before
    public void setUp() {
        TypeMirror returnType = mock();

        context = mock(Context.class, RETURNS_DEEP_STUBS);
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
        Assert.assertSame("For package", context, Source.forPackage(context, packageElement).context());
        Assert.assertSame("For method", context, Source.forMethod(context, methodElement).context());
    }

    /**
     * Unit test {@link Source#parameters()}
     */
    @Test
    public void test_parameters() {
        Assert.assertSame("For package", ParameterList.EMPTY, Source.forPackage(context, packageElement).parameters());

        ParameterList parameters = mock();

        try (
            MockedStatic<ParameterList> parameterList = mockStatic(ParameterList.class)
        ) {
            parameterList.when(() -> ParameterList.build(methodElement))
                .thenReturn(parameters);


            Assert.assertSame("For method", parameters, Source.forMethod(context, methodElement).parameters());
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

            Assert.assertSame("Unexpected treePath", methodTreePath, treePath[0]);
            Assert.assertSame("Unexpected tree", methodDocTree, tree[0]);
        }
    }

    /**
     * Unit test {@link Source#hasDocCommentTree()}
     */
    @Test
    public void test_hasDocCommentTree() {
        Assert.assertFalse("For package", Source.forPackage(context, packageElement).hasDocCommentTree());
        Assert.assertTrue("For method", Source.forMethod(context, methodElement).hasDocCommentTree());
    }

    /**
     * Unit test {@link Source#docCommentTree()}
     */
    @Test
    public void test_docCommentTree() {
        Assert.assertThrows("For package",
            IllegalStateException.class,
            () -> Source.forPackage(context, packageElement).docCommentTree());

        Assert.assertSame("For method", methodDocTree, Source.forMethod(context, methodElement).docCommentTree());
    }

    /**
     * Unit test {@link Source#resolveElement(String)}
     */
    @Test
    public void test_resolveElement_null() {
        Assert.assertNull("null signature", Source.forMethod(context, methodElement).resolveElement(null));
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
            Assert.assertSame("nonnull", expected, Source.forMethod(context, methodElement).resolveElement("#local"));
            Assert.assertEquals("unexpected root element", methodElement, root[0]);
            Assert.assertEquals("unexpected signature", "#local", signature[0]);
        }
    }

    /**
     * Unit test {@link Source#resolveSignature(String)}
     */
    @Test
    public void test_resolveSignature_null() {
        Assert.assertNull("null signature", Source.forMethod(context, methodElement).resolveSignature(null));
    }

    /**
     * Unit test {@link Source#resolveSignature(String)}
     */
    @Test
    public void test_resolveSignature_notFound() {
        try (
            MockedConstruction<Resolver> resolver =
                mockConstruction(Resolver.class, (mock, context) -> {
                    when(mock.resolve(anyString()))
                        .thenReturn(null);
                })
        ) {
            Assert.assertNull("not found", Source.forMethod(context, methodElement).resolveSignature("#local"));
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
                mockConstruction(Resolver.class, (mock, context) -> {
                    when(mock.resolve(anyString()))
                        .thenReturn(local);
                });
            MockedStatic<Signature> signatureMock = mockStatic(Signature.class)
        ) {
            signatureMock.when(() -> Signature.of(local))
                .thenReturn("my.class.local");

            Assert.assertEquals("found",
                "my.class.local",
                Source.forMethod(context, methodElement).resolveSignature("#local"));
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

            Assert.assertSame("For package", "my.package", Source.forPackage(context, packageElement).signature());
            Assert.assertSame("For method", "my.package.class", Source.forMethod(context, methodElement).signature());
        }
    }

    /**
     * Unit test {@link Source#eelType()}
     */
    @Test
    public void test_eelType() {
        Assert.assertNull("For package", Source.forPackage(context, packageElement).eelType());
        Assert.assertEquals("For method", EelType.NUMBER, Source.forMethod(context, methodElement).eelType());
    }
}