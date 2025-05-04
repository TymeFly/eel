package func.bad_functions;

import com.github.tymefly.eel.udf.EelFunction;
import com.github.tymefly.eel.udf.PackagedEelFunction;

@PackagedEelFunction
public class Test6 {
    @EelFunction("test.6")
    public Thread test6() {
        return Thread.currentThread();
    }
}

