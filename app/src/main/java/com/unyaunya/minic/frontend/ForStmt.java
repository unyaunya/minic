package com.unyaunya.minic.frontend;

import lombok.Value;

@Value
public class ForStmt implements Stmt {
    Stmt init;   // may be null
    Expr cond;   // may be null
    Stmt update; // may be null
    Block body;
}