package func.bad_functions;

import com.github.tymefly.eel.udf.EelFunction;
import com.github.tymefly.eel.udf.PackagedEelFunction;

@PackagedEelFunction
public class Test13 {
    @EelFunction("123.badName")
    public String test13() {
        return null;
    }
}

