package com.github.tymefly.eel.function.util;

import org.junit.Assert;
import org.junit.Test;

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

        Assert.assertTrue("UUID has unexpected format: " + actual,
            actual.matches("[a-f0-9]{8}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{12}"));
    }
}