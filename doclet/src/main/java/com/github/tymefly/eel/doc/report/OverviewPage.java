package com.github.tymefly.eel.doc.report;

import java.io.File;
import java.util.EnumSet;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.github.tymefly.eel.doc.config.Config;
import com.github.tymefly.eel.doc.context.Context;
import com.github.tymefly.eel.doc.model.TagModel;
import com.github.tymefly.eel.doc.utils.FileUtils;
import com.github.tymefly.eel.doc.utils.TextUtils;
import j2html.TagCreator;
import j2html.tags.DomContent;

import static j2html.TagCreator.a;
import static j2html.TagCreator.div;
import static j2html.TagCreator.each;
import static j2html.TagCreator.h2;
import static j2html.TagCreator.iff;
import static j2html.TagCreator.join;
import static j2html.TagCreator.section;
import static j2html.TagCreator.table;
import static j2html.TagCreator.tbody;
import static j2html.TagCreator.td;
import static j2html.TagCreator.th;
import static j2html.TagCreator.tr;

/**
 * Generate the HTML page for the Overview (index.html) page
 */
class OverviewPage extends AbstractPage {
    private final Context context;

    OverviewPage(@Nonnull Context context, @Nonnull String pageName) {
        super(context, pageName);

        this.context = context;
    }


    @Nonnull
    @Override
    protected EnumSet<MenuItem> disabledMenuOptions() {
        return EnumSet.of(MenuItem.OVERVIEW);
    }


    @Nonnull
    @Override
    public DomContent buildPageContent() {
        return
          div(
            iff(Config.getInstance().overview(), this::overview),
            groupTable()
          ).withId("doc-body");
    }


    @Nullable
    private DomContent overview(@Nonnull File file) {
        DomContent content;

        if (!FileUtils.canRead(file)) {
            context.error("Can not read overview file " + file);
            content = null;
        } else {
            content =
              section(
                h2(Config.getInstance().docTitle()).withClass("block-name"),
                section(
                  div(
                    each(FileUtils.read(file),
                      TagCreator::rawHtml
                    )
                  ).withClasses("block-detail")
                ).withClass("block-description")
              ).withClass("summary-description");
        }

        return content;
    }


    @Nonnull
    private DomContent groupTable() {
        return
          section(
            join(
              h2("Function Groups   ").withClass("block-name")
            ),
            section(
              div(
                table(
                  tbody(
                    tr(
                      th("Group").withClasses("col-types", "table-header").withId("summary-col-group"),
                      th("Description").withClass("col-types").withId("summary-col-description")
                    ),
                    each(context.modelManager().groups(), group ->
                      tr(
                        td(
                          a(
                            TextUtils.capitalise(group.name())
                          ).withHref(group.fileName())
                        ).withClass("content-link"),
                        td(
                          iff(group.summary(), (TagModel tag) ->
                            TextRender.build(context, group).text(tag.text())
                          )
                        )
                      ).withClass("col-types")
                    )
                  ).withClass("content-body")
                ).withClass("summary-types")
              ).withClasses("block-detail", "content-container")
            ).withClass("block-description")
          ).withClass("summary-description");
    }
}
