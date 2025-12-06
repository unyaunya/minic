package com.unyaunya.minic.frontend;

import lombok.Value;

@Value
public class Assign implements Stmt {
    LValue lvalue;
    Expr expr;
    public String toString() {
        return String.format("%s=%s", lvalue.toString(), expr.toString());
    }
}