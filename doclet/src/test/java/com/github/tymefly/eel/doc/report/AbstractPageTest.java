package com.github.tymefly.eel.doc.report;

import java.util.Collections;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;

import javax.annotation.Nonnull;

import com.github.tymefly.eel.doc.config.Config;
import com.github.tymefly.eel.doc.context.EelDocContext;
import j2html.tags.DomContent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit test for {@link AbstractPage}
 */
public class AbstractPageTest {

    private EelDocContext context;
    private Config config;

    @BeforeEach
    public void setUp() {
        context = Mockito.mock(EelDocContext.class);
        config = Mockito.mock(Config.class);

        Mockito.when(config.title())
            .thenReturn("MyTitle");
        Mockito.when(config.charSet())
            .thenReturn("UTF-8");
        Mockito.when(config.top())
            .thenReturn(null);
        Mockito.when(config.bottom())
            .thenReturn(null);
    }

    @Nonnull
    private AbstractPage buildPage(@Nonnull Set<MenuItem> disabledMenuOptions) {
        return new AbstractPage(config, context, "Test") {
            @Nonnull
            @Override
            protected Set<MenuItem> disabledMenuOptions() {
                return disabledMenuOptions;
            }

            @Nonnull
            @Override
            protected DomContent buildPageContent() {
                return j2html.TagCreator.div("page-content");
            }
        };
    }


    /**
     * Unit test for {@link AbstractPage#buildPage}
     */
    @Test
    public void test_buildPage_outline() {
        AbstractPage page = buildPage(Collections.emptySet());

        String html = page.buildPage();

        assertNotNull(html, "Missing HTML");
        assertTrue(html.contains("page-content"), "Missing page content");
        assertTrue(html.contains("MyTitle - Test"), "Missing page title");
        assertTrue(html.contains("resource/main.css"), "Missing stylesheet link");
        assertTrue(html.contains("resource/icon.png"), "Missing icon link");
        assertTrue(html.contains("resource/eel-doc.js"), "Missing JS script");
        assertTrue(html.contains("<a name=\"_top\"></a>"), "Missing jump back anchor");
    }


    /**
     * Unit test for {@link AbstractPage#buildPage}
     */
    @Test
    public void test_buildPage_MenuItems() {
        AbstractPage page = buildPage(Set.of(MenuItem.OVERVIEW));
        String html = page.buildPage();

        assertNotNull(html, "Missing HTML");
        assertTrue(html.contains("<a href=\"_index.html\" class=\"menu-item-text\">Index</a>"), "Failed to render Index");
        assertTrue(html.contains("<span class=\"menu-item-text\">Overview</span>"), "Failed to render Overview");
    }


    /**
     * Unit test for {@link AbstractPage#buildPage}
     */
    @Test
    public void test_buildPage_withTopAndBottom() {
        Mockito.when(config.top())
            .thenReturn(Optional.of("TOP BAR"));
        Mockito.when(config.bottom())
            .thenReturn(Optional.of("BOTTOM BAR"));

        AbstractPage page = buildPage(Collections.emptySet());
        String actual = page.buildPage();

        assertNotNull(actual, "Missing HTML");
        assertTrue(
            Pattern.compile("<div\\s+class=\"page-top\"\\s*>\\s*TOP BAR\\s*</div>", Pattern.DOTALL)
                .matcher(actual)
                .find(),
            "Missing top bar");
        assertTrue(
            Pattern.compile("<div\\s+class=\"page-bottom\"\\s*>\\s*BOTTOM BAR\\s*</div>", Pattern.DOTALL)
                .matcher(actual)
                .find(),
            "Missing bottom bar");
    }

    /**
     * Unit test for {@link AbstractPage#buildPage}
     */
    @Test
    public void test_buildPage_withoutTopAndBottom() {
        Mockito.when(config.top())
            .thenReturn(Optional.empty());
        Mockito.when(config.bottom())
            .thenReturn(Optional.empty());

        AbstractPage page = buildPage(Collections.emptySet());
        String html = page.buildPage();

        assertNotNull(html, "Missing HTML");
        assertFalse(html.contains("<div class=\"page-top\">"), "Found top bar");
        assertFalse(html.contains("<div class=\"page-bottom\">"), "Found bottom bar");
    }
}
