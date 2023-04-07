package func.bad_functions;

import com.github.tymefly.eel.udf.EelFunction;
import com.github.tymefly.eel.udf.PackagedEelFunction;

@PackagedEelFunction
public class Test5 {
    @EelFunction(name = "test.5")
    public int test5() {
        throw new UnsupportedOperationException("Can't execute me");
    }
}

