// backend/Casl2Emitter.java
package com.unyaunya.minic.backend;

import com.unyaunya.minic.frontend.*;
import java.util.*;

public class Casl2Emitter {
    private final StringBuilder out = new StringBuilder();
    private int labelSeq = 0;

    public String emit(Program p) {
        out.append("START\n");
        // emit globals if you have any; here we inline locals per function
        for (FunctionDecl f : p.functions) emitFunction(f);
        out.append("END\n");
        return out.toString();
    }

    private void emitFunction(FunctionDecl f) {
        String entry = "F_" + f.name;
        out.append(entry).append("\n");
        // simple prologue (assuming GR registers; adapt as needed)
        // save return address, frame, etc. — keep minimal initially
        Map<String, Integer> locals = new HashMap<>();
        int sp = 0;

        for (Stmt s : f.body) emitStmt(s, locals);

        // ensure function ends (for void)
        out.append("RET\n");
    }

    private void emitStmt(Stmt s, Map<String,Integer> locals) {
        if (s instanceof VarDecl vd) {
            locals.put(vd.name, locals.size()); // assign a slot
            if (vd.init != null) {
                emitExpr(vd.init, "GR1");
                out.append("ST GR1, VAR_").append(vd.name).append("\n");
            } else {
                out.append("LAD GR1, 0\n");
                out.append("ST GR1, VAR_").append(vd.name).append("\n");
            }
            return;
        }
        if (s instanceof Assign a) {
            emitExpr(a.value, "GR1");
            out.append("ST GR1, VAR_").append(a.name).append("\n");
            return;
        }
        if (s instanceof ReturnStmt r) {
            if (r.value != null) {
                emitExpr(r.value, "GR1");
                out.append("RET\n");
            } else {
                out.append("RET\n");
            }
            return;
        }
        if (s instanceof IfStmt i) {
            String lElse = newLabel("ELSE");
            String lEnd  = newLabel("ENDIF");
            emitExpr(i.cond, "GR1");
            out.append("CPA GR1, =0\n");
            out.append("JZE ").append(lElse).append("\n");
            for (Stmt t : i.thenBlock) emitStmt(t, locals);
            out.append("JMP ").append(lEnd).append("\n");
            out.append(lElse).append("\n");
            for (Stmt e : i.elseBlock) emitStmt(e, locals);
            out.append(lEnd).append("\n");
            return;
        }
        if (s instanceof WhileStmt w) {
            String lHead = newLabel("WHILE");
            String lEnd  = newLabel("WEND");
            out.append(lHead).append("\n");
            emitExpr(w.cond, "GR1");
            out.append("CPA GR1, =0\n");
            out.append("JZE ").append(lEnd).append("\n");
            for (Stmt b : w.body) emitStmt(b, locals);
            out.append("JMP ").append(lHead).append("\n");
            out.append(lEnd).append("\n");
            return;
        }
        if (s instanceof Call c) {
            emitCall(c);
            return;
        }
        throw new RuntimeException("[codegen] Unknown stmt: " + s.getClass());
    }

    private void emitExpr(Expr e, String reg) {
        if (e instanceof IntLit i) {
            out.append("LAD ").append(reg).append(", =").append(i.value).append("\n");
            return;
        }
        if (e instanceof VarRef v) {
            out.append("LD ").append(reg).append(", VAR_").append(v.name).append("\n");
            return;
        }
        if (e instanceof Binary b) {
            emitExpr(b.left, reg);
            out.append("PUSH ").append(reg).append("\n");
            emitExpr(b.right, "GR2");
            out.append("POP GR3\n");
            switch (b.op) {
                case ADD -> out.append("AD ").append(reg).append(", GR2\n");
                case SUB -> out.append("SUB ").append(reg).append(", GR2\n");
                case MUL -> out.append("MUL ").append(reg).append(", GR2\n");
                case DIV -> out.append("DIV ").append(reg).append(", GR2\n");
                case LT  -> { out.append("CPA GR3, GR2\n"); out.append("JMI ").append(newLabel("LT_TRUE")).append("\n"); out.append("LAD ").append(reg).append(", =0\n"); }
                default -> out.append("NOP\n"); // expand comparisons properly for your CASL2 dialect
            }
            return;
        }
        if (e instanceof Call c) {
            emitCall(c);
            out.append("LD ").append(reg).append(", GR1\n");
            return;
        }
        throw new RuntimeException("[codegen] Unknown expr: " + e.getClass());
    }

    private void emitCall(Call c) {
        // Minimal: ignore args, jump to function, assume GR1 carries return
        out.append("CALL F_").append(c.name).append("\n");
    }

    private String newLabel(String base) { return base + "_" + (labelSeq++); }
}
