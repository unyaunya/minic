package com.unyaunya.minic.frontend;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class WhileStmt implements Stmt {
    private final Expr cond;
    private final Block body;
}