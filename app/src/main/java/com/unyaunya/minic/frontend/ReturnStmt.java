package com.unyaunya.minic.frontend;

public class ReturnStmt implements Stmt {
    public final Expr value; // may be null for void returns
    public ReturnStmt(Expr value) { this.value = value; }
}