package com.unyaunya.minic.ast;
import lombok.Getter;

import com.unyaunya.minic.Location;


@Getter
public class ForStmt extends StmtNode {
    Stmt init;   // may be null
    Expr cond;   // may be null
    Stmt update; // may be null
    Block body;

    public ForStmt(Location location, Stmt init, Expr cond, Stmt update, Block body) {
        super(location);
        this.init = init;
        this.cond = cond;
        this.update = update;
        this.body = body;
    }
    
    @Override
    public String toString() {
        return String.format("for (%s; %s; %s)",
                init != null ? init.toString() : "",
                cond != null ? cond.toString() : "",
                update != null ? update.toString() : ""
                );
    }
}