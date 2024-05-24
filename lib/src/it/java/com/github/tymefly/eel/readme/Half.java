package com.github.tymefly.eel.readme;

public class Half {
    @com.github.tymefly.eel.udf.EelFunction(name = "divide.by2")
    public int half(int value) {
        return value / 2;
    }
}