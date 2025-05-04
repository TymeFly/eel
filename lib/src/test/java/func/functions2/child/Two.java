package func.functions2.child;

import java.math.BigDecimal;

import com.github.tymefly.eel.udf.EelFunction;
import com.github.tymefly.eel.udf.PackagedEelFunction;

@PackagedEelFunction
public class Two {
    @EelFunction("test.two")
    public BigDecimal two() {
        return BigDecimal.valueOf(2);
    }
}

