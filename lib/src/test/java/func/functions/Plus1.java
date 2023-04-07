package func.functions;

import com.github.tymefly.eel.udf.EelFunction;
import com.github.tymefly.eel.udf.PackagedEelFunction;

@PackagedEelFunction
public class Plus1 {
    @EelFunction(name = "test.plus1")
    public Long execute(int value) {                // return a wrapped type
        return (long) (value + 1);
    }
}

