package com.unyaunya.minic.frontend;

import lombok.Value;

@Value
public class Cast implements Expr {
    TypeSpec type;
    Expr expr;
}
