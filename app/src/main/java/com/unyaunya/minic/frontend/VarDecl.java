package com.unyaunya.minic.frontend;

import lombok.Value;

@Value
public class VarDecl implements Stmt {
    TypeSpec type;
    String name;
    Expr init; // init may be null
}