package com.unyaunya.minic.frontend;

import lombok.Value;

@Value
public class PtrDeref implements Expr {
    Expr expr;
}
