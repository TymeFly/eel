package func.bad_functions;

import com.github.tymefly.eel.udf.EelFunction;
import com.github.tymefly.eel.udf.PackagedEelFunction;

@PackagedEelFunction
public class Test2 {
    public Test2() {
        throw new UnsupportedOperationException("can't construct me");
    }


    @EelFunction("test.2")
    public int test2() {
        return -1;
    }
}

