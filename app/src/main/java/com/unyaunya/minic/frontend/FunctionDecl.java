package com.unyaunya.minic.frontend;

import java.util.List;

import lombok.Value;

@Value
public class FunctionDecl implements Node {
    TypeSpec returnType;
    String name;
    List<Param> params;
    Block body;
}
