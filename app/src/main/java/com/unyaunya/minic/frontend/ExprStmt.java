package com.unyaunya.minic.frontend;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class ExprStmt implements Stmt {
    private final Expr expr;    

    public String toString() {
        return String.format("%s", expr.toString());
    }
}
