package com.unyaunya.minic.frontend;

import lombok.Value;

@Value
public class LvArrayElem implements LValue {
    String name;
    Expr expr;

    public String toString() {
        return String.format("%s[%s]", name, expr.toString());
    }
}
