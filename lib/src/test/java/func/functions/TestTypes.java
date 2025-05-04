package func.functions;

import java.io.File;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.ZonedDateTime;

import javax.annotation.Nonnull;

import com.github.tymefly.eel.EelContext;
import com.github.tymefly.eel.Value;
import com.github.tymefly.eel.udf.EelFunction;
import com.github.tymefly.eel.udf.FunctionalResource;
import com.github.tymefly.eel.udf.PackagedEelFunction;

@PackagedEelFunction
public class TestTypes {
    @EelFunction("types.functionalResource")
    public String types(@Nonnull FunctionalResource in) {
        return in.getResource("resourceName", n -> "resourceValue!!");
    }

    @EelFunction("types.context")
    public String types(@Nonnull EelContext in) {
        return in.contextId();
    }

    @EelFunction("types.value")
    public Value types(@Nonnull Value in) {
        return in;
    }

    @EelFunction("types.str")
    public String types(@Nonnull String in) {
        return in;
    }


    @EelFunction("types.Bool")
    public Boolean types(@Nonnull Boolean in) {
        return in;
    }

    @EelFunction("types.bool")
    public boolean types(boolean in) {
        return in;
    }


    @EelFunction("types.Byte")
    public Byte types(@Nonnull Byte in) {
        return in;
    }

    @EelFunction("types.byte")
    public byte types(byte in) {
        return in;
    }


    @EelFunction("types.Short")
    public Short types(@Nonnull Short in) {
        return in;
    }

    @EelFunction("types.short")
    public short types(short in) {
        return in;
    }


    @EelFunction("types.Int")
    public Integer types(@Nonnull Integer in) {
        return in;
    }

    @EelFunction("types.int")
    public int types(int in) {
        return in;
    }


    @EelFunction("types.Long")
    public Long types(@Nonnull Long in) {
        return in;
    }

    @EelFunction("types.long")
    public long types(long in) {
        return in;
    }


    @EelFunction("types.Float")
    public Float types(@Nonnull Float in) {
        return in;
    }

    @EelFunction("types.float")
    public float types(float in) {
        return in;
    }


    @EelFunction("types.Double")
    public Double types(@Nonnull Double in) {
        return in;
    }

    @EelFunction("types.double")
    public double types(double in) {
        return in;
    }


    @EelFunction("types.BigInt")
    public BigInteger types(@Nonnull BigInteger in) {
        return in;
    }

    @EelFunction("types.BigDec")
    public BigDecimal types(@Nonnull BigDecimal in) {
        return in;
    }

    @EelFunction("types.date")
    public ZonedDateTime types(@Nonnull ZonedDateTime in) {
        return in;
    }

    @EelFunction("types.char")
    public char types(char in) {
        return in;
    }

    @EelFunction("types.Character")
    public Character types(@Nonnull Character in) {
        return in;
    }

    @EelFunction("types.File")
    public String types(@Nonnull File in) {
        return in.getPath();
    }
}

