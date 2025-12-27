package com.unyaunya.minic.frontend;
import lombok.Getter;

import com.unyaunya.minic.Location;


@Getter
public class WhileStmt extends StmtNode {
    Expr cond;
    Block body;

    public WhileStmt(Location location, Expr cond, Block body) {
        super(location);
        this.cond = cond;
        this.body = body;
    }

    public String toString() {
        return String.format("while(%s)", cond.toString());
    }
}