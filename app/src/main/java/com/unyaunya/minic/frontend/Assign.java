package com.unyaunya.minic.frontend;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class Assign implements Stmt {
    private LValue lvalue;
    private Expr expr;
}