package com.unyaunya.minic.ast;

import com.unyaunya.minic.Location;
import lombok.Getter;

@Getter
public class VarDecl extends StmtNode {
    TypeSpec type;
    String name;
    Expr init; // init may be null

    public VarDecl(Location location, TypeSpec type, String name, Expr init) {
        super(location);
        this.type = type;
        this.name = name;
        this.init = init;
    }

    public String toString() {
        if (this.init == null) {
            return String.format("%s %s;", type.toString(), name);
        } else {
            return String.format("%s %s=%s;", type.toString(), name, init.toString());
        }
    }
}