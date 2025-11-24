package com.unyaunya.minic.frontend;

import lombok.Value;

@Value
public class Assign implements Stmt {
    LValue lvalue;
    Expr expr;
}