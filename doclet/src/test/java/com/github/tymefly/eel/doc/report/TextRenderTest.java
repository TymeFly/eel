package com.github.tymefly.eel.doc.report;

import java.util.Collection;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.github.tymefly.eel.doc.context.EelDocContext;
import com.github.tymefly.eel.doc.model.FunctionModel;
import com.github.tymefly.eel.doc.model.GroupModel;
import com.github.tymefly.eel.doc.model.ModelManager;
import com.github.tymefly.eel.doc.model.TagModel;
import com.github.tymefly.eel.doc.model.TagType;
import com.github.tymefly.eel.doc.model.TextModel;
import com.github.tymefly.eel.doc.model.TextStyle;
import j2html.tags.DomContent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.when;

/**
 * Unit Test {@link TextRender}
 */
public class TextRenderTest {
    private EelDocContext context;
    private GroupModel group;
    private ModelManager modelManager;
    private FunctionModel function;


    @BeforeEach
    public void setUp() {
        context = Mockito.mock(EelDocContext.class);
        group = Mockito.mock(GroupModel.class);
        modelManager = Mockito.mock(ModelManager.class);
        function = Mockito.mock(FunctionModel.class);

        when(context.modelManager())
            .thenReturn(modelManager);
    }


    /**
     * Unit Test {@link TextRender#text(Collection)}
     */
    @Test
    public void test_text_allStyle() {
        assertEquals("Hello",
            text_allStyleHelper(TextStyle.NONE, "hello").render(),
            "No Style");
        assertEquals("<b>raw</b>",
            text_allStyleHelper(TextStyle.RAW, "<b>raw</b>").render(),
            "RAW style");
        assertEquals("<code>code</code>",
            text_allStyleHelper(TextStyle.CODE, "code").render(),
            "code text");
        assertEquals("<span class=\"literal\">lit</span>",
            text_allStyleHelper(TextStyle.LITERAL, "lit").render(),
            "literal text");
        assertEquals("<span class=\"plain-link\"><code>Plain</code></span>",
            text_allStyleHelper(TextStyle.PLAIN_LINK, "plain").render(),
            "plain link");
        assertEquals("<code>Link</code>",
            text_allStyleHelper(TextStyle.LINK, "link").render(),
            "broken link");
        assertEquals("<span class=\"invalid-highlight\">Error</span>",
            text_allStyleHelper(TextStyle.ERROR, "error").render(),
            "error text");
    }


    @Nonnull
    private DomContent text_allStyleHelper(@Nonnull TextStyle style, @Nonnull String text) {
        TextRender render = TextRender.build(context, group);
        TextModel textModel = Mockito.mock(TextModel.class);
        DomContent actual;

        when(textModel.style())
            .thenReturn(style);
        when(textModel.text())
            .thenReturn(text);

        if ((style == TextStyle.LINK) || (style == TextStyle.PLAIN_LINK)) {
            when(textModel.target())
                .thenReturn(null);
        }

        actual = render.text(List.of(textModel));

        return actual;
    }


    /**
     * Unit Test {@link TextRender#text(Collection)}
     */
    @Test
    public void test_text_capitalisation() {
        TextModel first = Mockito.mock(TextModel.class);
        TextModel second = Mockito.mock(TextModel.class);
        TextRender render = TextRender.build(context, group);

        when(first.style())
            .thenReturn(TextStyle.NONE);
        when(first.text())
            .thenReturn("first ");

        when(second.style())
            .thenReturn(TextStyle.NONE);
        when(second.text())
            .thenReturn("second");

        DomContent content = render.text(List.of(first, second));

        assertEquals("First second", content.render(), "Failed to capitalise");
    }

    /**
     * Unit Test {@link TextRender#link(TagModel, String)}
     */
    @Test
    public void test_link_validFunction_sameGroup() {
        when(modelManager.bySignature("sig"))
            .thenReturn(function);
        when(function.uniqueId())
            .thenReturn("uid");
        when(function.eelSignature())
            .thenReturn("func");
        when(function.group())
            .thenReturn(group);

        DomContent content = renderLink("sig", true);

        assertEquals("<a href=\"#uid\">func</a>", content.render(), "valid link");
    }

    /**
     * Unit Test {@link TextRender#link(TagModel, String)}
     */
    @Test
    public void test_link_validFunction_otherGroup() {
        GroupModel otherGroup = Mockito.mock(GroupModel.class);

        when(modelManager.bySignature("sig"))
            .thenReturn(function);
        when(function.uniqueId())
            .thenReturn("uid");
        when(function.eelSignature())
            .thenReturn("func");
        when(function.group())
            .thenReturn(otherGroup);
        when(otherGroup.fileName())
            .thenReturn("_other.html");

        DomContent content = renderLink("sig", true);

        assertEquals("<a href=\"_other.html#uid\">func</a>", content.render(), "valid link");
    }

    /**
     * Unit Test {@link TextRender#link(TagModel, String)}
     */
    @Test
    public void test_link_brokenLink() {
        when(modelManager.bySignature("#my.package.MyClass#myMethod"))
            .thenReturn(null);

        DomContent content = renderLink("#my.package.MyClass#myMethod", true);

        assertEquals("<code>MyClass.myMethod</code>", content.render(), "valid link");
    }

    /**
     * Unit Test {@link TextRender#link(TagModel, String)}
     */
    @Test
    public void test_link_hiddenLink() {
        when(modelManager.bySignature("missingSig"))
            .thenReturn(null);

        DomContent content = renderLink("missingSig", false);

        assertNull(content, "valid link");
    }

    @Nullable
    private DomContent renderLink(@Nonnull String target, boolean showAllReference) {
        TagModel tag = Mockito.mock(TagModel.class);
        TagType tagType = Mockito.mock(TagType.class);
        TextRender render = TextRender.build(context, group);

        when(tag.target())
            .thenReturn(target);
        when(tag.tagType())
            .thenReturn(tagType);
        when(tagType.showAllReference())
            .thenReturn(showAllReference);

        return render.link(tag, "text");
    }
}
