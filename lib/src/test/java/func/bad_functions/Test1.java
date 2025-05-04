package func.bad_functions;

import com.github.tymefly.eel.udf.EelFunction;
import com.github.tymefly.eel.udf.PackagedEelFunction;

@PackagedEelFunction
public class Test1 {
    public Test1(int x) {           // No 0 argument constructor
    }

    @EelFunction("test.1")
    public int test1() {
        return -1;
    }
}
