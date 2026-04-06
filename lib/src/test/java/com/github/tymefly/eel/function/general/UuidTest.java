package com.github.tymefly.eel.function.general;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit test for {@link Uuid}
 */
public class UuidTest {

    /**
     * Unit test {@link Uuid#uuid()}
     */
    @Test
    public void test_uuid() {
        String actual = new Uuid().uuid();

        assertTrue(actual.matches("[a-f0-9]{8}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{12}"),
            "UUID has unexpected format: " + actual);
    }
}