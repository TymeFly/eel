package func;


import javax.annotation.Nonnull;

import com.github.tymefly.eel.Value;
import com.github.tymefly.eel.udf.EelFunction;
import com.github.tymefly.eel.udf.PackagedEelFunction;

@PackagedEelFunction
public class Echo {
    @EelFunction("test.echo")
    @Nonnull
    public Value echo(@Nonnull Value value) {
        return value;
    }
}
