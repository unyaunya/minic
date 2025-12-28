package com.unyaunya.minic.ast;
import lombok.Getter;

import com.unyaunya.minic.Location;


@Getter
public class MacroStmt extends StmtNode {
    String op;

    public MacroStmt(Location location, String op) {
        super(location);
        this.op = op;
    }

    public String toString() {
        return String.format("%s()", op);
    }
}
