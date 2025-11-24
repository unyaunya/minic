package com.unyaunya.minic.frontend;

public class VarDecl implements Stmt {
    public final String name; public final TypeSpec type; public final Expr init; // init may be null
    public VarDecl(String name, TypeSpec type, Expr init) { this.name = name; this.type = type; this.init = init; }
}