package com.unyaunya.minic.ast;
import lombok.Getter;

import com.unyaunya.minic.Location;


@Getter
public class LvPtrDeref extends ExprNode implements LValue {
    Expr expr;

    public LvPtrDeref(Location location, Expr expr) {
        super(location);
        this.expr = expr;
    }
}
