package com.github.tymefly.eel.doc.report;

import java.util.Collection;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.github.tymefly.eel.doc.context.Context;
import com.github.tymefly.eel.doc.model.FunctionModel;
import com.github.tymefly.eel.doc.model.GroupModel;
import com.github.tymefly.eel.doc.model.ModelManager;
import com.github.tymefly.eel.doc.model.TagModel;
import com.github.tymefly.eel.doc.model.TagType;
import com.github.tymefly.eel.doc.model.TextModel;
import com.github.tymefly.eel.doc.model.TextStyle;
import j2html.tags.DomContent;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

/**
 * Unit Test {@link TextRender}
 */
public class TextRenderTest {
    private Context context;
    private GroupModel group;
    private ModelManager modelManager;
    private FunctionModel function;


    @Before
    public void setUp() {
        context = Mockito.mock(Context.class);
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
        assertEquals("No Style",
            "Hello",
            text_allStyleHelper(TextStyle.NONE, "hello").render());
        assertEquals("RAW style",
            "<b>raw</b>",
            text_allStyleHelper(TextStyle.RAW, "<b>raw</b>").render());
        assertEquals("code text",
            "<code>code</code>",
            text_allStyleHelper(TextStyle.CODE, "code").render());
        assertEquals("literal text",
            "<span class=\"literal\">lit</span>",
            text_allStyleHelper(TextStyle.LITERAL, "lit").render());
        assertEquals("plain link",
            "<span class=\"plain-link\"><code>Plain</code></span>",
            text_allStyleHelper(TextStyle.PLAIN_LINK, "plain").render());
        assertEquals("broken link",
            "<code>Link</code>",
            text_allStyleHelper(TextStyle.LINK, "link").render());
        assertEquals("error text",
            "<span class=\"invalid-highlight\">Error</span>",
            text_allStyleHelper(TextStyle.ERROR, "error").render());
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
        Assert.assertEquals("Failed to capitalise", "First second", content.render());
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

        Assert.assertEquals("valid link", "<a href=\"#uid\">func</a>", content.render());
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

        Assert.assertEquals("valid link", "<a href=\"_other.html#uid\">func</a>", content.render());
    }

    /**
     * Unit Test {@link TextRender#link(TagModel, String)}
     */
    @Test
    public void test_link_brokenLink() {
        when(modelManager.bySignature("#my.package.MyClass#myMethod"))
            .thenReturn(null);

        DomContent content = renderLink("#my.package.MyClass#myMethod", true);

        Assert.assertEquals("valid link", "<code>MyClass.myMethod</code>", content.render());
    }

    /**
     * Unit Test {@link TextRender#link(TagModel, String)}
     */
    @Test
    public void test_link_hiddenLink() {
        when(modelManager.bySignature("missingSig"))
            .thenReturn(null);

        DomContent content = renderLink("missingSig", false);

        Assert.assertNull("valid link", content);
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
