package com.github.tymefly.eel;

import java.time.Duration;
import java.time.Instant;

import com.github.tymefly.eel.exception.EelTimeoutException;
import func.Delay;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import uk.org.webcompere.systemstubs.jupiter.SystemStub;
import uk.org.webcompere.systemstubs.jupiter.SystemStubsExtension;
import uk.org.webcompere.systemstubs.stream.SystemErr;
import uk.org.webcompere.systemstubs.stream.SystemOut;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;


/**
 * Integration Test for expression timeouts
 */
@ExtendWith(SystemStubsExtension.class)
public class TimeoutIntegrationTest {
    @SystemStub
    private SystemOut stdOut;

    @SystemStub
    private SystemErr stdErr;


    private static final long DURATION_TOLERANCE = 500;


    /**
     * Integration test {@link Eel}. 'delay' drives the CPU constantly for the duration - thread can't be interrupted
     */
    @Test
    public void test_delay() {
        Instant start = Instant.now();
        EelTimeoutException actual = assertThrows(EelTimeoutException.class, () -> Eel.factory()
            .withUdfClass(Delay.class)
            .withTimeout(Duration.ofSeconds(5))
            .compile("$( test.delay( 30 ) )")
            .evaluate()
            .asText());

        long duration = Duration.between(start, Instant.now()).toMillis();

        assertEquals("EEL Timeout after 5 second(s)", actual.getMessage(), "Unexpected message");
        assertTrue(validateDuration(duration, 5_000), "Unexpected duration of " + duration + "ms");
    }

    /**
     * Integration test {@link Eel}. 'sleep' calls {@link Thread#sleep(long)} and can be interrupted
     */
    @Test
    public void test_sleep_expectTimeout() {
        Instant start = Instant.now();
        EelTimeoutException actual = assertThrows(EelTimeoutException.class, () -> Eel.factory()
            .withUdfClass(Delay.class)
            .withTimeout(Duration.ofSeconds(5))
            .compile("$( test.sleep( 30 ) )")
            .evaluate()
            .asText());

        long duration = Duration.between(start, Instant.now()).toMillis();

        assertEquals("EEL Timeout after 5 second(s)", actual.getMessage(), "Unexpected message");
        assertTrue(validateDuration(duration, 5_000), "Unexpected duration of " + duration + "ms");
    }

    /**
     * Integration test {@link Eel}
     */
    @Test
    public void test_sleep_shouldNotTimeOut() {
        Instant start = Instant.now();
        String actual = Eel.factory()
            .withUdfClass(Delay.class)
            .withTimeout(Duration.ofSeconds(15))
            .compile("$( test.sleep( 3 ) )")
            .evaluate()
            .asText();

        long duration = Duration.between(start, Instant.now()).toMillis();

        assertEquals("Slept for 3 seconds", actual, "Unexpected message");
        assertTrue(validateDuration(duration, 3_000), "Unexpected duration of " + duration + "ms");
    }


    /**
     * Integration test {@link Eel}
     */
    @Test
    public void test_sleep_disableTimeout() {
        Instant start = Instant.now();
        String actual = Eel.factory()
            .withUdfClass(Delay.class)
            .withTimeout(Duration.ofSeconds(0))
            .compile("$( test.sleep( 3 ) )")
            .evaluate()
            .asText();

        long duration = Duration.between(start, Instant.now()).toMillis();

        assertEquals("Slept for 3 seconds", actual, "Unexpected message");
        assertTrue(validateDuration(duration, 3_000), "Unexpected duration of " + duration + "ms");
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
