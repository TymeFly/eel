package com.github.tymefly.eel.doc.model;

import java.util.List;
import java.util.Optional;

import javax.annotation.Nonnull;

import com.github.tymefly.eel.doc.source.Parameter;
import com.github.tymefly.eel.doc.utils.EelType;
import org.junit.Assert;
import org.junit.Test;

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
        GroupModel group = mock();
        Function function = new Function("myFunction", group, EelType.LOGIC);

        Assert.assertEquals("Unexpected name", "myFunction", function.name());
        Assert.assertSame("Unexpected group", group, function.group());
        Assert.assertEquals("Unexpected type", Optional.of(EelType.LOGIC), function.type());
    }


    /**
     * Unit test {@link Function#name()}, {@link Function#group()}, {@link Function#type()}
     */
    @Test
    public void test_parameters_noType() {
        GroupModel group = mock();
        Function function = new Function("anotherFunction", group, null);

        Assert.assertEquals("Unexpected name", "anotherFunction", function.name());
        Assert.assertSame("Unexpected group", group, function.group());
        Assert.assertEquals("Unexpected type", Optional.empty(), function.type());
    }


    /**
     * Unit test {@link Function#toString()}
     */
    @Test
    public void test_toString() {
        GroupModel group = mock();
        Function function = new Function("myFunction", group, EelType.LOGIC);

        Assert.assertEquals("Unexpected name", "FunctionModel{name='myFunction'}", function.toString());
    }


    /**
     * Unit test {@link Function#uniqueId()}
     */
    @Test
    public void test_uniqueId() {
        GroupModel group = mock();
        Function function1 = new Function("myFunction", group, EelType.LOGIC);
        Function function2 = new Function("myFunction", group, EelType.LOGIC);
        Function function3 = new Function("myFunction", group, EelType.LOGIC);

        Assert.assertNotEquals("function1 and function2", function1.uniqueId(), function2.uniqueId());
        Assert.assertNotEquals("function1 and function3", function1.uniqueId(), function3.uniqueId());
        Assert.assertNotEquals("function2 and function3", function2.uniqueId(), function3.uniqueId());
    }


    /**
     * Unit test {@link Function#eelSignature()}
     */
    @Test
    public void test_eelSignature() {
        GroupModel group = mock();
        Function function = new Function("myFunction", group, EelType.LOGIC);
        Parameter arg1 = new Parameter("arg1", EelType.TEXT, 0, null, false);
        Parameter arg3 = new Parameter("arg3", null, 2, null, false);
        Parameter arg4 = new Parameter("arg4", EelType.NUMBER, 3, "some default", false);
        Parameter arg5 = new Parameter("arg5", EelType.LOGIC, 5, "another default", true);

        Assert.assertEquals("empty parameter list", "myFunction()", function.eelSignature());

        function.addParameter("arg1", arg1);
        Assert.assertEquals("With normal argument", "myFunction(arg1)", function.eelSignature());

        function.addParameter("arg2", null);
        Assert.assertEquals("With null argument", "myFunction(arg1)", function.eelSignature());

        function.addParameter("arg3", arg3);
        Assert.assertEquals("Not an EEL type", "myFunction(arg1)", function.eelSignature());

        function.addParameter("arg4", arg4);
        Assert.assertEquals("with default", "myFunction(arg1, arg4)", function.eelSignature());

        function.addParameter("arg5", arg5);
        Assert.assertEquals("with varArgs", "myFunction(arg1, arg4, arg5...)", function.eelSignature());
    }


    /**
     * Unit test {@link Function#parameters()}
     */
    @Test
    public void test_addParameter() {
        GroupModel group = mock();
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

        Assert.assertEquals("Unexpected number of parameters", expected.length, actual.size());

        for (var param : actual) {
            Parameter test = expected[++index];

            Assert.assertEquals(message + " Param" + index + " name:", test.name(), param.identifier());
            Assert.assertEquals(message + " Param" + index + " name:", test.index(), param.order());
        }
    }
}