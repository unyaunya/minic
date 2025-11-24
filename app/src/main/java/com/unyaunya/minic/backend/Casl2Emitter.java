
package com.unyaunya.minic.backend;

import com.unyaunya.minic.frontend.*;
import java.util.*;

public class Casl2Emitter {
    private final StringBuilder asm = new StringBuilder();
    private int labelCount = 0;

    public String emit(Program program) {
        emitHeader();
        emitGlobals(program.getGlobals());
        for (FunctionDecl f : program.getFunctions()) {
            emitFunction(f);
        }
        emitFooter();
        return asm.toString();
    }

    private void emitHeader() {
        asm.append("START\n");
    }

    private void emitFooter() {
        asm.append("END\n");
    }

    private void emitGlobals(List<GlobalDecl> globals) {
        for (GlobalDecl g : globals) {
            int size = (g.getArraySize() > 0) ? g.getArraySize() : 1;
            asm.append(g.getName()).append(" DS ").append(size).append("\n");
        }
    }

    private void emitFunction(FunctionDecl f) {
        asm.append(f.getName()).append("\n");
        emitBlock(f.getBody());
        asm.append("RET\n");
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
            asm.append("ST GR1,").append(lv.getName()).append("\n");
        } else {
            asm.append("; TODO: handle array or pointer assignment\n");
        }
    }

    private void emitExpr(Expr e) {
        if (e instanceof IntLit lit) {
            asm.append("LAD GR1,").append(lit.getValue()).append("\n");
        } else if (e instanceof VarRef var) {
            asm.append("LD GR1,").append(var.getName()).append("\n");
        } else if (e instanceof Binary bin) {
            emitExpr(bin.getLeft());
            asm.append("PUSH GR1\n");
            emitExpr(bin.getRight());
            asm.append("POP GR2\n");
            switch (bin.getOp()) {
                case ADD -> asm.append("ADDA GR1,GR2\n");
                case SUB -> asm.append("SUBA GR1,GR2\n");
                case MUL -> asm.append("MULA GR1,GR2\n");
                case DIV -> asm.append("DIVA GR1,GR2\n");
                case LT, GT, LE, GE, EQ, NE -> asm.append("; Comparison handled in control flow\n");
            }
        } else if (e instanceof Call c) {
            emitCall(c);
        }
    }

    private void emitIf(IfStmt i) {
        String elseLabel = newLabel("ELSE");
        String endLabel = newLabel("ENDIF");
        emitExpr(i.getCond());
        asm.append("JZE ").append(elseLabel).append("\n");
        emitBlock(i.getThenBlock());
        asm.append("JUMP ").append(endLabel).append("\n");
        asm.append(elseLabel).append("\n");
        if (i.getElseBlock() != null) emitBlock(i.getElseBlock());
        asm.append(endLabel).append("\n");
    }

    private void emitWhile(WhileStmt w) {
        String startLabel = newLabel("WHILE");
        String endLabel = newLabel("ENDWHILE");
        asm.append(startLabel).append("\n");
        emitExpr(w.getCond());
        asm.append("JZE ").append(endLabel).append("\n");
        emitBlock(w.getBody());
        asm.append("JUMP ").append(startLabel).append("\n");
        asm.append(endLabel).append("\n");
    }

    private void emitFor(ForStmt f) {
        String startLabel = newLabel("FOR");
        String endLabel = newLabel("ENDFOR");
        if (f.getInit() != null) emitStmt(f.getInit());
        asm.append(startLabel).append("\n");
        if (f.getCond() != null) {
            emitExpr(f.getCond());
            asm.append("JZE ").append(endLabel).append("\n");
        }
        emitBlock(f.getBody());
        if (f.getUpdate() != null) emitStmt(f.getUpdate());
        asm.append("JUMP ").append(startLabel).append("\n");
        asm.append(endLabel).append("\n");
    }

    private void emitReturn(ReturnStmt r) {
        if (r.getValue() != null) {
            emitExpr(r.getValue());
            // Return value assumed in GR1
        }
        asm.append("RET\n");
    }

    private void emitCall(Call c) {
        for (Expr arg : c.getArgs()) {
            emitExpr(arg);
            asm.append("PUSH GR1\n");
        }
        asm.append("CALL ").append(c.getName()).append("\n");
        for (int i = 0; i < c.getArgs().size(); i++) {
            asm.append("POP GR2\n");
        }
    }

    private String newLabel(String prefix) {
        return prefix + "_" + (labelCount++);
    }
}
