package com.unyaunya.minic.frontend;
import lombok.Getter;

import com.unyaunya.minic.Location;


@Getter
public class UnaryNeg extends ExprNode {
    Expr expr;

    public UnaryNeg(Location location, Expr expr) {
        super(location);
        this.expr = expr;
    }
}