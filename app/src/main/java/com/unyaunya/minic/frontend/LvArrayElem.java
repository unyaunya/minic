package com.unyaunya.minic.frontend;

import lombok.Value;

@Value
public class LvArrayElem implements LValue {
    String name;
    Expr expr;
}
