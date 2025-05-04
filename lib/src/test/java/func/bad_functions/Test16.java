package func.bad_functions;

import java.io.IOException;

import com.github.tymefly.eel.udf.EelFunction;
import com.github.tymefly.eel.udf.PackagedEelFunction;

@PackagedEelFunction
public class Test16 {
    @EelFunction("test.16")
    public String test16() throws IOException {
        throw new IOException("This message was thrown by test.16");
    }
}

