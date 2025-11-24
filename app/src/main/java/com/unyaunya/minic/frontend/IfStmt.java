package com.unyaunya.minic.frontend;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class IfStmt implements Stmt {
    private final Expr cond;
    private final Block thenBlock;
    private final Block elseBlock;
}
