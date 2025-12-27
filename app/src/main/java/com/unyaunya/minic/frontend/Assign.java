package com.unyaunya.minic.frontend;
import lombok.Getter;

import com.unyaunya.minic.Location;


@Getter
public class Assign extends StmtNode {
    LValue lvalue;
    Expr expr;

    public Assign(Location location, LValue lvalue, Expr expr) {
        super(location);
        this.lvalue = lvalue;
        this.expr = expr;
    }

    public String toString() {
        return String.format("%s=%s", lvalue.toString(), expr.toString());
    }
}