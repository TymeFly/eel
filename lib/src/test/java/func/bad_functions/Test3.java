package func.bad_functions;

import com.github.tymefly.eel.udf.PackagedEelFunction;

@PackagedEelFunction
public class Test3 {
    // missing @EelFunction annotation
    public int test3() {
        return -1;
    }
}

