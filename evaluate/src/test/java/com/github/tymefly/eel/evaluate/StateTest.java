package com.github.tymefly.eel.evaluate;

import java.util.HashSet;
import java.util.Set;

import org.junit.Assert;
import org.junit.Test;

/**
 * Unit test for {@link State}
 */
public class StateTest {

    /**
     * Unit test {@link State#getReturnCode()}
     */
    @Test
    public void test_getReturnCode() {
        Set<Integer> codes = new HashSet<>();

        for (var code : State.values()) {
            int returnCode = code.getReturnCode();
            boolean unique = codes.add(returnCode);

            Assert.assertTrue("Duplicate return code " + returnCode, unique);
        }
    }

    /**
     * Unit test {@link State#description()}
     */
    @Test
    public void test_description() {
        Assert.assertEquals("EVALUATED", "Successfully evaluated EEL expression", State.EVALUATED.description());
        Assert.assertEquals("VERSION", "Version information requested", State.VERSION.description());
        Assert.assertEquals("BAD_COMMAND_LINE", "Invalid command line options passed", State.BAD_COMMAND_LINE.description());
    }
}