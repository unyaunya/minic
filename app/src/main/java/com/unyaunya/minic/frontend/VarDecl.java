package com.unyaunya.minic.frontend;

import lombok.Value;

@Value
public class VarDecl implements Stmt {
    TypeSpec type;
    String name;
    Expr init; // init may be null

    public String toString() {
        if (this.init == null) {
            return String.format("%s %s;", type.toString(), name);
        } else {
            return String.format("%s %s=%s;", type.toString(), name, init.toString());
        }
    }
}