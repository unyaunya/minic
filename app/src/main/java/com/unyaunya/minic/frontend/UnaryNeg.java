package com.unyaunya.minic.frontend;

import lombok.Value;

@Value
public class UnaryNeg implements Expr {
    Expr expr;
}