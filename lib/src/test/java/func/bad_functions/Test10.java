package func.bad_functions;

import com.github.tymefly.eel.exception.EelFunctionException;
import com.github.tymefly.eel.udf.EelFunction;
import com.github.tymefly.eel.udf.PackagedEelFunction;

@PackagedEelFunction
public class Test10 {
    @EelFunction(name = "test.10")
    public int test10() {
        throw new EelFunctionException("This was thrown in side a Function");
    }
}

