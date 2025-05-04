package func.functions2;

import java.math.BigDecimal;

import com.github.tymefly.eel.udf.EelFunction;
import com.github.tymefly.eel.udf.PackagedEelFunction;

@PackagedEelFunction
public class One {
    @EelFunction("test.one")
    public BigDecimal two() {
        return BigDecimal.ONE;
    }
}

