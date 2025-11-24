package com.unyaunya.minic.frontend;

import java.util.List;

public class FunctionDecl implements Node {
    public final String name;
    public final List<Param> params;
    public final TypeSpec returnType;
    public final Block body;
    public FunctionDecl(String name, List<Param> params, TypeSpec returnType, Block body) {
        this.name = name; this.params = params; this.returnType = returnType; this.body = body;
    }
}
