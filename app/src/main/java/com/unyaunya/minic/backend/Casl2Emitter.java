
package com.unyaunya.minic.backend;

import com.unyaunya.minic.frontend.*;
import java.util.*;

public class Casl2Emitter {
    private final Casl2Builder builder = new Casl2Builder();
    private int labelCount = 0;

    public String emit(Program program) {
        builder.start("100").l("MAIN").c("Program start");
        emitGlobals(program.getGlobals());
        builder.lad("GR8", "STACK").c("Initialize stack pointer");
        for (FunctionDecl f : program.getFunctions()) {
            emitFunction(f);
        }
        builder.end().c("Program end");
        builder.ds(256).l("STACK").c("Reserve stack space");
        return builder.build();
    }

    private void emitGlobals(List<GlobalDecl> globals) {
        for (GlobalDecl g : globals) {
            int size = (g.getArraySize() != null) ? g.getArraySize() : 1;
            builder.ds(size).l(g.getName());
        }
    }

    private void emitFunction(FunctionDecl f) {
        builder.comment("Function: " + f.getName());
        builder.l(f.getName());
        builder.rpush().c("Prologue: save registers");
        emitBlock(f.getBody());
        builder.rpop().c("Epilogue: restore registers");
        builder.ret();
    }

    private void emitBlock(Block b) {
        for (Stmt s : b.getStatements()) {
            emitStmt(s);
        }
    }

    private void emitStmt(Stmt s) {
        if (s instanceof Assign a) {
            emitAssign(a);
        } else if (s instanceof ExprStmt e) {
            emitExpr(e.getExpr());
        } else if (s instanceof IfStmt i) {
            emitIf(i);
        } else if (s instanceof WhileStmt w) {
            emitWhile(w);
        } else if (s instanceof ForStmt f) {
            emitFor(f);
        } else if (s instanceof ReturnStmt r) {
            emitReturn(r);
        } else if (s instanceof Block b) {
            emitBlock(b);
        }
    }

    private void emitAssign(Assign a) {
        emitExpr(a.getExpr());
        if (a.getLvalue() instanceof LvVar lv) {
            builder.st("GR1", lv.getName());
        } else if (a.getLvalue() instanceof LvArrayElem lv) {
            emitExpr(lv.getExpr());
            builder.lad("GR2", lv.getName());
            builder.adda("GR2", "GR1");
            builder.st("GR1", "0,GR2");
        } else {
            builder.comment("TODO: handle pointer assignment");
        }
    }

    private void emitExpr(Expr e) {
        if (e instanceof IntLit lit) {
            builder.lad("GR1", String.valueOf(lit.getValue()));
        } else if (e instanceof VarRef var) {
            builder.ld("GR1", var.getName());
        } else if (e instanceof Binary bin) {
            emitExpr(bin.getLeft());
            builder.push("GR1");
            emitExpr(bin.getRight());
            builder.pop("GR2");
            switch (bin.getOp()) {
                case ADD -> builder.adda("GR1", "GR2");
                case SUB -> builder.suba("GR1", "GR2");
                case MUL -> builder.comment("TODO: MULA not implemented");
                case DIV -> builder.comment("TODO: DIVA not implemented");
                case LT, GT, LE, GE, EQ, NE -> emitComparison(bin.getOp());
            }
        } else if (e instanceof Call c) {
            emitCall(c);
        }
    }

    private void emitComparison(Binary.Op op) {
        String trueLabel = newLabel("TRUE");
        String endLabel = newLabel("ENDCMP");
        builder.cpa("GR2", "GR1");
        switch (op) {
            case LT -> builder.jmi(trueLabel);
            case GT -> builder.jpl(trueLabel);
            case LE -> { builder.jmi(trueLabel); builder.jze(trueLabel); }
            case GE -> { builder.jpl(trueLabel); builder.jze(trueLabel); }
            case EQ -> builder.jze(trueLabel);
            case NE -> builder.jnz(trueLabel);
        }
        builder.lad("GR1", "0");
        builder.jump(endLabel);
        builder.comment("True branch").l(trueLabel);
        builder.lad("GR1", "1");
        builder.l(endLabel);
    }

    private void emitIf(IfStmt i) {
        String elseLabel = newLabel("ELSE");
        String endLabel = newLabel("ENDIF");
        emitExpr(i.getCond());
        builder.jze(elseLabel);
        emitBlock(i.getThenBlock());
        builder.jump(endLabel);
        builder.l(elseLabel);
        if (i.getElseBlock() != null) emitBlock(i.getElseBlock());
        builder.l(endLabel);
    }

    private void emitWhile(WhileStmt w) {
        String startLabel = newLabel("WHILE");
        String endLabel = newLabel("ENDWHILE");
        builder.l(startLabel);
        emitExpr(w.getCond());
        builder.jze(endLabel);
        emitBlock(w.getBody());
        builder.jump(startLabel);
        builder.l(endLabel);
    }

    private void emitFor(ForStmt f) {
        String startLabel = newLabel("FOR");
        String endLabel = newLabel("ENDFOR");
        if (f.getInit() != null) emitStmt(f.getInit());
        builder.l(startLabel);
        if (f.getCond() != null) {
            emitExpr(f.getCond());
            builder.jze(endLabel);
        }
        emitBlock(f.getBody());
        if (f.getUpdate() != null) emitStmt(f.getUpdate());
        builder.jump(startLabel);
        builder.l(endLabel);
    }

    private void emitReturn(ReturnStmt r) {
        if (r.getValue() != null) {
            emitExpr(r.getValue());
        }
        builder.ret();
    }

    private void emitCall(Call c) {
        for (Expr arg : c.getArgs()) {
            emitExpr(arg);
            builder.push("GR1");
        }
        builder.call(c.getName());
        for (int i = 0; i < c.getArgs().size(); i++) {
            builder.pop("GR2");
        }
    }

    private String newLabel(String prefix) {
        return prefix + "_" + (labelCount++);
    }
}
