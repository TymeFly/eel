package com.github.tymefly.eel;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

import com.github.tymefly.eel.exception.EelTimeoutException;
import func.Delay;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.SystemErrRule;
import org.junit.contrib.java.lang.system.SystemOutRule;
import org.junit.rules.Stopwatch;


/**
 * Integration Test for expression timeouts
 */
public class TimeoutIntegrationTest {
    @Rule
    public SystemOutRule stdOut = new SystemOutRule().enableLog().muteForSuccessfulTests();

    @Rule
    public SystemErrRule stdErr = new SystemErrRule().enableLog().muteForSuccessfulTests();

    @Rule
    public Stopwatch stopwatch = new Stopwatch();


    private static final long DURATION_TOLERANCE = 500;


    /**
     * Integration test {@link Eel}. 'delay' drives the CPU constantly for the duration - thread can't be interrupted
     */
    @Test
    public void test_delay() {
        EelTimeoutException actual = Assert.assertThrows(EelTimeoutException.class, () -> Eel.factory()
            .withUdfClass(Delay.class)
            .withTimeout(Duration.ofSeconds(5))
            .compile("$( test.delay( 30 ) )")
            .evaluate()
            .asText());

        long duration = stopwatch.runtime(TimeUnit.MILLISECONDS);

        Assert.assertEquals("Unexpected message", "EEL Timeout after 5 second(s)", actual.getMessage());
        Assert.assertTrue("Unexpected duration of " + duration + "ms", validateDuration(duration, 5_000));
    }

    /**
     * Integration test {@link Eel}. 'sleep' calls {@link Thread#sleep(long)} and can be interrupted
     */
    @Test
    public void test_sleep_expectTimeout() {
        EelTimeoutException actual = Assert.assertThrows(EelTimeoutException.class, () -> Eel.factory()
            .withUdfClass(Delay.class)
            .withTimeout(Duration.ofSeconds(5))
            .compile("$( test.sleep( 30 ) )")
            .evaluate()
            .asText());

        long duration = stopwatch.runtime(TimeUnit.MILLISECONDS);

        Assert.assertEquals("Unexpected message", "EEL Timeout after 5 second(s)", actual.getMessage());
        Assert.assertTrue("Unexpected duration of " + duration + "ms", validateDuration(duration, 5_000));
    }

    /**
     * Integration test {@link Eel}
     */
    @Test
    public void test_sleep_shouldNotTimeOut() {
        String actual = Eel.factory()
            .withUdfClass(Delay.class)
            .withTimeout(Duration.ofSeconds(15))
            .compile("$( test.sleep( 3 ) )")
            .evaluate()
            .asText();

        long duration = stopwatch.runtime(TimeUnit.MILLISECONDS);

        Assert.assertEquals("Unexpected message", "Slept for 3 seconds", actual);
        Assert.assertTrue("Unexpected duration of " + duration + "ms", validateDuration(duration, 3_000));
    }


    /**
     * Integration test {@link Eel}
     */
    @Test
    public void test_sleep_disableTimeout() {
        String actual = Eel.factory()
            .withUdfClass(Delay.class)
            .withTimeout(Duration.ofSeconds(0))
            .compile("$( test.sleep( 3 ) )")
            .evaluate()
            .asText();

        long duration = stopwatch.runtime(TimeUnit.MILLISECONDS);

        Assert.assertEquals("Unexpected message", "Slept for 3 seconds", actual);
        Assert.assertTrue("Unexpected duration of " + duration + "ms", validateDuration(duration, 3_000));
    }


    /**
     * Returns {@literal true} only if the actual duration is +/- DURATION_TOLERANCE of the expected duration
     * @param actual        actual duration in mS
     * @param expected      expected duration im mS
     * @return {@literal true} only if the actual duration is +/- DURATION_TOLERANCE of the expected duration
     */
    private boolean validateDuration(long actual, long expected) {
        boolean valid = (actual >= (expected - DURATION_TOLERANCE));
        valid = valid && (actual <= (expected + DURATION_TOLERANCE));

        return valid;
    }
}
