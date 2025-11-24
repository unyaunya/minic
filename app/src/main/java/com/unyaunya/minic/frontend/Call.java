package com.unyaunya.minic.frontend;

import java.util.List;

import lombok.Value;

@Value
public class Call implements Expr, Stmt {
    String name;
    List<Expr> args;
}
