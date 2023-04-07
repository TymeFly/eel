package func.functions;

import com.github.tymefly.eel.udf.EelFunction;
import com.github.tymefly.eel.udf.PackagedEelFunction;

@PackagedEelFunction
public class Sum {
    @EelFunction(name = "test.sum")
    public long sum(int first, int... rest) {
        long result = first;

        for (var x : rest) {
            result += x;
        }

        return result;
    }
}

