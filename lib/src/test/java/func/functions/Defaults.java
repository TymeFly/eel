package func.functions;

import java.time.ZonedDateTime;

import javax.annotation.Nonnull;

import com.github.tymefly.eel.udf.DefaultArgument;
import com.github.tymefly.eel.udf.EelFunction;
import com.github.tymefly.eel.udf.PackagedEelFunction;

@PackagedEelFunction
public class Defaults {
    @EelFunction(name = "test.defaults")
    public String defaults(@Nonnull String text,
                           @DefaultArgument(of = "987") int number,
                           @DefaultArgument(of = "false") boolean logic,
                           @DefaultArgument(of = "???") @Nonnull String late,
                           @DefaultArgument(of = "2001-02-03T04:05") @Nonnull ZonedDateTime time) {
        return "Passed '" + text + "', " + number + ", " + logic + ", '" + late + "'" + " ~ " + time;
    }
}

