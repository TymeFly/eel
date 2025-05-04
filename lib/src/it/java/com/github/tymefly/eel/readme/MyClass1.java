package com.github.tymefly.eel.readme;

@com.github.tymefly.eel.udf.PackagedEelFunction
public class MyClass1 {
    @com.github.tymefly.eel.udf.EelFunction("my.random")
    public int random(@com.github.tymefly.eel.udf.DefaultArgument("0") int min,
                      @com.github.tymefly.eel.udf.DefaultArgument("99") int max) {
       // implement me
        return max - min;
    }
}
