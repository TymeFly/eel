package com.github.tymefly.eel.readme;

import com.github.tymefly.eel.FunctionalResource;

public class MyClass2 {
    @com.github.tymefly.eel.udf.EelFunction(name = "my.stateful")
    public String stateful(FunctionalResource functionalResource) {
        DTO myDto = functionalResource.getResource("myName", DTO::new);

       // use DTO
       return (myDto.name + myDto.value);
    }
}