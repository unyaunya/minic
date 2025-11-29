package com.unyaunya.minic.frontend;

import lombok.Value;

@Value
public class VarRef implements Expr {
    String name;
    public String toString() {
        return name;
    }
}