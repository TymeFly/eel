package com.github.tymefly.eel.doc.source;

import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Name;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;

import com.github.tymefly.eel.doc.utils.EelType;
import com.github.tymefly.eel.udf.DefaultArgument;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

/**
 * Unit test for {@link ParameterList}
 */
public class ParameterListTest {

    @Nonnull
    private VariableElement mockParam(@Nonnull String name, @Nullable String description, boolean varArgs) {
        VariableElement mock = mock();
        Name mockName = mock();
        TypeMirror type = mock();

        when(mock.getSimpleName())
            .thenReturn(mockName);
        when(mockName.toString())
            .thenReturn(name);
        when(mock.asType())
            .thenReturn(type);
        when(type.getKind())
            .thenReturn(varArgs ? TypeKind.ARRAY : TypeKind.DECLARED);

        if (description == null) {
            when(mock.getAnnotation(DefaultArgument.class))
                .thenReturn(null);
        } else {
            DefaultArgument desc = mock();

            when(mock.getAnnotation(DefaultArgument.class))
                .thenReturn(desc);
            when(desc.value())
                .thenReturn(name + "-description");
            when(desc.description())
                .thenReturn(description);
        }

        return mock;
    }



    /**
     * Unit test {@link ParameterList#get(String)}
     */
    @Test
    public void test_get_noDescription() {
        VariableElement param1 = mockParam("param1", null, false);
        ExecutableElement element = mock();

        when(element.getParameters())
            .thenAnswer(i -> List.of(param1));

        try (
            MockedStatic<TranslateType> translate = mockStatic(TranslateType.class)
        ) {
            translate.when(() -> TranslateType.toEel(any()))
                .thenReturn(EelType.TEXT);

            ParameterList actual = ParameterList.build(element);

            assertEquals(
                new Parameter("param1", EelType.TEXT, 0, null, false),
                actual.get("param1"),
                "Unexpected parameter");
        }
    }

    /**
     * Unit test {@link ParameterList#get(String)}
     */
    @Test
    public void test_get_emptyDescription() {
        VariableElement param1 = mockParam("param1", "", false);
        ExecutableElement element = mock();

        when(element.getParameters())
            .thenAnswer(i -> List.of(param1));

        try (
            MockedStatic<TranslateType> translate = mockStatic(TranslateType.class)
        ) {
            translate.when(() -> TranslateType.toEel(any()))
                .thenReturn(EelType.TEXT);

            ParameterList actual = ParameterList.build(element);

            assertEquals(
                new Parameter("param1", EelType.TEXT, 0, "param1-description", false),
                actual.get("param1"),
                "Unexpected parameter");
        }
    }

    /**
     * Unit test {@link ParameterList#get(String)}
     */
    @Test
    public void test_get_withDescription() {
        VariableElement param1 = mockParam("param1", "Hello World", false);
        ExecutableElement element = mock();

        when(element.getParameters())
            .thenAnswer(i -> List.of(param1));

        try (
            MockedStatic<TranslateType> translate = mockStatic(TranslateType.class)
        ) {
            translate.when(() -> TranslateType.toEel(any()))
                .thenReturn(EelType.TEXT);

            ParameterList actual = ParameterList.build(element);

            assertEquals(
                new Parameter("param1", EelType.TEXT, 0, "Hello World", false),
                actual.get("param1"),
                "Unexpected parameter");
        }
    }

    /**
     * Unit test {@link ParameterList#get(String)}
     */
    @Test
    public void test_get_varArgs() {
        VariableElement param1 = mockParam("param1", null, true);
        ExecutableElement element = mock();

        when(element.getParameters())
            .thenAnswer(i -> List.of(param1));

        try (
            MockedStatic<TranslateType> translate = mockStatic(TranslateType.class)
        ) {
            translate.when(() -> TranslateType.toEel(any()))
                .thenReturn(EelType.TEXT);

            ParameterList actual = ParameterList.build(element);

            assertEquals(
                new Parameter("param1", EelType.TEXT, 0, null, true),
                actual.get("param1"),
                "Unexpected parameter");
        }
    }


    /**
     * Unit test {@link ParameterList#stream()}
     */
    @Test
    public void test_stream() {
        VariableElement param1 = mockParam("param1", null, false);
        VariableElement param2 = mockParam("param2", "", false);
        VariableElement param3 = mockParam("param3", "foo", true);
        ExecutableElement element = mock();

        when(element.getParameters())
            .thenAnswer(i -> List.of(param1, param2, param3));

        try (
            MockedStatic<TranslateType> translate = mockStatic(TranslateType.class)
        ) {
            translate.when(() -> TranslateType.toEel(any()))
                .thenReturn(EelType.TEXT, EelType.NUMBER, EelType.LOGIC);

            ParameterList list = ParameterList.build(element);
            List<Parameter> actual = list.stream().toList();

            assertEquals(
                List.of(
                    new Parameter("param1", EelType.TEXT, 0, null, false),
                    new Parameter("param2", EelType.NUMBER, 1, "param2-description", false),
                    new Parameter("param3", EelType.LOGIC, 2, "foo", true)
                ),
                actual,
                "Unexpected parameter");
        }
    }
}