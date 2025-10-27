package com.github.tymefly.eel.doc.report;

import java.util.Collection;
import java.util.Set;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.github.tymefly.eel.doc.context.Context;
import com.github.tymefly.eel.doc.model.FunctionModel;
import com.github.tymefly.eel.doc.model.GroupModel;
import com.github.tymefly.eel.doc.model.TagModel;
import com.github.tymefly.eel.doc.model.TextModel;
import com.github.tymefly.eel.doc.model.TextStyle;
import com.github.tymefly.eel.doc.utils.TextUtils;
import j2html.TagCreator;
import j2html.tags.DomContent;

import static j2html.TagCreator.a;
import static j2html.TagCreator.code;
import static j2html.TagCreator.each;
import static j2html.TagCreator.join;
import static j2html.TagCreator.rawHtml;
import static j2html.TagCreator.span;

/**
 * Generate the HTML model used to render a block of styled text. This may include a link
 */
class TextRender {
    private static final Set<TextStyle> CAPITALISE = Set.of(
        TextStyle.NONE,
        TextStyle.LINK,
        TextStyle.PLAIN_LINK,
        TextStyle.ERROR);
    private final Context context;
    private final GroupModel group;

    private TextRender(@Nonnull Context context, @Nonnull GroupModel group) {
        this.context = context;
        this.group = group;
    }

    @Nonnull
    static TextRender build(@Nonnull Context context, @Nonnull GroupModel group) {
        return new TextRender(context, group);
    }


    @Nonnull
    DomContent text(@Nonnull Collection<TextModel> text) {
        boolean[] capitalise = { true };

        return
          join(
            each(text, t -> {
                DomContent result = text(t, capitalise[0]);
                capitalise[0] = false;

                return result;
            })
          );
    }


    @Nullable
    DomContent link(@Nonnull TagModel tag, @Nonnull String text) {
        return link(text, tag.target(), tag.tagType().showAllReference());
    }


    @Nullable
    private DomContent text(@Nonnull TextModel text, boolean capitalise) {
        TextStyle style = text.style();
        String raw = text.text();
        DomContent content;

        if (capitalise && CAPITALISE.contains(style)) {
            raw = TextUtils.capitalise(raw);
        }

        content = switch (style) {
            case NONE -> TagCreator.text(raw);
            case RAW -> rawHtml(raw);
            case LITERAL -> span(raw).withClass("literal");
            case CODE -> code(raw);
            case PLAIN_LINK -> span(
                                 link(raw, text.target(), true)
                               ).withClass("plain-link");
            case LINK -> link(raw, text.target(), true);
            case ERROR -> span(raw).withClass("invalid-highlight");
        };

        return content;
    }


    @Nullable
    private DomContent link(@Nonnull String text, @Nullable String target, boolean renderBrokenLink) {
        FunctionModel function = (target == null ? null : context.modelManager().bySignature(target));
        String id = (function == null ? null : function.uniqueId());
        DomContent content;

        if (id != null) {
            content = validLink(function);
        } else if (renderBrokenLink) {
            content = brokenLink(text, target);
        } else {
            content = null;
        }

        return content;
    }

    @Nonnull
    private DomContent validLink(@Nonnull FunctionModel function) {
        String name = function.eelSignature();
        String id = function.uniqueId();
        GroupModel targetGroup = function.group();
        String fileName = (targetGroup == this.group ? "" : targetGroup.fileName());

        return a(name).withHref(fileName + "#" + id);
    }

    @Nonnull
    private DomContent brokenLink(@Nonnull String text, @Nullable String target) {
        String link = (target == null ? text : target);

        // Clean the text by:
        //      - Replacing fully qualified names with short names
        //      - Removing (possible) leading '#' sign, and changing inner '#' to a dot
        String clean = link.replaceAll("^(#)|\\b(?:[A-Za-z0-9_]+\\.)+([A-Za-z0-9_]+)", "$2")
            .replace('#', '.');

        return code(clean);
    }
}
