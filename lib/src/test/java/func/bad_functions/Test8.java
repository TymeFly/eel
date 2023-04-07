package func.bad_functions;

import javax.annotation.Nonnull;

import com.github.tymefly.eel.udf.EelFunction;
import com.github.tymefly.eel.udf.PackagedEelFunction;

@PackagedEelFunction
public class Test8 {
    @EelFunction(name = "test.8")
    public int test8(@Nonnull Thread x) {
        return -1;
    }
}

