package func.functions;

import java.math.BigDecimal;

import com.github.tymefly.eel.udf.EelFunction;
import com.github.tymefly.eel.udf.PackagedEelFunction;

@PackagedEelFunction
public class SameValue {
    @EelFunction(name = "test.sameValue")
    public boolean sameValue(byte b, short s, int i, long l, BigDecimal big) {
        boolean sameValue = (b == s);

        sameValue &= (s == i);
        sameValue &= (i == l);
        sameValue &= (l == big.longValue());

        return sameValue;
    }
}

