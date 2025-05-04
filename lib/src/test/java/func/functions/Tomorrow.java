package func.functions;

import java.time.ZonedDateTime;

import javax.annotation.Nonnull;

import com.github.tymefly.eel.udf.EelFunction;
import com.github.tymefly.eel.udf.PackagedEelFunction;

@PackagedEelFunction
public class Tomorrow {
    @EelFunction("test.tomorrow")
    public ZonedDateTime tomorrow(@Nonnull ZonedDateTime today) {
        return today.plusDays(1);
    }
}

