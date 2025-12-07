package com.unyaunya.minic.frontend;

import lombok.Value;

@Value
public class StringLit implements Expr {
    String value;

    public String toString() {
        return String.format("'%s'", this.value);
    }
}