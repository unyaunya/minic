package com.unyaunya.minic.frontend;

import lombok.Value;

@Value
public class LvPtrDeref implements LValue {
    Expr expr;
}
