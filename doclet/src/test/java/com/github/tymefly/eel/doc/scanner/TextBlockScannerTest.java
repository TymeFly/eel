package com.github.tymefly.eel.doc.scanner;

import java.util.Arrays;
import java.util.List;

import com.github.tymefly.eel.doc.context.Context;
import com.github.tymefly.eel.doc.model.ModelManager;
import com.github.tymefly.eel.doc.model.TextBlockGenerator;
import com.github.tymefly.eel.doc.source.Source;
import com.sun.source.doctree.DocTree;
import com.sun.source.util.DocTreePath;
import org.junit.Test;
import org.mockito.MockedConstruction;

import static org.junit.Assert.assertSame;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit test for {@link TextBlockScanner}
 */
public class TextBlockScannerTest {

    /**
     * Unit Test {@link TextBlockScanner#run(Source, List)}
     */
    @Test
    public void testRun_returnsTextBlockGenerator() {
        Source source = mock();
        Context context = mock();
        ModelManager modelManager = mock();
        TextBlockGenerator textBlock = mock();
        DocTree firstNode = mock();
        DocTree secondNode = mock();
        DocTreePath docPath = mock();

        List<DocTree> docNodes = Arrays.asList(firstNode, secondNode);

        when(source.context())
            .thenReturn(context);
        when(context.modelManager())
            .thenReturn(modelManager);
        when(modelManager.textBlock())
            .thenReturn(textBlock);
        when(source.docTreePath())
            .thenReturn(docPath);

        try (
            MockedConstruction<TextBlockScanner> mockConstruction = mockConstruction(TextBlockScanner.class,
                (mockScanner, context1) -> when(mockScanner.scanText(docNodes))
                    .thenReturn(textBlock))
        ) {
            TextBlockGenerator result = TextBlockScanner.run(source, docNodes);

            assertSame(textBlock, result);
            verify(source).context();
            verify(context).modelManager();
            verify(modelManager).textBlock();
        }
    }

    /**
     * Unit Test {@link TextBlockScanner#run(Source, List)}
     */
    @Test
    public void testScanText_invokesScanForEachNode() {
        Source source = mock();
        TextBlockGenerator textBlock = mock();
        DocTree firstNode = mock();
        DocTree secondNode = mock();
        DocTreePath docPath = mock();

        List<DocTree> docNodes = Arrays.asList(firstNode, secondNode);

        TextBlockScanner scanner = spy(new TextBlockScanner(source, textBlock));

        when(source.docTreePath())
            .thenReturn(docPath);

        TextBlockGenerator result = scanner.scanText(docNodes);

        assertSame(textBlock, result);
        verify(scanner, times(2)).scan(any(DocTreePath.class), isNull());
    }
}
