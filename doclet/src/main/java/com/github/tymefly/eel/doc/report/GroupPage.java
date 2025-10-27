package com.github.tymefly.eel.doc.report;

import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.github.tymefly.eel.doc.context.Context;
import com.github.tymefly.eel.doc.model.ElementModel;
import com.github.tymefly.eel.doc.model.FunctionModel;
import com.github.tymefly.eel.doc.model.GroupModel;
import com.github.tymefly.eel.doc.model.ParamModel;
import com.github.tymefly.eel.doc.model.TagModel;
import com.github.tymefly.eel.doc.model.TagType;
import com.github.tymefly.eel.doc.utils.EelType;
import j2html.tags.ContainerTag;
import j2html.tags.DomContent;

import static j2html.TagCreator.br;
import static j2html.TagCreator.code;
import static j2html.TagCreator.div;
import static j2html.TagCreator.each;
import static j2html.TagCreator.h2;
import static j2html.TagCreator.h3;
import static j2html.TagCreator.hr;
import static j2html.TagCreator.iff;
import static j2html.TagCreator.join;
import static j2html.TagCreator.section;
import static j2html.TagCreator.span;
import static j2html.TagCreator.text;

/**
 * Generate a page that describes a group of functions
 */
class GroupPage extends AbstractPage {
    private final Context context;
    private final GroupModel group;

    GroupPage(@Nonnull Context context, @Nonnull String name, @Nonnull GroupModel group) {
        super(context, name);

        this.context = context;
        this.group = group;
    }



    @Nonnull
    @Override
    protected EnumSet<MenuItem> disabledMenuOptions() {
        return EnumSet.noneOf(MenuItem.class);
    }


    @Nonnull
    @Override
    public DomContent buildPageContent() {
        return
            div(
              GroupSummaryRender.local(context, group).render(),
              hr(),
              iff(group.hasDescription(),
                span(
                  groupDetail(group),
                  hr()
                )
              ),
              each(group.getFunctions(), this::functionDetail)
            ).withId("doc-body");
    }




    /** Description of the group */
    @Nonnull
    private DomContent groupDetail(@Nonnull GroupModel model) {
        return
          section(
            join(
              h2(model.name()).withClass("block-name").withId("_Group_")
            ),
            section(
              div(
                deprecated(model),
                description(model),
                each(TagType.sections(), type ->
                  textTags(model, type)
                )
              ).withClass("block-detail")
            ).withClass("overview-description")
          ).withClass("group-description");
    }


    @Nonnull
    private DomContent functionDetail(@Nonnull FunctionModel model) {
        return
          section(
            join(
              h2(model.eelSignature()).withClass("block-name").withId(model.uniqueId())
            ),
            section(
              div(
                deprecated(model),
                description(model),
                calledAs(model),
                parameters(model),
                returnType(model),
                each(TagType.sections(), type ->
                  textTags(model, type)
                )
              ).withClass("block-detail")
            ).withClass("block-description"),
            hr()
          ).withClass("function-description");
    }

    @Nullable
    private DomContent deprecated(@Nonnull ElementModel model) {
        Optional<TagModel> tag = model.deprecated();

        return
            iff(tag, t ->
              div(
                join(
                  h3("Deprecated")
                ),
                iff(!t.text().isEmpty(),
                  span(
                    TextRender.build(context, group).text(t.text()),
                    br()
                  ).withClass("deprecated-text")
                )
              ).withClasses("deprecated", "block-detail-section")
            );
    }

    @Nullable
    private DomContent description(@Nonnull ElementModel model) {
        return
          iff((!model.text().isEmpty()),
            div(
              TextRender.build(context, group).text(model.text())
            ).withClass("description")
          );
    }

    @Nullable
    private DomContent calledAs(@Nonnull FunctionModel function) {
        List<String> calls = signatureLists(function);

        return
          div(
            join(
              h3("Called as:")
            ),
            each(calls, call ->
              join(
                div(
                  code(
                    call
                  )
                )
              )
            )
          ).withClass("block-detail-section");
    }


    @Nonnull
    private DomContent parameters(@Nonnull FunctionModel function) {
        Collection<ParamModel> parameters = function.parameters();

        return
          section(
            iff((!parameters.isEmpty()),
              div(
                join(
                  h3("Parameters:")
                ),
                each(parameters, parameter ->
                  div(
                    div(
                      join(
                        code(
                          text(parameter.identifier()),
                          text(parameter.isVarArgs() ? "..." : "")
                        )
                      ),
                      div(
                        iff(parameter.type(), (EelType type) ->
                          join(
                            div(
                              span("Type : ").withClass("description-title"),
                              code(type.toString()).withClasses("parameterType", "description")
                            )
                          )
                        ),
                        iff(parameter.defaultDescription(), (String description) ->
                          join(
                            div(
                              span("Default Value : ").withClass("description-title"),
                              span(description).withClass("description")
                            )
                          )
                        ),
                        iff((!parameter.text().isEmpty()),
                          join(
                            div(
                              span("Description : ").withClass("description-title"),
                              span(
                                TextRender.build(context, group).text(parameter.text())
                              ).withClass("description")
                            )
                          )
                        )
                      ).withClass("parameter-description")
                    )
                  ).withClass("parameter-block")
                )
              ).withClass("block-detail-section")
            )
          );
    }

    @Nullable
    private DomContent returnType(@Nonnull FunctionModel function) {
        return
          iff(function.type(), (EelType type) ->
            div(
              join(
                h3("Return type:")
              ),
              join(
                div(
                  type.toString()
                ).withClass("parameterType")
              )
            ).withClass("block-detail-section")
          );
    }


    @Nullable
    ContainerTag<?> textTags(@Nonnull ElementModel model, @Nonnull TagType type) {
        List<TagModel> tags = model.tags(type);

        return
          iff((!tags.isEmpty() && type.isEnabled()),
            div(
              span(
                join(
                  h3(type + ":")
                ),
                each(tags, tag ->
                  div(
                    tagBody(tag)
                  )
                )
              ).withClass("block-detail-section")
            )
          );
    }

    @Nonnull
    private DomContent tagBody(@Nonnull TagModel tag) {
        DomContent content = tag.reference()                    // used by @throws clauses
            .map(reference -> reference(tag, reference))
            .orElse(null);

        return join(
          content,
          span(
            TextRender.build(context, group).text(tag.text())
          ).withClass("description"));
    }

    @Nullable
    private DomContent reference(@Nonnull TagModel tag, @Nonnull String reference) {
        DomContent content = TextRender.build(context, group)
            .link(tag, reference);

        if (content != null) {
            content = span(
              content,
              text(" ")
            );
        }

        return content;
    }



    @Nonnull
    private List<String> signatureLists(@Nonnull FunctionModel function) {
        Collection<ParamModel> parameters = function.parameters();
        List<String> calls = new ArrayList<>(parameters.size());
        StringBuilder builder = new StringBuilder(function.name())
            .append('(');
        String delimiter = "";

        for (var parameter : parameters) {
            if (parameter.isDefaulted()) {
                int index = builder.length();

                calls.add(builder.append(')').toString());
                builder.setLength(index);
            }

            builder.append(delimiter)
                .append(parameter.identifier());

            if (parameter.isVarArgs()) {
                builder.append("...");
            }

            delimiter = ", ";
        }

        calls.add(builder.append(')').toString());
        return calls;
    }
}
