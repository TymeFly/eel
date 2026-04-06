package com.github.tymefly.eel.evaluate;

import java.util.HashSet;
import java.util.Set;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit test for {@link State}
 */
class StateTest {

    /**
     * Unit test {@link State#getReturnCode()}
     */
    @Test
    void test_getReturnCode() {
        Set<Integer> codes = new HashSet<>();

        for (var code : State.values()) {
            int returnCode = code.getReturnCode();
            boolean unique = codes.add(returnCode);

            assertTrue(unique, "Duplicate return code " + returnCode);
        }
    }

    /**
     * Unit test {@link State#description()}
     */
    @Test
    void test_description() {
        assertEquals("Successfully evaluated EEL expression", State.EVALUATED.description(), "EVALUATED");
        assertEquals("Version information requested", State.VERSION.description(), "VERSION");
        assertEquals("Invalid command line options passed", State.BAD_COMMAND_LINE.description(), "BAD_COMMAND_LINE");
    }
}
