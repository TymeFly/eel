package test.functions2;

import com.github.tymefly.eel.udf.EelFunction;
import com.github.tymefly.eel.udf.PackagedEelFunction;

@PackagedEelFunction
public class Plus2 {
    @EelFunction(name = "test.plus2")
    public int plus2(int value) {
        return (value + 2);
    }
}

