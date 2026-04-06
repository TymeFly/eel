package com.github.tymefly.eel.doc.model;

import java.util.List;
import java.util.Optional;

import javax.annotation.Nonnull;

import com.github.tymefly.eel.doc.source.Parameter;
import com.github.tymefly.eel.doc.utils.EelType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;

/**
 * Unit test for {@link Function}
 */
public class FunctionTest {

    /**
     * Unit test {@link Function#name()}, {@link Function#group()}, {@link Function#type()}
     */
    @Test
    public void test_parameters_withType() {
        GroupModel group = mock(GroupModel.class);
        Function function = new Function("myFunction", group, EelType.LOGIC);

        assertEquals("myFunction", function.name(), "Unexpected name");
        assertSame(group, function.group(), "Unexpected group");
        assertEquals(Optional.of(EelType.LOGIC), function.type(), "Unexpected type");
    }

    /**
     * Unit test {@link Function#name()}, {@link Function#group()}, {@link Function#type()}
     */
    @Test
    public void test_parameters_noType() {
        GroupModel group = mock(GroupModel.class);
        Function function = new Function("anotherFunction", group, null);

        assertEquals("anotherFunction", function.name(), "Unexpected name");
        assertSame(group, function.group(), "Unexpected group");
        assertEquals(Optional.empty(), function.type(), "Unexpected type");
    }

    /**
     * Unit test {@link Function#toString()}
     */
    @Test
    public void test_toString() {
        GroupModel group = mock(GroupModel.class);
        Function function = new Function("myFunction", group, EelType.LOGIC);

        assertEquals("FunctionModel{name='myFunction'}", function.toString(), "Unexpected name");
    }

    /**
     * Unit test {@link Function#uniqueId()}
     */
    @Test
    public void test_uniqueId() {
        GroupModel group = mock(GroupModel.class);
        Function function1 = new Function("myFunction", group, EelType.LOGIC);
        Function function2 = new Function("myFunction", group, EelType.LOGIC);
        Function function3 = new Function("myFunction", group, EelType.LOGIC);

        assertNotEquals(function1.uniqueId(), function2.uniqueId(), "function1 and function2");
        assertNotEquals(function1.uniqueId(), function3.uniqueId(), "function1 and function3");
        assertNotEquals(function2.uniqueId(), function3.uniqueId(), "function2 and function3");
    }

    /**
     * Unit test {@link Function#eelSignature()}
     */
    @Test
    public void test_eelSignature() {
        GroupModel group = mock(GroupModel.class);
        Function function = new Function("myFunction", group, EelType.LOGIC);
        Parameter arg1 = new Parameter("arg1", EelType.TEXT, 0, null, false);
        Parameter arg3 = new Parameter("arg3", null, 2, null, false);
        Parameter arg4 = new Parameter("arg4", EelType.NUMBER, 3, "some default", false);
        Parameter arg5 = new Parameter("arg5", EelType.LOGIC, 5, "another default", true);

        assertEquals("myFunction()", function.eelSignature(), "empty parameter list");

        function.addParameter("arg1", arg1);
        assertEquals("myFunction(arg1)", function.eelSignature(), "With normal argument");

        function.addParameter("arg2", null);
        assertEquals("myFunction(arg1)", function.eelSignature(), "With null argument");

        function.addParameter("arg3", arg3);
        assertEquals("myFunction(arg1)", function.eelSignature(), "Not an EEL type");

        function.addParameter("arg4", arg4);
        assertEquals("myFunction(arg1, arg4)", function.eelSignature(), "with default");

        function.addParameter("arg5", arg5);
        assertEquals("myFunction(arg1, arg4, arg5...)", function.eelSignature(), "with varArgs");
    }

    /**
     * Unit test {@link Function#parameters()}
     */
    @Test
    public void test_addParameter() {
        GroupModel group = mock(GroupModel.class);
        Function function = new Function("myFunction", group, EelType.LOGIC);
        Parameter arg1 = new Parameter("arg1", EelType.TEXT, 0, null, false);
        Parameter arg3 = new Parameter("arg3", null, 2, null, false);
        Parameter arg4 = new Parameter("arg4", EelType.NUMBER, 3, "some default", false);
        Parameter arg5 = new Parameter("arg5", EelType.LOGIC, 5, "another default", true);

        assertParameters("empty", function);

        function.addParameter("arg1", arg1);
        assertParameters("normal param", function, arg1);

        function.addParameter("arg2", null);
        assertParameters("null param", function, arg1);

        function.addParameter("arg3", arg3);
        assertParameters("Not an EEL type", function, arg1);

        function.addParameter("arg4", arg4);
        assertParameters("with default", function, arg1, arg4);

        function.addParameter("arg5", arg5);
        assertParameters("with varArgs", function, arg1, arg4, arg5);
    }

    private void assertParameters(@Nonnull String message, @Nonnull Function function, Parameter... expected) {
        List<ParamModel> actual = function.parameters();
        int index = -1;

        assertEquals(expected.length, actual.size(), "Unexpected number of parameters");

        for (var param : actual) {
            Parameter test = expected[++index];

            assertEquals(test.name(), param.identifier(), message + " Param" + index + " name:");
            assertEquals(test.index(), param.order(), message + " Param" + index + " name:");
        }
    }
}
