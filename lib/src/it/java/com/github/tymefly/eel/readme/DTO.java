package com.github.tymefly.eel.readme;

public class DTO {
    static int count = 0;

    int value = (++count);
    String name;

    public DTO(String name) {
        this.name = name;
    }
}