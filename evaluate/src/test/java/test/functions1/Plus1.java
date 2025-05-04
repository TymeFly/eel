package test.functions1;

import com.github.tymefly.eel.udf.EelFunction;

public class Plus1 {
    @EelFunction("test.plus1")
    public int plus1(int value) {
        return (value + 1);
    }
}

