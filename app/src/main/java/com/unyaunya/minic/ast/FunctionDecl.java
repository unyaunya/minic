package com.unyaunya.minic.ast;

import java.util.List;

import lombok.Value;

@Value
public class FunctionDecl implements Node {
    TypeSpec returnType;
    String name;
    List<Param> params;
    Block body;

    public String toString() {
        return String.format("%s(%s)", name, String.join(", ", params.stream().map(Object::toString).toList()));
    }
}
