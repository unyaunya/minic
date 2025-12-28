package com.unyaunya.minic.ast;
import lombok.Getter;

import com.unyaunya.minic.Location;


@Getter
public class Cast extends ExprNode {
    TypeSpec type;
    Expr expr;

    public Cast(Location location, TypeSpec type, Expr expr) {
        super(location);
        this.type = type;
        this.expr = expr;
    }
}
