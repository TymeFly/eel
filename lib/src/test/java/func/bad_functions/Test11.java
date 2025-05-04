package func.bad_functions;

import com.github.tymefly.eel.udf.EelFunction;
import com.github.tymefly.eel.udf.PackagedEelFunction;

@PackagedEelFunction
public class Test11 {
    @EelFunction("noPrefix")
    public String test11() {
        return null;
    }
}

