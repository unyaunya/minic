package com.unyaunya.minic.frontend;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class MacroStmt implements Stmt {
    private final String op;    

    public String toString() {
        return String.format("%s()", op);
    }
}
