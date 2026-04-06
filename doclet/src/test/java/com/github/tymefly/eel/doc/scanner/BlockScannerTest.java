package com.github.tymefly.eel.doc.scanner;

import java.util.Collections;
import java.util.List;

import com.github.tymefly.eel.doc.context.EelDocContext;
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
import com.sun.source.util.DocTreePath;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * Unit test for {@link BlockScanner}
 */
public class BlockScannerTest {
    // Concrete class for testing
    private static class MockBlockScanner extends BlockScanner {
        MockBlockScanner(Source source, ElementGenerator<?> model) {
            super(source, model);
        }

        @Override
        public Void scan(Iterable<? extends DocTree> nodes, Void unused) {
            return null;
        }

        @Override
        public Void scan(DocTreePath path, Void arg) {
            return null;
        }
    }

    
    /**
     * Unit test for {@link BlockScanner#visitSummary(SummaryTree, Void)}
     */
    @Test
    public void test_visitSummary() {
        Source source = mock();
        SummaryTree summary = mock();
        ElementGenerator<?> model = mock();
        TextBlockGenerator textBlock = mock();

        List<DocTree> docNodes = Collections.singletonList(mock(DocTree.class));

        when(summary.getSummary())
            .thenAnswer(i -> docNodes);

        try (
            MockedStatic<TextBlockScanner> staticMock = mockStatic(TextBlockScanner.class)
        ) {
            staticMock.when(() -> TextBlockScanner.run(source, docNodes))
                .thenReturn(textBlock);

            BlockScanner scanner = spy(new MockBlockScanner(source, model));

            scanner.visitSummary(summary, null);

            verify(model).addSummary(textBlock);
            staticMock.verify(() -> TextBlockScanner.run(source, docNodes));
        }
    }

    
    /**
     * Unit test for {@link BlockScanner#visitSee(SeeTree, Void)} 
     */
    @Test
    public void test_visitSee_withReference() {
        Source source = mock();
        ElementGenerator<?> model = mock();
        SeeTree see = mock();
        ReferenceTree reference = mock();
        TagGenerator tag = mock();
        List<DocTree> refs = Collections.singletonList(reference);

        BlockScanner scanner = spy(new MockBlockScanner(source, model));

        when(model.addTag(TagType.SEE))
            .thenReturn(tag);
        when(see.getReference())
            .thenAnswer(i -> refs);
        when(reference.getSignature())
            .thenReturn("sig");
        when(source.resolveSignature("sig"))
            .thenReturn("resolved");

        scanner.visitSee(see, null);

        verify(model).addTag(TagType.SEE);
        verify(tag).withReference("sig", "resolved");
    }
    
    /**
     * Unit test for {@link BlockScanner#visitSee(SeeTree, Void)} 
     */
    @Test
    public void test_visitSee_withoutReference() {
        Source source = mock();
        ElementGenerator<?> model = mock();
        SeeTree see = mock();
        TagGenerator tag = mock();
        List<DocTree> refs = Collections.emptyList();

        BlockScanner scanner = spy(new MockBlockScanner(source, model));

        when(model.addTag(TagType.SEE))
            .thenReturn(tag);
        when(see.getReference())
            .thenAnswer(i -> refs);

        scanner.visitSee(see, null);

        verify(model).addTag(TagType.SEE);
        verifyNoInteractions(source);
    }

    
    /**
     * Unit test for {@link BlockScanner#visitAuthor(AuthorTree, Void)} 
     */
    @Test
    public void test_visitAuthor() {
        Source source = mock();
        ElementGenerator<?> model = mock();
        AuthorTree author = mock();
        TagGenerator tag = mock();

        BlockScanner scanner = spy(new MockBlockScanner(source, model));

        when(model.addTag(TagType.AUTHOR))
            .thenReturn(tag);

        scanner.visitAuthor(author, null);

        verify(model).addTag(TagType.AUTHOR);
    }

    
    /**
     * Unit test for {@link BlockScanner#visitSince(SinceTree, Void)} 
     */
    @Test
    public void test_visitSince() {
        Source source = mock();
        ElementGenerator<?> model = mock();
        SinceTree since = mock();
        TagGenerator tag = mock();

        BlockScanner scanner = spy(new MockBlockScanner(source, model));

        when(model.addTag(TagType.SINCE))
            .thenReturn(tag);

        scanner.visitSince(since, null);

        verify(model).addTag(TagType.SINCE);
    }

    
    /**
     * Unit test for {@link BlockScanner#visitVersion(VersionTree, Void)} 
     */
    @Test
    public void test_visitVersion() {
        Source source = mock();
        ElementGenerator<?> model = mock();
        VersionTree version = mock();
        TagGenerator tag = mock();

        BlockScanner scanner = spy(new MockBlockScanner(source, model));

        when(model.addTag(TagType.VERSION))
            .thenReturn(tag);

        scanner.visitVersion(version, null);

        verify(model).addTag(TagType.VERSION);
    }

    
    /**
     * Unit test for {@link BlockScanner#visitDeprecated(DeprecatedTree, Void)} 
     */
    @Test
    public void test_visitDeprecated() {
        Source source = mock();
        ElementGenerator<?> model = mock();
        DeprecatedTree deprecated = mock();
        TagGenerator tag = mock();

        BlockScanner scanner = spy(new MockBlockScanner(source, model));

        when(model.addTag(TagType.DEPRECATED))
            .thenReturn(tag);

        scanner.visitDeprecated(deprecated, null);

        verify(model).addTag(TagType.DEPRECATED);
    }

    
    /**
     * Unit test for {@link BlockScanner#visitHidden(HiddenTree, Void)} 
     */
    @Test
    public void test_visitHidden() {
        Source source = mock();
        ElementGenerator<?> model = mock();
        HiddenTree hidden = mock();

        BlockScanner scanner = spy(new MockBlockScanner(source, model));

        scanner.visitHidden(hidden, null);

        verify(model).hide();
    }

    
    /**
     * Unit test for {@link BlockScanner#visitUnknownBlockTag(UnknownBlockTagTree, Void)} 
     */
    @Test
    public void test_visitUnknownBlockTag() {
        Source source = mock();
        ElementGenerator<?> model = mock();
        UnknownBlockTagTree unknownTag = mock();
        TagGenerator tag = mock();
        EelDocContext context = mock();

        BlockScanner scanner = spy(new MockBlockScanner(source, model));

        when(model.addIgnoredBlock())
            .thenAnswer(i -> tag);
        when(source.context())
            .thenReturn(context);
        when(unknownTag.getTagName())
            .thenReturn("custom");

        scanner.visitUnknownBlockTag(unknownTag, null);

        verify(model).addIgnoredBlock();
        verify(context).warn("Unexpected tag %s", "custom");
    }
}
