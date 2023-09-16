package func.functions;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.ZonedDateTime;

import javax.annotation.Nonnull;

import com.github.tymefly.eel.EelValue;
import com.github.tymefly.eel.udf.EelFunction;
import com.github.tymefly.eel.udf.PackagedEelFunction;

@PackagedEelFunction
public class TestTypes {
    @EelFunction(name = "types.value")
    public EelValue types(@Nonnull EelValue in) {
        return in;
    }

    @EelFunction(name = "types.str")
    public String types(@Nonnull String in) {
        return in;
    }


    @EelFunction(name = "types.Bool")
    public Boolean types(@Nonnull Boolean in) {
        return in;
    }

    @EelFunction(name = "types.bool")
    public boolean types(boolean in) {
        return in;
    }


    @EelFunction(name = "types.Byte")
    public Byte types(@Nonnull Byte in) {
        return in;
    }

    @EelFunction(name = "types.byte")
    public byte types(byte in) {
        return in;
    }


    @EelFunction(name = "types.Short")
    public Short types(@Nonnull Short in) {
        return in;
    }

    @EelFunction(name = "types.short")
    public short types(short in) {
        return in;
    }


    @EelFunction(name = "types.Int")
    public Integer types(@Nonnull Integer in) {
        return in;
    }

    @EelFunction(name = "types.int")
    public int types(int in) {
        return in;
    }


    @EelFunction(name = "types.Long")
    public Long types(@Nonnull Long in) {
        return in;
    }

    @EelFunction(name = "types.long")
    public long types(long in) {
        return in;
    }


    @EelFunction(name = "types.Float")
    public Float types(@Nonnull Float in) {
        return in;
    }

    @EelFunction(name = "types.float")
    public float types(float in) {
        return in;
    }


    @EelFunction(name = "types.Double")
    public Double types(@Nonnull Double in) {
        return in;
    }

    @EelFunction(name = "types.double")
    public double types(double in) {
        return in;
    }


    @EelFunction(name = "types.BigInt")
    public BigInteger types(@Nonnull BigInteger in) {
        return in;
    }

    @EelFunction(name = "types.BigDec")
    public BigDecimal types(@Nonnull BigDecimal in) {
        return in;
    }

    @EelFunction(name = "types.date")
    public ZonedDateTime types(@Nonnull ZonedDateTime in) {
        return in;
    }

    @EelFunction(name = "types.char")
    public char types(char in) {
        return in;
    }

    @EelFunction(name = "types.Character")
    public Character types(@Nonnull Character in) {
        return in;
    }
}

