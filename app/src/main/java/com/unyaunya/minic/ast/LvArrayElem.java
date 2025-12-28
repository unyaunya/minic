package com.unyaunya.minic.ast;
import lombok.Getter;

import com.unyaunya.minic.Location;


@Getter
public class LvArrayElem extends ExprNode implements LValue {
    String name;
    Expr expr;

    public LvArrayElem(Location location, String name, Expr expr) {
        super(location);
        this.name = name;
        this.expr = expr;
    }

    public String toString() {
        return String.format("%s[%s]", name, expr.toString());
    }
}
