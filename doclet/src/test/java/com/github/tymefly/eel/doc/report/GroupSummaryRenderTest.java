package com.github.tymefly.eel.doc.report;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import com.github.tymefly.eel.doc.context.Context;
import com.github.tymefly.eel.doc.model.FunctionModel;
import com.github.tymefly.eel.doc.model.GroupModel;
import com.github.tymefly.eel.doc.model.TagModel;
import com.github.tymefly.eel.doc.model.TextModel;
import com.github.tymefly.eel.doc.utils.EelType;
import j2html.tags.DomContent;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.mockito.MockedStatic;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;


/**
 * Unit test for {@link GroupSummaryRender}
 */
public class GroupSummaryRenderTest {
    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    private Context context;
    private GroupModel group;
    private FunctionModel function;
    private TagModel tag;


    @Before
    public void setup() {
        context = mock(Context.class);
        group = mock(GroupModel.class);
        function = mock(FunctionModel.class);
        tag = mock(TagModel.class);
        
        TextModel textModel = mock(TextModel.class);

        when(group.name())
            .thenReturn("Math");
        when(group.hasDescription())
            .thenReturn(true);
        when(group.getFunctions())
            .thenReturn(List.of(function));
        when(group.summary())
            .thenReturn(Optional.of(tag));
        when(group.fileName())
            .thenReturn("math.html");

        when(function.eelSignature())
            .thenReturn("sum(a, b)");
        when(function.uniqueId())
            .thenReturn("myFunction");
        when(function.type())
            .thenReturn(Optional.of(EelType.NUMBER));
        when(function.summary())
            .thenReturn(Optional.of(tag));

        when(tag.text())
            .thenReturn(List.of(textModel));
    }

    /**
     * Unit test for {@link GroupSummaryRender#render()}
     */
    @Test
    public void test_render_local() {
        DomContent domContent = mock(DomContent.class);
        TextRender textRenderInstance = mock(TextRender.class);

        when(domContent.render())
            .thenReturn("<html>Math group</html>");
        when(textRenderInstance.text(any(Collection.class)))
            .thenReturn(domContent);

        try (
            MockedStatic<TextRender> textRenderMock = mockStatic(TextRender.class)
        ) {
            textRenderMock.when(() -> TextRender.build(context, group)).thenReturn(textRenderInstance);

            GroupSummaryRender render = GroupSummaryRender.local(context, group);
            DomContent actual = render.render();
            String html = actual.render();

            assertTrue("HTML contains group name", html.contains("Math group"));
        }
    }

    /**
     * Unit test for {@link GroupSummaryRender#render()}
     */
    @Test
    public void test_render_general() {
        DomContent domContent = mock(DomContent.class);
        TextRender textRenderInstance = mock(TextRender.class);

        when(domContent.render())
            .thenReturn("<html><a href=\"math.html\">Math group</a></html>");
        when(textRenderInstance.text(any(Collection.class)))
            .thenReturn(domContent);

        try (
            MockedStatic<TextRender> textRenderMock = mockStatic(TextRender.class)
        ) {
            textRenderMock.when(() -> TextRender.build(context, group))
                .thenReturn(textRenderInstance);

            GroupSummaryRender render = GroupSummaryRender.general(context, group);
            DomContent actual = render.render();
            String html = actual.render();

            assertTrue("HTML contains link to group page", html.contains("href=\"math.html\""));
            assertTrue("HTML contains group name", html.contains("Math group"));
        }
    }

    /**
     * Unit test for {@link GroupSummaryRender#render()}
     */
    @Test
    public void test_render_singleFunction() {
        DomContent domContent = mock(DomContent.class);
        TextRender textRenderInstance = mock(TextRender.class);

        when(domContent.render())
            .thenReturn("<html>sum(a, b) NUMBER</html>");
        when(textRenderInstance.text(any(Collection.class)))
            .thenReturn(domContent);

        try (
            MockedStatic<TextRender> textRenderMock = mockStatic(TextRender.class)
        ) {
            textRenderMock.when(() -> TextRender.build(context, group))
                .thenReturn(textRenderInstance);

            GroupSummaryRender render = GroupSummaryRender.local(context, group);
            DomContent actual = render.render();
            String html = actual.render();

            assertTrue("HTML contains function signature", html.contains("sum(a, b)"));
            assertTrue("HTML contains function type", html.contains("Number"));
        }
    }

    /**
     * Unit test for {@link GroupSummaryRender#render()}
     */
    @Test
    public void test_render_multipleFunctions() {
        FunctionModel otherFunction = mock(FunctionModel.class);
        DomContent domContent = mock(DomContent.class);
        TextRender textRenderInstance = mock(TextRender.class);

        when(otherFunction.eelSignature())
            .thenReturn("multiply(a, b)");
        when(otherFunction.uniqueId())
            .thenReturn("myFunction2");
        when(otherFunction.type())
            .thenReturn(Optional.of(EelType.NUMBER));
        when(otherFunction.summary())
            .thenReturn(Optional.of(tag));

        when(group.getFunctions())
            .thenReturn(List.of(function, otherFunction));

        when(domContent.render())
            .thenReturn("<html>sum(a, b) multiply(a, b)</html>");

        when(textRenderInstance.text(any(Collection.class)))
            .thenReturn(domContent);

        try (
            MockedStatic<TextRender> textRenderMock = mockStatic(TextRender.class)
        ) {
            textRenderMock.when(() -> TextRender.build(context, group))
                .thenReturn(textRenderInstance);

            GroupSummaryRender render = GroupSummaryRender.local(context, group);
            DomContent actual = render.render();
            String html = actual.render();

            assertTrue("HTML contains first function", html.contains("sum(a, b)"));
            assertTrue("HTML contains second function", html.contains("multiply(a, b)"));
        }
    }


    /**
     * Unit test for {@link GroupSummaryRender#render()}
     */
    @Test
    public void test_render_NoSummaryOrFunctions() {
        DomContent domContent = mock(DomContent.class);
        TextRender textRenderInstance = mock(TextRender.class);

        when(group.summary())
            .thenReturn(Optional.empty());
        when(function.type())
            .thenReturn(Optional.empty());
        when(function.summary())
            .thenReturn(Optional.empty());

        when(domContent.render())
            .thenReturn("<html>Math group</html>");

        when(textRenderInstance.text(any(Collection.class)))
            .thenReturn(domContent);

        try (
            MockedStatic<TextRender> textRenderMock = mockStatic(TextRender.class)
        ) {
            textRenderMock.when(() -> TextRender.build(context, group))
                .thenReturn(textRenderInstance);

            GroupSummaryRender render = GroupSummaryRender.local(context, group);
            DomContent actual = render.render();
            String html = actual.render();

            assertTrue("HTML still renders without summaries or types", html.contains("Math group"));
        }
    }
}