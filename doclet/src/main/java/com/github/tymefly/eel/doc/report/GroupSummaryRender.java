package com.github.tymefly.eel.doc.report;

import javax.annotation.Nonnull;

import com.github.tymefly.eel.doc.context.EelDocContext;
import com.github.tymefly.eel.doc.model.FunctionModel;
import com.github.tymefly.eel.doc.model.GroupModel;
import com.github.tymefly.eel.doc.model.TagModel;
import com.github.tymefly.eel.doc.utils.EelType;
import com.github.tymefly.eel.doc.utils.TextUtils;
import j2html.tags.DomContent;

import static j2html.TagCreator.a;
import static j2html.TagCreator.b;
import static j2html.TagCreator.code;
import static j2html.TagCreator.div;
import static j2html.TagCreator.each;
import static j2html.TagCreator.h2;
import static j2html.TagCreator.iff;
import static j2html.TagCreator.iffElse;
import static j2html.TagCreator.join;
import static j2html.TagCreator.section;
import static j2html.TagCreator.table;
import static j2html.TagCreator.tbody;
import static j2html.TagCreator.td;
import static j2html.TagCreator.text;
import static j2html.TagCreator.th;
import static j2html.TagCreator.tr;

/**
 * Generate the HTML model used to render a table describing all the functions in a single group.
 */
class GroupSummaryRender {
    private final EelDocContext context;
    private final GroupModel group;
    private final String pageLink;
    private final boolean isLocal;

    private GroupSummaryRender(@Nonnull EelDocContext context,
                               @Nonnull GroupModel group,
                               @Nonnull String pageLink,
                               boolean isLocal) {
        this.context = context;
        this.group = group;
        this.pageLink = pageLink;
        this.isLocal = isLocal;
    }


    /**
     * Factory method for group summaries where the details are displayed in the current page
     * @param context   Document context
     * @param group     the local group
     * @return          an object to render the {@code group}
     */
    @Nonnull
    static GroupSummaryRender local(@Nonnull EelDocContext context, @Nonnull GroupModel group) {
        return new GroupSummaryRender(context, group, "", true);
    }

    /**
     * Factory method for group summaries that are rendered on another page
     * @param context   Document context
     * @param group     the local group
     * @return          an object to render the {@code group}
     */
    @Nonnull
    static GroupSummaryRender general(@Nonnull EelDocContext context, @Nonnull GroupModel group) {
        String file = group.fileName();

        return new GroupSummaryRender(context, group, file, false);
    }


    @Nonnull
    DomContent render() {
        return
          section(
            join(
              iffElse(isLocal,
                h2(TextUtils.capitalise(group.name()) + " group").withClass("block-name"),
                a(
                  h2(TextUtils.capitalise(group.name()) + " group").withClass("block-name")
                ).withHref(group.fileName())
              )
            ),
            section(
              div(
                table(
                  tbody(
                    header(),
                    iff(isLocal && group.hasDescription(),
                      groupDescription()
                    ),
                    each(group.getFunctions(), this::functionDescription)
                  ).withClass("content-body")
                ).withClass("summary-types")
              ).withClasses("block-detail", "content-container")
            ).withClass("block-description")
          ).withClass("summary-description");
    }

    @Nonnull
    private DomContent header() {
        return
          tr(
            join(
              th("Name").withClass("col-types").withId("summary-col-block")
            ),
            join(
              th("Type").withClass("col-types").withId("summary-col-returns")
            ),
            join(
              th("Description").withClass("col-types").withId("summary-col-description")
            )
          ).withClass("table-header");
    }

    @Nonnull
    private DomContent groupDescription() {
        return
          tr(
            td(
              code(
                b(
                  a(
                    text(group.name())
                  ).withHref(pageLink + "#_Group_").withClass("section-link")
                )
              )
            ),
            td(
              code(
                b("Group")
              )
            ),
            td(
              iff(group.summary(), (TagModel tag) ->
                TextRender.build(context, group).text(tag.text())
              )
            )
          ).withClass("col-types");
    }

    @Nonnull
    private DomContent functionDescription(@Nonnull FunctionModel function) {
        return
          tr(
            td(
              join(
                code(
                  a(
                    function.eelSignature()
                  ).withHref(pageLink + "#" + function.uniqueId())
                ).withClass("section-link")
              )
            ),
            join(
              td(
                iff(function.type(), (EelType type) ->
                  text(type.toString())
                )
              )
            ),
            td(
              iff(function.summary(), (TagModel tag) ->
                TextRender.build(context, group).text(tag.text())
              )
            )
          ).withClass("col-types");
    }
}
