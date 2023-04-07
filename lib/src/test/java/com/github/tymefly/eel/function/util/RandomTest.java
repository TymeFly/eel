package com.github.tymefly.eel.function.util;

import org.junit.Assert;
import org.junit.Test;

/**
 * Unit test for {@link Random}
 */
public class RandomTest {

    /**
     * Unit test {@link Random#random(int, int)}
     */
    @Test
    public void test_random_defaults() {
        runTest(0, 99);
    }

    /**
     * Unit test {@link Random#random(int, int)}
     */
    @Test
    public void test_random_subRange() {
        runTest(10, 25);
    }


    private void runTest(int lower, int upper) {
        Random random = new Random();
        long min = Integer.MAX_VALUE;
        long max = Integer.MIN_VALUE;
        int count = 5000;

        while (count-- != 0) {
            long value = random.random(lower, upper);

            min = Math.min(min, value);
            max = Math.max(max, value);
        }

        Assert.assertTrue("Max value returned is out of range: " + max + " > " + upper, (max <= upper));
        Assert.assertTrue("Min value returned is out of range: " + min + " < " + lower, (min >= lower));
    }
}