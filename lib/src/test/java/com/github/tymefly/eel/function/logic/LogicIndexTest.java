package com.github.tymefly.eel.function.logic;

import com.github.tymefly.eel.Value;
import org.junit.jupiter.api.Test;

import static java.lang.Boolean.FALSE;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Unit Test {@link LogicIndex}
 */
class LogicIndexTest {
    /**
     * Unit Test {@link LogicIndex#index(Value...)}
     */
    @Test
    void test_textIndex() {
        assertEquals(-1, new LogicIndex().index(), "no values");
        assertEquals(1, new LogicIndex().index(Value.TRUE, Value.FALSE, Value.FALSE), "3 logics: found");
        assertEquals(-1, new LogicIndex().index(Value.FALSE, Value.FALSE, Value.FALSE), "3 logics: not found");

        assertEquals(2, new LogicIndex().index(Value.of(0), Value.of(1), Value.of(2)), "3 numbers: found");
        assertEquals(-1, new LogicIndex().index(Value.of(0), Value.of(-1), Value.of(-1)), "3 numbers: not found");

        assertEquals(3, new LogicIndex().index(Value.FALSE, Value.EPOCH_START_UTC, Value.of(1)), "3 mixed types: found");
        assertEquals(-1, new LogicIndex().index(Value.of(FALSE), Value.of(0), Value.of(-1)), "3 mixed types: not found");
    }
}