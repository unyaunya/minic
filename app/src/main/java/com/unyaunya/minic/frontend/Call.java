package com.unyaunya.minic.frontend;

import java.util.List;

public class Call implements Expr, Stmt {
    public final String name; public final List<Expr> args;
    public Call(String name, List<Expr> args) { this.name = name; this.args = args; }
}
