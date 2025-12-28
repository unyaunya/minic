package com.unyaunya.minic.ast;
import lombok.Getter;

import com.unyaunya.minic.Location;


@Getter
public class IfStmt extends StmtNode {
    Expr cond;
    Block thenBlock;
    Block elseBlock;

    public IfStmt(Location location, Expr cond, Block thenBlock, Block elseBlock) {
        super(location);
        this.cond = cond;
        this.thenBlock = thenBlock;
        this.elseBlock = elseBlock;
    }
}
