package func.bad_functions;

import javax.annotation.Nonnull;

import com.github.tymefly.eel.udf.EelFunction;
import com.github.tymefly.eel.udf.PackagedEelFunction;

@PackagedEelFunction
public class Test4 {
    @EelFunction("test.4_1")
    @Nonnull
    public String Test4_1() {
        return "execute1";
    }

    @EelFunction("test.4_2")
    @Nonnull
    public String Test4_2() {
        return "execute2";
    }
}

