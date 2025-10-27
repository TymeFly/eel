package com.github.tymefly.eel.doc.report;

import java.util.Collections;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;

import javax.annotation.Nonnull;

import com.github.tymefly.eel.doc.config.Config;
import com.github.tymefly.eel.doc.context.Context;
import j2html.tags.DomContent;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

/**
 * Unit test for {@link AbstractPage}
 */
public class AbstractPageTest {

    private Context context;
    private Config config;

    @Before
    public void setUp() {
        context = Mockito.mock(Context.class);
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

        Assert.assertNotNull("Missing HTML", html);
        Assert.assertTrue("Missing page content", html.contains("page-content"));
        Assert.assertTrue("Missing page title", html.contains("MyTitle - Test"));
        Assert.assertTrue("Missing stylesheet link", html.contains("resource/main.css"));
        Assert.assertTrue("Missing icon link", html.contains("resource/icon.png"));
        Assert.assertTrue("Missing JS script", html.contains("resource/eel-doc.js"));
        Assert.assertTrue("Missing jump back anchor", html.contains("<a name=\"_top\"></a>"));
    }


    /**
     * Unit test for {@link AbstractPage#buildPage}
     */
    @Test
    public void test_buildPage_MenuItems() {
        AbstractPage page = buildPage(Set.of(MenuItem.OVERVIEW));
        String html = page.buildPage();

        Assert.assertNotNull("Missing HTML", html);
        Assert.assertTrue("Failed to render Index", html.contains("<a href=\"_index.html\" class=\"menu-item-text\">Index</a>"));
        Assert.assertTrue("Failed to render Overview", html.contains("<span class=\"menu-item-text\">Overview</span>"));
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

        Assert.assertNotNull("Missing HTML", actual);
        Assert.assertTrue("Missing top bar",
            Pattern.compile("<div\\s+class=\"page-top\"\\s*>\\s*TOP BAR\\s*</div>", Pattern.DOTALL)
                .matcher(actual)
                .find());
        Assert.assertTrue("Missing bottom bar",
            Pattern.compile("<div\\s+class=\"page-bottom\"\\s*>\\s*BOTTOM BAR\\s*</div>", Pattern.DOTALL)
                .matcher(actual)
                .find());
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

        Assert.assertNotNull("Missing HTML", html);
        Assert.assertFalse("Found top bar", html.contains("<div class=\"page-top\">"));
        Assert.assertFalse("Found bottom bar", html.contains("<div class=\"page-bottom\">"));
    }
}
