package com.unyaunya.minic.frontend;
import lombok.Getter;

import com.unyaunya.minic.Location;


@Getter
public class PtrDeref extends ExprNode {
    Expr expr;

    public PtrDeref(Location location, Expr expr) {
        super(location);
        this.expr = expr;
    }
}
