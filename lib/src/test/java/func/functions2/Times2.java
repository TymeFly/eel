package func.functions2;

import java.math.BigDecimal;

import javax.annotation.Nonnull;

import com.github.tymefly.eel.udf.EelFunction;

//@PackagedEelFunction is not required
public class Times2 {
    @EelFunction(name = "test.double")
    public BigDecimal execute(@Nonnull BigDecimal value) {
        return value.multiply(BigDecimal.valueOf(2));
    }
}

