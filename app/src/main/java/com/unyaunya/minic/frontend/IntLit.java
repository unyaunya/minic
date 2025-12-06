package com.unyaunya.minic.frontend;

import lombok.Value;

@Value
public class IntLit implements Expr {
    int value;

    public String toString() {
        return Integer.toString(value);
    }
}