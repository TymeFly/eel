package com.github.tymefly.eel.doc.report;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;

import javax.annotation.Nonnull;

import com.github.tymefly.eel.doc.context.EelDocContext;
import com.github.tymefly.eel.doc.model.FunctionModel;
import com.github.tymefly.eel.doc.model.GroupModel;
import com.github.tymefly.eel.doc.model.ModelManager;
import j2html.tags.DomContent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Unit test for {@link IndexPage}
 */
public class IndexPageTest {

    private EelDocContext context;
    private ModelManager modelManager;
    private GroupModel group;

    @BeforeEach
    public void setUp() {
        context = mock(EelDocContext.class);
        modelManager = mock(ModelManager.class);
        group = mock(GroupModel.class);

        when(context.modelManager())
            .thenReturn(modelManager);
    }

    @Nonnull
    private IndexPage buildPage(@Nonnull List<GroupModel> groups) {
        when(modelManager.groups())
            .thenReturn(groups);

        return new IndexPage(context, "Index");
    }


    /**
     * Unit test for {@link IndexPage#buildPageContent()}
     */
    @Test
    public void test_disabledMenuOptions() {
        IndexPage page = buildPage(Collections.emptyList());

        EnumSet<MenuItem> expected = EnumSet.of(MenuItem.INDEX);
        EnumSet<MenuItem> actual = page.disabledMenuOptions();

        assertEquals(expected, actual, "Only INDEX should be disabled");
    }


    /**
     * Unit test for {@link IndexPage#buildPageContent()}
     */
    @Test
    public void test_buildPageContent_emptyGroups() {
        IndexPage page = buildPage(Collections.emptyList());

        DomContent content = page.buildPageContent();

        assertNotNull(content, "buildPageContent should return non-null DomContent");

        String actual = content.render();

        assertTrue(actual.contains("id=\"doc-body\""), "HTML should have doc-body div");
        assertFalse(actual.contains("group-summary"), "HTML should be empty when no groups");
    }

    /**
     * Unit test for {@link IndexPage#buildPageContent()}
     */
    @Test
    public void test_buildPageContent_withFunctions() {
        GroupSummaryRender reader = mock(GroupSummaryRender.class);

        when(group.getFunctions())
            .thenAnswer(i -> Collections.singletonList(mock(FunctionModel.class)));
        when(reader.render())
            .thenReturn(j2html.TagCreator.div("Page content"));

        try (
            MockedStatic<GroupSummaryRender> mockedStatic = Mockito.mockStatic(GroupSummaryRender.class)
        ) {
            mockedStatic.when(() -> GroupSummaryRender.general(Mockito.eq(context), Mockito.eq(group)))
                .thenReturn(reader);

            IndexPage page = buildPage(Collections.singletonList(group));

            DomContent actual = page.buildPageContent();

            String expectedHtml = "<div id=\"doc-body\"><div>Page content</div></div>";
            assertEquals(expectedHtml, actual.render(), "Unexpected HTML");
        }
    }
}
