package com.github.tymefly.eel.doc.report;

import java.io.File;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

import com.github.tymefly.eel.doc.config.Config;
import com.github.tymefly.eel.doc.context.EelDocContext;
import com.github.tymefly.eel.doc.model.GroupModel;
import com.github.tymefly.eel.doc.model.ModelManager;
import com.github.tymefly.eel.doc.model.TagModel;
import com.github.tymefly.eel.doc.utils.FileUtils;
import j2html.tags.DomContent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


/**
 * Unit test for {@link OverviewPage}
 */
public class OverviewPageTest {

    private EelDocContext context;
    private ModelManager modelManager;
    private GroupModel group;
    private File overviewFile;
    private Config config;

    @BeforeEach
    public void setUp() {
        context = mock(EelDocContext.class);
        modelManager = mock(ModelManager.class);
        group = mock(GroupModel.class);
        overviewFile = mock(File.class);
        config = mock(Config.class);

        when(config.docTitle())
            .thenReturn("Overview Title");
        when(context.modelManager())
            .thenReturn(modelManager);
        when(group.name())
            .thenReturn("myGroup");
        when(group.fileName())
            .thenReturn("myGroup.html");
    }


    private OverviewPage buildPage() {
        return new OverviewPage(context, "Overview");
    }


    /**
     * Unit test for {@link OverviewPage#disabledMenuOptions()}
     */
    @Test
    public void test_disabledMenuOptions() {
        OverviewPage page = buildPage();

        EnumSet<MenuItem> actual = page.disabledMenuOptions();

        assertEquals(EnumSet.of(MenuItem.OVERVIEW), actual, "Only OVERVIEW should be disabled");
    }

    /**
     * Unit test for {@link OverviewPage#buildPageContent()}
     */
    @Test
    public void test_buildPageContent_emptyGroups() {
        OverviewPage page = buildPage();

        when(modelManager.groups())
            .thenReturn(Collections.emptyList());

        try (
            MockedStatic<Config> mockedConfig = mockStatic(Config.class)
        ) {
            mockedConfig.when(Config::getInstance)
                .thenReturn(config);
            when(config.overview())
                .thenReturn(null);

            String actual = page.buildPageContent()
                .render();

            assertTrue(actual.contains("id=\"doc-body\""), "Missing doc-body");
            assertFalse(actual.contains("content-link"), "Rendered unexpected groups");

            assertFalse(actual.contains("<h2 class=\"block-name\">Overview Title</h2>"),
                "Unexpected overview Section");
        }
    }

    /**
     * Unit test for {@link OverviewPage#buildPageContent()}
     */
    @Test
    public void test_buildPageContent_withGroup() {
        TagModel tag = mock(TagModel.class);
        TextRender textRender = mock(TextRender.class);
        OverviewPage page = buildPage();

        DomContent contentDiv = j2html.TagCreator.div("GROUP CONTENT");

        when(modelManager.groups())
            .thenReturn(Collections.singletonList(group));

        when(group.summary())
            .thenReturn(Optional.of(tag));
        when(tag.text())
            .thenAnswer(i -> Collections.singletonList(tag));
        when(textRender.text(any()))
            .thenReturn(contentDiv);

        try (
            MockedStatic<Config> mockedConfig = mockStatic(Config.class);
            MockedStatic<TextRender> mockedTextRender = mockStatic(TextRender.class)
        ) {

            mockedTextRender.when(() -> TextRender.build(context, group))
                .thenReturn(textRender);

            mockedConfig.when(Config::getInstance)
                .thenReturn(config);
            when(config.overview())
                .thenReturn(null);

            String actual = page.buildPageContent()
                .render();

            assertTrue(actual.contains("id=\"doc-body\""), "Missing doc-body");
            assertTrue(actual.contains("content-link"), "Missing group link");

            assertTrue(
                Pattern.compile("<td\\s+class=\"content-link\"\\s*>\\s*<a\\s+href=\"myGroup\\.html\"\\s*>\\s*MyGroup\\s*</a>\\s*</td>", Pattern.DOTALL)
                    .matcher(actual)
                    .find(),
                "Missing content-link");
            assertTrue(
                Pattern.compile("<td>\\s*<div>GROUP CONTENT</div>\\s*</td>", Pattern.DOTALL)
                    .matcher(actual)
                    .find(),
                "Missing content name");

            assertFalse(actual.contains("<h2 class=\"block-name\">Overview Title</h2>"),
                "Unexpected overview Section");
        }
    }


    /**
     * Unit test for {@link OverviewPage#buildPageContent()}
     */
    @Test
    public void test_overview() {
        OverviewPage page = new OverviewPage(context, "Overview");

        try (
            MockedStatic<Config> mockedConfig = Mockito.mockStatic(Config.class);
            MockedStatic<FileUtils> mockedFileUtils = Mockito.mockStatic(FileUtils.class)
        ) {
            mockedConfig.when(Config::getInstance)
                .thenReturn(config);
            mockedFileUtils.when(() -> FileUtils.canRead(overviewFile))
                .thenReturn(true);
            mockedFileUtils.when(() -> FileUtils.read(overviewFile))
                .thenReturn(List.of("<p>Line1</p>", "<p>Line2</p>"));
            when(config.overview())
                .thenReturn(Optional.of(overviewFile));

            String actual = page.buildPageContent()
                .render();

            assertTrue(actual.contains("<h2 class=\"block-name\">Overview Title</h2>"), "Missing overview title");
            assertTrue(actual.contains("<p>Line1</p>"), "Missing overview content");
            assertTrue(actual.contains("<p>Line2</p>"), "Missing overview content");
        }
    }

    /**
     * Unit test for {@link OverviewPage#buildPageContent()}
     */
    @Test
    public void test_overview_fileUnreadable() {
        OverviewPage page = buildPage();

        try (
            MockedStatic<Config> mockedConfig = mockStatic(Config.class)
        ) {
            mockedConfig.when(Config::getInstance).thenReturn(config);

            when(overviewFile.canRead())
                .thenReturn(false);
            when(config.overview())
                .thenReturn(Optional.of(overviewFile));

            page.buildPageContent();

            verify(context).error(contains("Can not read overview file"));
        }
    }
}
