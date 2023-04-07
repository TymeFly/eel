package func.functions2;

import java.math.BigDecimal;
import java.math.RoundingMode;

import javax.annotation.Nonnull;

import com.github.tymefly.eel.udf.EelFunction;
import com.github.tymefly.eel.udf.PackagedEelFunction;

@PackagedEelFunction
public class Half {
    @EelFunction(name = "test.half")
    public BigDecimal half(@Nonnull BigDecimal value) {
        return value.divide(BigDecimal.valueOf(2), RoundingMode.HALF_UP);
    }
}

