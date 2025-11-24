package com.unyaunya.minic.frontend;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class FunctionDecl implements Node {
    public final TypeSpec returnType;
    public final String name;
    public final List<Param> params;
    public final Block body;
}
