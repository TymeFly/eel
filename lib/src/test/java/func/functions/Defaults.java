package func.functions;

import java.time.ZonedDateTime;

import javax.annotation.Nonnull;

import com.github.tymefly.eel.Value;
import com.github.tymefly.eel.udf.DefaultArgument;
import com.github.tymefly.eel.udf.EelFunction;
import com.github.tymefly.eel.udf.PackagedEelFunction;

@PackagedEelFunction
public class Defaults {
    @EelFunction("test.defaults")
    public String defaults(@Nonnull String text,
                           @DefaultArgument("987") int number,
                           @DefaultArgument("false") boolean logic,
                           @DefaultArgument("???") @Nonnull String late,
                           @DefaultArgument("2001-02-03T04:05") @Nonnull ZonedDateTime time,
                           @DefaultArgument("???") @Nonnull Value lambda) {
        return "Passed '" + text + "', " + number + ", " + logic + ", '" + late + "'" + " ~ " + time + ", " + lambda.asText();
    }
}

