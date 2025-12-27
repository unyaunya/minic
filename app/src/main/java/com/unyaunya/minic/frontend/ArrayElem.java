package com.unyaunya.minic.frontend;
import lombok.Getter;

import com.unyaunya.minic.Location;


@Getter
public class ArrayElem extends ExprNode {
    String name;
    Expr expr;

    public ArrayElem(Location location, String name, Expr expr) {
        super(location);
        this.name = name;
        this.expr = expr;
    }

    public String toString() {
        return String.format("%s[%s]", name, expr.toString());
    }
}