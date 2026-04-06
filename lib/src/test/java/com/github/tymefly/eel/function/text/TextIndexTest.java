package com.github.tymefly.eel.function.text;

import com.github.tymefly.eel.Value;
import com.github.tymefly.eel.function.logic.LogicIndex;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Unit Test {@link LogicIndex}
 */
class TextIndexTest {

    /**
     * Unit Test {@link TextIndex#index(String, Value...)}
     */
    @Test
    void test_index() {
        assertEquals(-1, new TextIndex().index("x"), "no values");
        assertEquals(1, new TextIndex().index("x", Value.of("x"), Value.of("y"), Value.of("z")), "3 strings: found");
        assertEquals(-1, new TextIndex().index("a", Value.of("x"), Value.of("y"), Value.of("z")), "3 strings: not found");

        assertEquals(2, new TextIndex().index("2", Value.of(1), Value.of(2), Value.of(3)), "3 numbers: found");
        assertEquals(-1, new TextIndex().index("0", Value.of(1), Value.of(2), Value.of(3)), "3 numbers: not found");

        assertEquals(3, new TextIndex().index("?", Value.of(1), Value.of(true), Value.of("?")), "3 mixed types: found");
        assertEquals(-1, new TextIndex().index("?", Value.of(1), Value.of(true), Value.EPOCH_START_UTC), "3 mixed types: not found");
    }
}