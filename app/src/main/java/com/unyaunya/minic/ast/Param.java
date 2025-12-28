package com.unyaunya.minic.ast;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class Param implements Node {
    private final TypeSpec type;
    private final String name;

    public String toString() {
        return String.format("%s %s", type.toString(), name);
    }
}
