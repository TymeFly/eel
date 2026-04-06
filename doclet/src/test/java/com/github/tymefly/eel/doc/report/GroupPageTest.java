package com.github.tymefly.eel.doc.report;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import com.github.tymefly.eel.doc.context.EelDocContext;
import com.github.tymefly.eel.doc.model.ElementModel;
import com.github.tymefly.eel.doc.model.FunctionModel;
import com.github.tymefly.eel.doc.model.GroupModel;
import com.github.tymefly.eel.doc.model.ParamModel;
import com.github.tymefly.eel.doc.model.TagModel;
import com.github.tymefly.eel.doc.model.TagType;
import com.github.tymefly.eel.doc.model.TextModel;
import com.github.tymefly.eel.doc.utils.EelType;
import j2html.tags.ContainerTag;
import j2html.tags.DomContent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.MockedStatic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

/**
 * Unit test for {@link GroupPage}
 */
public class GroupPageTest {

    @TempDir
    public java.io.File temporaryFolder;

    private EelDocContext context;
    private GroupModel group;
    private FunctionModel function;
    private ParamModel parameter;
    private TagModel tag;
    private TextModel textModel;
    private GroupPage page;

    @BeforeEach
    public void setup() {
        context = mock(EelDocContext.class);
        group = mock(GroupModel.class);
        function = mock(FunctionModel.class);
        parameter = mock(ParamModel.class);
        tag = mock(TagModel.class);
        textModel = mock(TextModel.class);

        // Group
        when(group.name())
            .thenReturn("myGroups");
        when(group.hasDescription())
            .thenReturn(true);
        when(group.getFunctions())
            .thenReturn(List.of(function));
        when(group.summary())
            .thenReturn(Optional.of(tag));
        when(group.fileName())
            .thenReturn("myGroups.html");
        when(group.tags(any(TagType.class)))
            .thenReturn(Collections.emptyList());

        // Function
        when(function.name())
            .thenReturn("myFunction");
        when(function.eelSignature())
            .thenReturn("myFunction(arg1, arg2)");
        when(function.uniqueId())
            .thenReturn("myFunction");
        when(function.type())
            .thenReturn(Optional.of(EelType.NUMBER));
        when(function.summary())
            .thenReturn(Optional.of(tag));
        when(function.parameters())
            .thenReturn(List.of(parameter));

        when(parameter.identifier())
            .thenReturn("arg");
        when(parameter.isVarArgs())
            .thenReturn(false);
        when(parameter.isDefaulted())
            .thenReturn(false);
        when(parameter.type())
            .thenReturn(Optional.of(EelType.NUMBER));
        when(parameter.defaultDescription())
            .thenReturn(Optional.of("10"));
        when(parameter.text())
            .thenReturn(List.of(textModel));

        when(tag.text())
            .thenReturn(List.of(textModel));

        page = new GroupPage(context, "myGroups", group);
    }


    /**
     * Unit test for {@link GroupPage#disabledMenuOptions()}
     */
    @Test
    public void test_disabledMenuOptions() {
        Set<MenuItem> expected = Set.of();
        EnumSet<MenuItem> actual = page.disabledMenuOptions();

        assertEquals(expected, actual, "No options should be disabled");
    }


    /**
     * Unit test for {@link GroupPage#buildPageContent()}
     */
    @Test
    public void test_buildPageContent_group() {
        DomContent domContent = mock(DomContent.class);
        TextRender textRenderInstance = mock(TextRender.class);

        when(domContent.render())
            .thenReturn("<div>myGroups</div>");
        when(textRenderInstance.text(any(Collection.class)))
            .thenReturn(domContent);

        try (
            MockedStatic<TextRender> textRenderMock = mockStatic(TextRender.class)
        ) {
            textRenderMock.when(() -> TextRender.build(context, group))
                .thenReturn(textRenderInstance);

            DomContent actual = page.buildPageContent();
            String html = actual.render();

            assertTrue(html.contains("<h2 class=\"block-name\" id=\"_Group_\">myGroups</h2>"), "Missing group name");
        }
    }

    /**
     * Unit test for {@link GroupPage#buildPageContent()}
     */
    @Test
    public void test_buildPageContent_function() {
        DomContent domContent = mock(DomContent.class);
        TextRender textRenderInstance = mock(TextRender.class);

        when(domContent.render())
            .thenReturn("<div>myFunction(arg1, arg2)</div>");
        when(textRenderInstance.text(any(Collection.class)))
            .thenReturn(domContent);

        try (
            MockedStatic<TextRender> textRenderMock = mockStatic(TextRender.class)
        ) {
            textRenderMock.when(() -> TextRender.build(context, group))
                .thenReturn(textRenderInstance);

            DomContent actual = page.buildPageContent();
            String html = actual.render();

            assertTrue(html.contains("id=\"myFunction\">myFunction(arg1, arg2)"), "Missing function signature");
        }
    }

    /**
     * Unit test for {@link GroupPage#buildPageContent()}
     */
    @Test
    public void test_buildPageContent_parameters() {
        DomContent domContent = mock(DomContent.class);
        TextRender textRenderInstance = mock(TextRender.class);

        when(domContent.render())
            .thenReturn("<div>param</div>");
        when(textRenderInstance.text(any(Collection.class)))
            .thenReturn(domContent);

        try (
            MockedStatic<TextRender> textRenderMock = mockStatic(TextRender.class)
        ) {
            textRenderMock.when(() -> TextRender.build(context, group))
                .thenReturn(textRenderInstance);

            DomContent actual = page.buildPageContent();
            String html = actual.render();

            assertTrue(html.contains("<div class=\"parameter-block\">"), "Missing parameter block");
            assertTrue(html.contains("<code>arg</code>"), "Missing parameter name");
            assertTrue(html.contains("<code class=\"parameterType description\">Number</code>"), "Missing parameter desc");
            assertTrue(html.contains("<span class=\"description\">10</span>"), "Missing parameter default");
        }
    }

    /**
     * Unit test for {@link GroupPage#buildPageContent()}
     */
    @Test
    public void test_buildPageContent_defaultedParameter() {
        DomContent domContent = mock(DomContent.class);
        TextRender textRenderInstance = mock(TextRender.class);

        when(parameter.isDefaulted())
            .thenReturn(true);
        when(parameter.identifier())
            .thenReturn("arg");

        when(domContent.render())
            .thenReturn("<div>myFunction()</div>");
        when(textRenderInstance.text(any(Collection.class)))
            .thenReturn(domContent);

        try (
            MockedStatic<TextRender> textRenderMock = mockStatic(TextRender.class)
        ) {
            textRenderMock.when(() -> TextRender.build(context, group))
                .thenReturn(textRenderInstance);

            DomContent content = page.buildPageContent();
            String actual = content.render();

            assertTrue(actual.contains("<code>myFunction()</code>"), "Signature with defaulted parameter");
            assertTrue(actual.contains("<code>myFunction(arg)</code>"), "Signature without defaulted parameter");
        }
    }

    /**
     * Unit test for {@link GroupPage#buildPageContent()}
     */
    @Test
    public void test_buildPageContent_varArgsParameter() {
        DomContent domContent = mock(DomContent.class);
        TextRender textRenderInstance = mock(TextRender.class);

        when(parameter.isDefaulted())
            .thenReturn(false);
        when(parameter.isVarArgs())
            .thenReturn(true);
        when(parameter.identifier())
            .thenReturn("arg");

        when(domContent.render())
            .thenReturn("<div>myFunction(arg...)</div>");

        when(textRenderInstance.text(any(Collection.class)))
            .thenReturn(domContent);

        try (
            MockedStatic<TextRender> textRenderMock = mockStatic(TextRender.class)
        ) {
            textRenderMock.when(() -> TextRender.build(context, group)).thenReturn(textRenderInstance);

            DomContent actual = page.buildPageContent();
            String html = actual.render();

            assertTrue(html.contains("myFunction(arg...)"), "buildPageContent renders call with varargs parameter");
        }
    }

    /**
     * Unit test for {@link GroupPage#buildPageContent()}
     */
    @Test
    public void test_buildPageContent_returnType() {
        DomContent domContent = mock(DomContent.class);
        TextRender textRenderInstance = mock(TextRender.class);

        when(domContent.render())
            .thenReturn("<div>NUMBER</div>");
        when(textRenderInstance.text(any(Collection.class)))
            .thenReturn(domContent);

        try (
            MockedStatic<TextRender> textRenderMock = mockStatic(TextRender.class)
        ) {
            textRenderMock.when(() -> TextRender.build(context, group))
                .thenReturn(textRenderInstance);

            DomContent actual = page.buildPageContent();
            String html = actual.render();

            assertTrue(html.contains("<div class=\"parameterType\">Number</div>"), "Missing return type");
        }
    }

    /**
     * Unit test for {@link GroupPage#buildPageContent()}
     */
    @Test
    public void test_buildPageContent_deprecatedText() {
        DomContent domContent = mock(DomContent.class);
        TextRender textRenderInstance = mock(TextRender.class);

        when(domContent.render())
            .thenReturn("<div>Deprecated</div>");
        when(textRenderInstance.text(any(Collection.class)))
            .thenReturn(domContent);
        when(function.deprecated())
            .thenReturn(Optional.of(tag));

        try (
            MockedStatic<TextRender> textRenderMock = mockStatic(TextRender.class)
        ) {
            textRenderMock.when(() -> TextRender.build(context, group))
                .thenReturn(textRenderInstance);

            DomContent actual = page.buildPageContent();
            String html = actual.render();

            assertTrue(html.contains("<h3>Deprecated</h3>"), "Missing deprecated warning");
        }
    }

    /**
     * Unit test for {@link GroupPage#buildPageContent()}
     */
    @Test
    public void test_buildPageContent_empty() {
        DomContent domContent = mock(DomContent.class);
        TextRender textRenderInstance = mock(TextRender.class);

        when(group.hasDescription())
            .thenReturn(false);
        when(function.parameters())
            .thenReturn(Collections.emptyList());
        when(function.summary())
            .thenReturn(Optional.empty());
        when(function.type())
            .thenReturn(Optional.empty());

        when(domContent.render())
            .thenReturn("<div>myGroups</div>");
        when(textRenderInstance.text(any(Collection.class)))
            .thenReturn(domContent);

        try (
            MockedStatic<TextRender> textRenderMock = mockStatic(TextRender.class)
        ) {
            textRenderMock.when(() -> TextRender.build(context, group))
                .thenReturn(textRenderInstance);

            DomContent actual = page.buildPageContent();
            String html = actual.render();

            assertTrue(html.contains("MyGroups"), "Expected group name");
        }
    }

    /**
     * Unit test for {@link GroupPage#textTags(ElementModel, TagType)}
     */
    @Test
    public void test_textTags() {
        TagModel tagWithReference = mock(TagModel.class);
        DomContent linkContent = mock(DomContent.class);
        TextRender textRenderInstance = mock(TextRender.class);

        when(tagWithReference.text())
            .thenReturn(List.of(textModel));
        when(tagWithReference.reference())
            .thenReturn(Optional.of("ref"));

        when(group.tags(TagType.RETURN))
            .thenReturn(List.of(tagWithReference));

        when(linkContent.render())
            .thenReturn("<span>link</span>");

        when(textRenderInstance.text(any(Collection.class)))
            .thenReturn(mock(DomContent.class));
        when(textRenderInstance.link(tagWithReference, "ref"))
            .thenReturn(linkContent);

        try (
            MockedStatic<TextRender> textRenderMock = mockStatic(TextRender.class)
        ) {
            textRenderMock.when(() -> TextRender.build(context, group)).thenReturn(textRenderInstance);

            ContainerTag<?> actual = page.textTags(group, TagType.RETURN);
            String html = actual.render();

            assertTrue(html.contains("<h3>Returns:</h3>"), "Missing Tag name");
        }
    }
}
