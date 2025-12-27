package com.unyaunya.minic.frontend;
import lombok.Getter;

import com.unyaunya.minic.Location;


@Getter
public class ReturnStmt extends StmtNode {
    Expr value; // may be null for void returns

    public ReturnStmt(Location location, Expr value) {
        super(location);
        this.value = value;
    }

    public String toString() {
        return String.format("return %s", value.toString());
    }
}