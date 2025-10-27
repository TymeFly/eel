package com.github.tymefly.eel.doc.scanner;

import com.github.tymefly.eel.doc.model.FunctionGenerator;
import com.github.tymefly.eel.doc.model.TagGenerator;
import com.github.tymefly.eel.doc.model.TagType;
import com.github.tymefly.eel.doc.source.Parameter;
import com.github.tymefly.eel.doc.source.Source;
import com.sun.source.doctree.IdentifierTree;
import com.sun.source.doctree.ParamTree;
import com.sun.source.doctree.ReferenceTree;
import com.sun.source.doctree.ReturnTree;
import com.sun.source.doctree.ThrowsTree;
import com.sun.source.util.DocTreePath;
import org.junit.Test;
import org.mockito.MockedConstruction;

import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit test for {@link FunctionScanner}
 */
public class FunctionScannerTest {

    /**
     * Unit test for {@link FunctionScanner#run(Source, FunctionGenerator)}
     */
    @Test
    public void test_run() {
        Source source = mock();
        FunctionGenerator functionGenerator = mock();
        DocTreePath docPath = mock();

        when(source.docTreePath())
            .thenReturn(docPath);

        try (
            MockedConstruction<FunctionScanner> mocked = mockConstruction(FunctionScanner.class)
        ) {
            FunctionScanner.run(source, functionGenerator);

            FunctionScanner scannerMock = mocked.constructed().get(0);

            verify(scannerMock).scan(docPath, null);
        }
    }

    /**
     * Unit Test {@link FunctionScanner#visitParam(ParamTree, Void)}
     */
    @Test
    public void test_visitParam() {
        Source source = mock(Source.class, RETURNS_DEEP_STUBS);
        ParamTree node = mock();
        IdentifierTree tree = mock();
        FunctionGenerator functionGenerator = mock();
        Parameter parameter = mock();

        FunctionScanner scanner = new FunctionScanner(source, functionGenerator);

        when(node.getName())
            .thenReturn(tree, null, tree);          // returning null is clunky, but makes super.visitParam() happy
        when(tree.toString())
            .thenReturn("myParam");
        when(source.parameters().get("myParam"))
            .thenReturn(parameter);

        scanner.visitParam(node, null);

        verify(functionGenerator).addParameter("myParam", parameter);
    }


    /**
     * Unit Test {@link FunctionScanner#visitReturn(ReturnTree, Void)} 
     */
    @Test
    public void test_visitReturn() {
        Source source = mock();
        FunctionGenerator functionGenerator = mock();
        ReturnTree node = mock();
        DocTreePath docTreePath = mock();

        FunctionScanner scanner = new FunctionScanner(source, functionGenerator);

        when(source.docTreePath())
            .thenReturn(docTreePath);

        scanner.visitReturn(node, null);

        verify(functionGenerator).addTag(TagType.RETURN);
    }


    /**
     * Unit Test {@link FunctionScanner#visitThrows(ThrowsTree, Void)} 
     */
    @Test
    public void test_visitThrows() {
        Source source = mock();
        FunctionGenerator functionGenerator = mock();
        ThrowsTree node = mock();
        ReferenceTree reference = mock();
        TagGenerator tag = mock();

        when(node.getExceptionName())
            .thenReturn(reference, null, reference);    // returning null is clunky, but makes super.visitThrows() happy
        when(reference.toString())
            .thenReturn("myReference");
        when(reference.getSignature())
            .thenReturn("my.Class");
        when(functionGenerator.addTag(TagType.THROWS))
            .thenReturn(tag);

        FunctionScanner scanner = new FunctionScanner(source, functionGenerator);

        scanner.visitThrows(node, null);

        verify(tag).withReference("myReference","my.Class");
    }
}