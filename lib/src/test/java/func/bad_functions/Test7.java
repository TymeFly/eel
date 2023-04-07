package func.bad_functions;

import com.github.tymefly.eel.udf.EelFunction;
import com.github.tymefly.eel.udf.PackagedEelFunction;

@PackagedEelFunction
public class Test7 {
    @EelFunction(name = "test.7")
    int test7() {         // not public
        return -1;
    }
}

