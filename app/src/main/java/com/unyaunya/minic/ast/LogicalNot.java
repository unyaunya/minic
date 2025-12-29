package com.unyaunya.minic.ast;

import com.unyaunya.minic.Location;
import lombok.Getter;


@Getter
public class LogicalNot extends ExprNode {
    Expr expr;

    public LogicalNot(Location location, Expr expr) {
        super(location);
        this.expr = expr;
    }

    public String toString() {
        return String.format("!%s", expr);
    }
}
