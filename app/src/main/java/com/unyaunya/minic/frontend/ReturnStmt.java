package com.unyaunya.minic.frontend;

import lombok.Value;

@Value
public class ReturnStmt implements Stmt {
    Expr value; // may be null for void returns

    public String toString() {
        return String.format("return %s", value.toString());
    }
}