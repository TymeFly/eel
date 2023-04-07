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
}