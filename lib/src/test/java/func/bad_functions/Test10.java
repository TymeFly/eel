package func.bad_functions;

import com.github.tymefly.eel.udf.EelFunction;
import com.github.tymefly.eel.udf.PackagedEelFunction;

@PackagedEelFunction
public class Test10 {
    @EelFunction("test.10")
    public int test10() {
        throw new RuntimeException("This was thrown inside a Function");
    }
}

