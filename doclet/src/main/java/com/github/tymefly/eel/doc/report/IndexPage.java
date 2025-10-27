package com.github.tymefly.eel.doc.report;

import java.util.EnumSet;

import javax.annotation.Nonnull;

import com.github.tymefly.eel.doc.context.Context;
import j2html.tags.DomContent;

import static j2html.TagCreator.div;
import static j2html.TagCreator.each;
import static j2html.TagCreator.iff;

/**
 * Generate the page for the function index
 */
class IndexPage extends AbstractPage {
    private final Context context;

    IndexPage(@Nonnull Context context, @Nonnull String pageName) {
        super(context, pageName);

        this.context = context;
    }


    @Nonnull
    @Override
    protected EnumSet<MenuItem> disabledMenuOptions() {
        return EnumSet.of(MenuItem.INDEX);
    }


    @Nonnull
    @Override
    public DomContent buildPageContent() {
        return
          div(
            each(context.modelManager().groups(), group ->
              iff(!group.getFunctions().isEmpty(),
                GroupSummaryRender.general(context, group).render()
              )
            )
          ).withId("doc-body");
    }
}
