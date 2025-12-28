package com.unyaunya.minic.ast;
import lombok.Getter;

import com.unyaunya.minic.Location;


@Getter
public class ExprStmt extends StmtNode {
    Expr expr;

    public ExprStmt(Location location, Expr expr) {
        super(location);
        this.expr = expr;
    }

    public String toString() {
        return String.format("%s", expr.toString());
    }
}
