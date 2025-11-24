package com.unyaunya.minic.frontend;

public class VarRef implements Expr {
    public final String name; public VarRef(String name) { this.name = name; }
}