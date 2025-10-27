package com.github.tymefly.eel.doc.report;

import java.util.Arrays;
import java.util.Set;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.github.tymefly.eel.annotation.VisibleForTesting;
import com.github.tymefly.eel.doc.config.Config;
import com.github.tymefly.eel.doc.context.Context;
import com.github.tymefly.eel.doc.utils.TextUtils;
import j2html.tags.DomContent;

import static j2html.TagCreator.a;
import static j2html.TagCreator.body;
import static j2html.TagCreator.div;
import static j2html.TagCreator.document;
import static j2html.TagCreator.each;
import static j2html.TagCreator.head;
import static j2html.TagCreator.header;
import static j2html.TagCreator.html;
import static j2html.TagCreator.iff;
import static j2html.TagCreator.iffElse;
import static j2html.TagCreator.join;
import static j2html.TagCreator.link;
import static j2html.TagCreator.meta;
import static j2html.TagCreator.rawHtml;
import static j2html.TagCreator.script;
import static j2html.TagCreator.span;
import static j2html.TagCreator.text;
import static j2html.TagCreator.title;

/**
 * Implemented by classes that produce HTML
 */
abstract class AbstractPage {
    private final Config config;
    private final Context context;
    private final String pageName;


    AbstractPage(@Nonnull Context context, @Nonnull String pageName) {
        this(Config.getInstance(), context, pageName);
    }

    @VisibleForTesting
    AbstractPage(@Nonnull Config config, @Nonnull Context context, @Nonnull String pageName) {
        this.config = config;
        this.context = context;
        this.pageName = pageName;
    }


    @Nonnull
    protected abstract Set<MenuItem> disabledMenuOptions();


    /**
     * Generates the content-specific part of the page body
     * @return the content-specific part of the page body
     */
    @Nonnull
    protected abstract DomContent buildPageContent();


    @Nullable
    String buildPage() {
        DomContent content = buildPageContent();
        String page;

        context.note("Building page %s", pageName);

        page = document().render() +
            html(
              head(
                title(config.title() + " - " + pageName),
                link().withRel("stylesheet").withHref("resource/main.css"),
                link().withRel("icon").withHref("resource/icon.png"),
                meta().attr("charset", config.charSet()),
                script().attr("src", "resource/eel-doc.js").attr("type", "text/javascript")
              ),
              body(
                join(
                  a().attr("name", "_top")
                ),
                top(),
                pageHeader(TextUtils.capitalise(pageName), disabledMenuOptions()),
                content,
                scrollUp(),
                bottom()
              )
            ).renderFormatted();

        return page;
    }


    @Nullable
    private DomContent top() {
        return iff(config.top(), (String top) ->
            div(
              rawHtml(top)
            ).withClass("page-top")
        );
    }


    @Nullable
    private DomContent bottom() {
        return iff(config.bottom(), (String bottom) ->
            div(
              rawHtml(bottom)
            ).withClass("page-bottom")
        );
    }


    @Nonnull
    private DomContent scrollUp() {
        return
          join(
            a(
              text("")
            ).withHref("#_top")
             .withId("scroll-up")
          );
    }


    @Nonnull
    private DomContent pageHeader(@Nonnull String title,
                                  @Nonnull Set<MenuItem> disabledMenuItems) {
        return
            header(
              span(title).withClass("header-title"),
              div(
                each(Arrays.asList(MenuItem.values()), item ->
                  span(
                    join(
                      iffElse((disabledMenuItems.contains(item)),
                        span(item.toString())
                          .withClass("menu-item-text"),
                        a(item.toString())
                          .withHref(item.getHref())
                          .withClass("menu-item-text")
                      )
                    )
                  ).withClass("menu-item")
                )
              ).withId("menu")
           ).withId("header").withClass("title");
    }
}
