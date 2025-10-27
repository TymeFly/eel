package com.github.tymefly.eel.doc.scanner;

import java.util.List;
import java.util.stream.Stream;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.PackageElement;

import com.github.tymefly.eel.doc.context.Context;
import com.github.tymefly.eel.doc.model.FunctionGenerator;
import com.github.tymefly.eel.doc.model.GroupGenerator;
import com.github.tymefly.eel.doc.model.ModelManager;
import com.github.tymefly.eel.doc.model.TextBlockGenerator;
import com.github.tymefly.eel.doc.source.Parameter;
import com.github.tymefly.eel.doc.source.ParameterList;
import com.github.tymefly.eel.doc.source.Source;
import com.github.tymefly.eel.doc.utils.EelType;
import com.github.tymefly.eel.udf.EelFunction;
import com.github.tymefly.eel.udf.GroupDescription;
import com.sun.source.doctree.DocCommentTree;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit test {@link RootScanner}
 */
public class RootScannerTest {
    private Context context;
    private ModelManager modelManager;
    private GroupGenerator groupGenerator;
    private FunctionGenerator functionGenerator;
    private Source source;
    private PackageElement packageElement;
    private ExecutableElement methodElement;

    @Before
    public void setUp() {
        context = mock(Context.class);
        modelManager = mock(ModelManager.class);
        groupGenerator = mock(GroupGenerator.class);
        functionGenerator = mock(FunctionGenerator.class);
        source = mock(Source.class);
        packageElement = mock(PackageElement.class);
        methodElement = mock(ExecutableElement.class);

        when(context.modelManager())
            .thenReturn(modelManager);
    }

    /**
     * Unit test {@link RootScanner#run(Context, Element)}
     */
    @Test
    public void test_run_packageElement_noDescription() {
        Element element = mock(PackageElement.class);

        when(element.getKind())
            .thenReturn(ElementKind.PACKAGE);

        RootScanner.run(context, element);

        verify(context, never()).modelManager();
    }

    /**
     * Unit test {@link RootScanner#run(Context, Element)}
     */
    @Test
    public void test_run_packageElement_withDescription_NoSummary() {
        GroupDescription description = mock(GroupDescription.class);

        when(packageElement.getKind())
            .thenReturn(ElementKind.PACKAGE);
        when(packageElement.getAnnotation(GroupDescription.class))
            .thenReturn(description);
        when(description.value())
            .thenReturn("myGroup");

        try (
            MockedStatic<Source> mockedSource = Mockito.mockStatic(Source.class);
            MockedStatic<PackageScanner> mockedPackageScanner = Mockito.mockStatic(PackageScanner.class);
            MockedStatic<TextBlockScanner> mockedTextBlockScanner = Mockito.mockStatic(TextBlockScanner.class)
        ) {
            DocCommentTree docTree = mock(DocCommentTree.class);
            TextBlockGenerator summary = mock(TextBlockGenerator.class);

            mockedSource.when(() -> Source.forPackage(context, packageElement))
                .thenReturn(source);
            mockedPackageScanner.when(() -> PackageScanner.run(source, groupGenerator))
                .thenAnswer(i -> null);
            mockedTextBlockScanner.when(() -> TextBlockScanner.run(source, docTree.getFirstSentence()))
                .thenReturn(summary);

            when(source.hasDocCommentTree())
                .thenReturn(true);
            when(modelManager.group("myGroup"))
                .thenReturn(groupGenerator);
            when(groupGenerator.hasDescription())
                .thenReturn(false);
            when(groupGenerator.withDescription())
                .thenReturn(groupGenerator);
            when(groupGenerator.hasSummary())
                .thenReturn(false);

            when(source.docCommentTree())
                .thenReturn(docTree);

            RootScanner.run(context, packageElement);

            verify(modelManager, times(2)).group("myGroup");            // mockito also makes a call
            verify(groupGenerator).withDescription();
            verify(groupGenerator).addSummary(any(TextBlockGenerator.class));
        }
    }


    /**
     * Unit test {@link RootScanner#run(Context, Element)}
     */
    @Test
    public void test_run_packageElement_withDescription_withSummary() {
        GroupDescription description = mock(GroupDescription.class);

        when(packageElement.getKind())
            .thenReturn(ElementKind.PACKAGE);
        when(packageElement.getAnnotation(GroupDescription.class))
            .thenReturn(description);
        when(description.value())
            .thenReturn("myGroup");

        try (
            MockedStatic<Source> mockedSource = Mockito.mockStatic(Source.class);
            MockedStatic<PackageScanner> mockedPackageScanner = Mockito.mockStatic(PackageScanner.class);
            MockedStatic<TextBlockScanner> mockedTextBlockScanner = Mockito.mockStatic(TextBlockScanner.class)
        ) {
            DocCommentTree docTree = mock(DocCommentTree.class);
            TextBlockGenerator summary = mock(TextBlockGenerator.class);

            mockedSource.when(() -> Source.forPackage(context, packageElement))
                .thenReturn(source);
            mockedPackageScanner.when(() -> PackageScanner.run(source, groupGenerator))
                .thenAnswer(i -> null);
            mockedTextBlockScanner.when(() -> TextBlockScanner.run(source, docTree.getFirstSentence()))
                .thenReturn(summary);

            when(source.hasDocCommentTree())
                .thenReturn(true);
            when(modelManager.group("myGroup"))
                .thenReturn(groupGenerator);
            when(groupGenerator.hasDescription())
                .thenReturn(false);
            when(groupGenerator.withDescription())
                .thenReturn(groupGenerator);
            when(groupGenerator.hasSummary())
                .thenReturn(true);

            when(source.docCommentTree())
                .thenReturn(docTree);

            RootScanner.run(context, packageElement);

            verify(modelManager, times(2)).group("myGroup");            // mockito also makes a call
            verify(groupGenerator).withDescription();
            verify(groupGenerator, never()).addSummary(any(TextBlockGenerator.class));
        }
    }


    /**
     * Unit test {@link RootScanner#run(Context, Element)}
     */
    @Test
    public void test_run_packageElement_duplicateDescription() {
        GroupDescription description = mock(GroupDescription.class);

        when(packageElement.getKind())
            .thenReturn(ElementKind.PACKAGE);
        when(packageElement.getAnnotation(GroupDescription.class))
            .thenReturn(description);
        when(description.value())
            .thenReturn("dupGroup");

        try (
            MockedStatic<Source> mockedSource = Mockito.mockStatic(Source.class);
            MockedStatic<PackageScanner> mockedPackageScanner = Mockito.mockStatic(PackageScanner.class)
        ) {
            mockedSource.when(() -> Source.forPackage(context, packageElement))
                .thenReturn(source);
            mockedPackageScanner.when(() -> PackageScanner.run(source, groupGenerator))
                .thenAnswer(i -> null);

            when(source.hasDocCommentTree())
                .thenReturn(true);
            when(modelManager.group("dupGroup"))
                .thenReturn(groupGenerator);
            when(groupGenerator.hasDescription())
                .thenReturn(true);

            RootScanner.run(context, packageElement);

            verify(context)
                .error(Mockito.contains("Multiple descriptions for group dupGroup"));
        }
    }

    /**
     * Unit test {@link RootScanner#run(Context, Element)}
     */
    @Test
    public void test_classElement_withUndocumentedEelFunction() {
        Element classElement = mock(Element.class);
        EelFunction eelFunction = mock(EelFunction.class);
        ParameterList parameters = mock(ParameterList.class);
        Parameter parameter = mock(Parameter.class);
        Stream<Parameter> parameterStream = Stream.of(parameter);

        when(parameter.name())
            .thenReturn("param1");

        when(classElement.getKind())
            .thenReturn(ElementKind.CLASS);
        when(classElement.getEnclosedElements())
            .thenAnswer(i -> List.of(methodElement));

        when(methodElement.getKind())
            .thenReturn(ElementKind.METHOD);
        when(methodElement.getAnnotation(EelFunction.class))
            .thenReturn(eelFunction);
        when(eelFunction.value())
            .thenReturn("myFunction");

        when(modelManager.hasFunction("myFunction"))
            .thenReturn(false);

        try (
            MockedStatic<Source> mockedSource = Mockito.mockStatic(Source.class)
        ) {
            mockedSource.when(() -> Source.forMethod(context, methodElement))
                .thenReturn(source);

            when(source.eelType())
                .thenReturn(EelType.TEXT);
            when(source.signature())
                .thenReturn("sig()");
            when(modelManager.addFunction(eq("myFunction"), eq(EelType.TEXT), anyString()))
                .thenReturn(functionGenerator);
            when(source.hasDocCommentTree())
                .thenReturn(false);

            when(source.parameters())
                .thenReturn(parameters);
            when(parameters.stream())
                .thenReturn(parameterStream);

            RootScanner.run(context, classElement);

            verify(modelManager).addFunction(eq("myFunction"), eq(EelType.TEXT), anyString());
            verify(functionGenerator).addParameter(eq("param1"), eq(parameter));
            verify(functionGenerator, never()).addSummary(any());
        }
    }


    /**
     * Unit test {@link RootScanner#run(Context, Element)}
     */
    @Test
    public void test_functionElement_documented_withSummaryTag() {
        EelFunction eelFunction = mock(EelFunction.class);
        ParameterList parameters = mock(ParameterList.class);
        Parameter parameter = mock(Parameter.class);
        Stream<Parameter> parameterStream = Stream.of(parameter);
        DocCommentTree docTree = mock(DocCommentTree.class);
        TextBlockGenerator summary = mock(TextBlockGenerator.class);

        when(parameter.name())
            .thenReturn("param1");

        when(methodElement.getKind())
            .thenReturn(ElementKind.METHOD);
        when(methodElement.getAnnotation(EelFunction.class))
            .thenReturn(eelFunction);
        when(eelFunction.value())
            .thenReturn("myFunction");

        when(modelManager.hasFunction("myFunction"))
            .thenReturn(false);

        when(source.eelType())
            .thenReturn(EelType.NUMBER);
        when(source.signature())
            .thenReturn("sig()");
        when(modelManager.addFunction(eq("myFunction"), eq(EelType.NUMBER), anyString()))
            .thenReturn(functionGenerator);
        when(functionGenerator.hasSummary())
            .thenReturn(true);
        when(source.hasDocCommentTree())
            .thenReturn(true);
        when(source.parameters())
            .thenReturn(parameters);
        when(parameters.stream())
            .thenReturn(parameterStream);
        when(source.docCommentTree())
            .thenReturn(docTree);

        try (
            MockedStatic<Source> mockedSource = Mockito.mockStatic(Source.class);
            MockedStatic<FunctionScanner> mockedFunctionScanner = Mockito.mockStatic(FunctionScanner.class);
            MockedStatic<TextBlockScanner> mockedTextBlockScanner = Mockito.mockStatic(TextBlockScanner.class)
        ) {
            mockedSource.when(() -> Source.forMethod(context, methodElement))
                .thenReturn(source);

            mockedTextBlockScanner.when(() -> TextBlockScanner.run(source, docTree.getFirstSentence()))
                .thenReturn(summary);

            RootScanner.run(context, methodElement);

            verify(modelManager).addFunction(eq("myFunction"), eq(EelType.NUMBER), anyString());
            verify(functionGenerator).addParameter(eq("param1"), eq(parameter));
            verify(functionGenerator, never()).addSummary(any(TextBlockGenerator.class));
        }
    }


    /**
     * Unit test {@link RootScanner#run(Context, Element)}
     */
    @Test
    public void test_functionElement_documented_withoutSummaryTag() {
        ParameterList parameters = mock(ParameterList.class);
        DocCommentTree docTree = mock(DocCommentTree.class);
        TextBlockGenerator summary = mock(TextBlockGenerator.class);
        EelFunction eelFunction = mock(EelFunction.class);

        when(methodElement.getKind())
            .thenReturn(ElementKind.METHOD);
        when(methodElement.getAnnotation(EelFunction.class))
            .thenReturn(eelFunction);
        when(eelFunction.value())
            .thenReturn("myFunction");

        when(modelManager.hasFunction("myFunction"))
            .thenReturn(false);

        when(source.hasDocCommentTree())
            .thenReturn(true);
        when(source.parameters())
            .thenReturn(parameters);
        when(source.docCommentTree())
            .thenReturn(docTree);
        when(source.eelType())
            .thenReturn(EelType.NUMBER);
        when(source.signature())
            .thenReturn("sig()");

        when(modelManager.addFunction(eq("myFunction"), eq(EelType.NUMBER), anyString()))
            .thenReturn(functionGenerator);
        when(functionGenerator.hasSummary())
            .thenReturn(false);

        try (
            MockedStatic<Source> mockedSource = Mockito.mockStatic(Source.class);
            MockedStatic<FunctionScanner> mockedFunctionScanner = Mockito.mockStatic(FunctionScanner.class);
            MockedStatic<TextBlockScanner> mockedTextBlockScanner = Mockito.mockStatic(TextBlockScanner.class)
        ) {
            mockedSource.when(() -> Source.forMethod(context, methodElement))
                .thenReturn(source);
            mockedTextBlockScanner.when(() -> TextBlockScanner.run(source, docTree.getFirstSentence()))
                .thenReturn(summary);

            RootScanner.run(context, methodElement);

            verify(modelManager).addFunction(eq("myFunction"), eq(EelType.NUMBER), anyString());
            verify(functionGenerator).addSummary(any(TextBlockGenerator.class));
        }
    }


    /**
     * Unit test {@link RootScanner#run(Context, Element)}
     */
    @Test
    public void test_functionElement_unannotated() {
        ExecutableElement method = mock(ExecutableElement.class);

        when(method.getKind())
            .thenReturn(ElementKind.METHOD);
        when(method.getAnnotation(EelFunction.class))
            .thenReturn(null);

        RootScanner.run(context, method);

        verify(modelManager, never()).addFunction(anyString(), any(), anyString());
    }

    /**
     * Unit test {@link RootScanner#run(Context, Element)}
     */
    @Test
    public void test_functionElement_alreadyDefined() {
        ExecutableElement method = mock(ExecutableElement.class);
        EelFunction eelFunction = mock(EelFunction.class);

        when(method.getKind())
            .thenReturn(ElementKind.METHOD);
        when(method.getAnnotation(EelFunction.class))
            .thenReturn(eelFunction);
        when(eelFunction.value())
            .thenReturn("myFunction");

        when(modelManager.hasFunction("myFunction"))
            .thenReturn(true);

        RootScanner.run(context, method);

        verify(modelManager, never()).addFunction(anyString(), any(), anyString());
    }


    /**
     * Unit test {@link RootScanner#run(Context, Element)}
     */
    @Test
    public void test_unsupportedElement() {
        Element element = mock(Element.class);

        when(element.getKind())
            .thenReturn(ElementKind.FIELD);

        RootScanner.run(context, element);

        verify(context, never()).modelManager();
    }
}
