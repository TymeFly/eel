package func;

import java.time.Instant;

import com.github.tymefly.eel.udf.EelFunction;
import com.github.tymefly.eel.udf.PackagedEelFunction;

@PackagedEelFunction
public class Delay {
    @EelFunction("test.sleep")
    public String sleep(long value) throws Exception {
        Thread.sleep(value * 1000);

        return "Slept for " + value + " seconds";
    }


    @EelFunction("test.delay")
    public String delay(long value) {
        Instant end = Instant.now().plusSeconds(value);
        Instant now;

        do {
            now = Instant.now();
        } while (now.isBefore(end));

        return "Delayed for " + value + " seconds";
    }
}

