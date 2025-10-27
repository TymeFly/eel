package com.github.tymefly.eel.doc.report;

import java.util.Collections;
import java.util.EnumSet;
import java.util.List;

import javax.annotation.Nonnull;

import com.github.tymefly.eel.doc.context.Context;
import com.github.tymefly.eel.doc.model.FunctionModel;
import com.github.tymefly.eel.doc.model.GroupModel;
import com.github.tymefly.eel.doc.model.ModelManager;
import j2html.tags.DomContent;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Unit test for {@link IndexPage}
 */
public class IndexPageTest {

    private Context context;
    private ModelManager modelManager;
    private GroupModel group;

    @Before
    public void setUp() {
        context = mock(Context.class);
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

        EnumSet<MenuItem> actual = page.disabledMenuOptions();

        assertEquals("Only INDEX should be disabled", EnumSet.of(MenuItem.INDEX), actual);
    }


    /**
     * Unit test for {@link IndexPage#buildPageContent()}
     */
    @Test
    public void test_buildPageContent_emptyGroups() {
        IndexPage page = buildPage(Collections.emptyList());

        DomContent content = page.buildPageContent();

        assertNotNull("buildPageContent should return non-null DomContent", content);

        String actual = content.render();

        assertTrue("HTML should have doc-body div", actual.contains("id=\"doc-body\""));
        assertFalse("HTML should be empty when no groups", actual.contains("group-summary"));
    }

    /**
     * Unit test for {@link IndexPage#buildPageContent()}
     */
    @Test
    public void test_buildPageContent_withFunctions() {
        GroupSummaryRender reader = mock();

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

            assertEquals("Unexpected HTML", "<div id=\"doc-body\"><div>Page content</div></div>", actual.render());
        }
    }
}
