
package com.unyaunya.minic.backend;

import static com.unyaunya.minic.backend.Casl2Builder.*;

import com.unyaunya.minic.MinicException;
import com.unyaunya.minic.frontend.*;
import com.unyaunya.minic.semantics.SemanticInfo;
import com.unyaunya.minic.semantics.StorageClass;
import com.unyaunya.minic.semantics.Symbol;

import java.util.*;

public class Casl2Emitter {
    private final Casl2Builder builder = new Casl2Builder();
    private SemanticInfo semanticInfo;
    private FunctionDecl currentFunction;
    private Casl2LabelGenerator lgCompareTrue = new Casl2LabelGenerator("CT", "True branch of a comparison");
    private Casl2LabelGenerator lgCompareEnd = new Casl2LabelGenerator("CE", "End label of a comparison");
    private Casl2LabelGenerator lgIfElse = new Casl2LabelGenerator("IFELS", "Else clause");
    private Casl2LabelGenerator lgIfEnd = new Casl2LabelGenerator("ENDIF", "End of if statement");
    private Casl2LabelGenerator lgWhile = new Casl2LabelGenerator("WHIL", "While statement");
    private Casl2LabelGenerator lgWend = new Casl2LabelGenerator("WEND", "End of while statement");
    private Casl2LabelGenerator lgFor = new Casl2LabelGenerator("FOR", "For statement");
    private Casl2LabelGenerator lgNext = new Casl2LabelGenerator("NXT", "End of while statement");

    public String emit(Program program, SemanticInfo semanticInfo, int stackSize) {
        this.semanticInfo = semanticInfo;
        builder.start().l("PRG").c("Program start");
        builder.lad(GR1, stackSize);
        builder.lad(GR7, "STACK", GR1).c("GR7 plays a role of ESP in x86");
        builder.ld(GR6, GR7).c("GR7 plays a role of EBP in x86");
        for (FunctionDecl f : program.getFunctions()) {
            emitFunction(f);
        }
        builder.comment("Data Section");
        emitGlobals(program.getGlobals());
        builder.ds(stackSize).l("STACK").c("Stack Ares");
        builder.end().c("Program end");
        return builder.build();
    }

    private void emitGlobals(List<GlobalDecl> globals) {
        for (GlobalDecl g : globals) {
            int size = (g.getType().getArraySize() != null) ? g.getType().getArraySize() : 1;
            builder.ds(size).l(g.getName().toUpperCase());
        }
    }

    private void emitFunction(FunctionDecl f) {
        this.currentFunction = f;
        builder.comment(f.toString());
        builder.suba(GR7, 1).l(f.getName().toUpperCase());
        builder.st(GR6, "0", GR7).c("Like push ebp");
        builder.ld(GR6, GR7).c("Like mov ebp, esp");
        int localSize = this.semanticInfo.getLocalSize(f.getName());
        if (localSize > 0) {
            builder.suba(GR7, localSize).c("Secure local variables");
        }
        emitBlock(f.getBody());
        if (!builder.lastIsRet()) {
            emitReturn();
        }
        this.currentFunction = null;
    }

    private void emitReturn() {
        int localSize = this.semanticInfo.getLocalSize(this.currentFunction.getName());
        if (localSize > 0) {
            builder.adda(GR7, localSize);
        }
        builder.ld(GR7, GR6).c("Like mov esp, ebp");
        builder.ld(GR6, "0", GR7).c("Like pop ebp");
        builder.adda(GR7, 1);
        builder.ret();
    }

    private void emitCall(Call c) {
        // Push arguments in reverse order
        for (Expr arg : c.getArgs().reversed()) {
            emitExpr(arg); // result in GR1
            builder.suba(GR7, 1).c("Decrement stack pointer");
            builder.st(GR1, "0", GR7);
        }
        // Call function
        builder.call(c.getName().toUpperCase());
        // Release arguments
        if (!c.getArgs().isEmpty()) {
            builder.adda(GR7, c.getArgs().size());
        }
        // Move return value to GR1
        builder.ld(GR1, GR0);
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
        } else if (s instanceof MacroStmt m) {
            emitMacro(m);
        } else if (s instanceof Block b) {
            emitBlock(b);
        } else if (s instanceof VarDecl v) {
            emitVarDecl(v);
        } else {
            throw new MinicException(String.format("Unimplemented: %s", s.toString()));
        }
    }

    private void emitVarDecl(VarDecl v) {
        builder.comment(v.toString());
        if (v.getInit() != null) {
            emitAssign(new LvVar(v.getName()), v.getInit());
        }
    }

    private void emitAssign(Assign a) {
        builder.comment(a.toString());
        emitAssign(a.getLvalue(), a.getExpr());
    }

    private void emitAssign(LValue lvalue, Expr expr) {
        emitExpr(expr);
        if (lvalue instanceof LvVar v) {
            Symbol symbol = this.semanticInfo.getSymbol(this.currentFunction.getName(), v.getName());
            String comment = String.format("Store %s", v.getName());
            switch (symbol.getStorageClass()) {
            case StorageClass.GLOBAL -> builder.st(GR1, v.getName().toUpperCase()).c(comment);
            case StorageClass.LOCAL ->  builder.st(GR1, 65536 - symbol.getOffset(), GR6).c(comment);
            case StorageClass.PARAM ->  builder.st(GR1, symbol.getOffset(), GR6).c(comment);
            }
        } else if (lvalue instanceof LvArrayElem lv) {
            // evacuate the value to assign
            builder.push("0", GR1);
            // put the index of the array in GR1
            emitExpr(lv.getExpr());
            // put the start address of the array in GR2
            Symbol symbol = this.semanticInfo.getSymbol(this.currentFunction.getName(), lv.getName());
            switch (symbol.getStorageClass()) {
            case StorageClass.GLOBAL -> builder.lad(GR2, lv.getName().toUpperCase());
            case StorageClass.LOCAL ->  builder.lad(GR2, 65536 - symbol.getOffset(), GR6);
            case StorageClass.PARAM ->  builder.lad(GR2, symbol.getOffset(), GR6);
            }
            // put the address of the target element in GR2
            builder.adda(GR2, GR1);
            // put the value to assign in GR1
            builder.pop(GR1);
            // store the value to assign in the target element
            builder.st(GR1, "0", GR2);
        } else {
            builder.comment("TODO: handle pointer assignment");
        }
    }

    private void emitExpr(Expr e) {
        switch (e) {
            case IntLit lit -> builder.lad(GR1, lit.getValue());
            case VarRef v -> {
                Symbol symbol = this.semanticInfo.getSymbol(this.currentFunction.getName(), v.getName());
                switch (symbol.getStorageClass()) {
                case StorageClass.GLOBAL -> builder.ld(GR1, v.getName().toUpperCase());
                case StorageClass.LOCAL ->  builder.ld(GR1, 65536 - symbol.getOffset(), GR6);
                case StorageClass.PARAM ->  builder.ld(GR1, symbol.getOffset(), GR6);
                }
            }
            case LvArrayElem v -> {
                // put the index of the array in GR1
                emitExpr(v.getExpr());
                // put the start address of the array in GR2
                Symbol symbol = this.semanticInfo.getSymbol(this.currentFunction.getName(), v.getName());
                switch (symbol.getStorageClass()) {
                case StorageClass.GLOBAL -> builder.lad(GR2, v.getName().toUpperCase());
                case StorageClass.LOCAL ->  builder.lad(GR2, 65536 - symbol.getOffset(), GR6);
                case StorageClass.PARAM ->  builder.lad(GR2, symbol.getOffset(), GR6);
                }
                // put the address of the target element in GR2
                builder.adda(GR2, GR1);
                // put the value of the target element in GR1
                builder.ld(GR1, "0", GR2);
            }
            case Binary bin -> {
                emitExpr(bin.getRight());
                builder.push("0", GR1);
                emitExpr(bin.getLeft());
                builder.pop(GR2);
                switch (bin.getOp()) {
                case ADD -> builder.adda(GR1, GR2);
                case SUB -> builder.suba(GR1, GR2);
                case MUL -> builder.comment("TODO: MULA not implemented");
                case DIV -> builder.comment("TODO: DIVA not implemented");
                default -> emitComparison(bin.getOp());
                }
            }
            case UnaryNeg u -> {
                emitExpr(u.getExpr());
                builder.xor(GR0, GR0);
                builder.suba(GR0, GR1);
                builder.ld(GR1, GR0);
            }
            case Call c -> emitCall(c);
            default -> {
                throw new MinicException(String.format("Unimplemented: %s", e.toString()));
            }
        }
    }

    private void emitComparison(Binary.Op op) {
        String trueLabel = lgCompareTrue.getNewLabel();
        String endLabel = lgCompareEnd.getNewLabel();
        builder.cpa(GR1, GR2);
        switch (op) {
        case LT -> builder.jmi(trueLabel);
        case GT -> builder.jpl(trueLabel);
        case LE -> { builder.jmi(trueLabel); builder.jze(trueLabel); }
        case GE -> { builder.jpl(trueLabel); builder.jze(trueLabel); }
        case EQ -> builder.jze(trueLabel);
        case NE -> builder.jnz(trueLabel);
        default -> throw new MinicException("unreachable");
        }
        builder.xor(GR0, GR0).c("False branch");
        builder.jump(endLabel);
        builder.ld(GR0, GR7).l(trueLabel).c("True branch");
        builder.nop().l(endLabel);
    }

    private void emitIf(IfStmt i) {
        String elseLabel = lgIfElse.getNewLabel();
        String endLabel = lgIfEnd.getNewLabel();
        emitExpr(i.getCond());
        if(i.getElseBlock() != null) {
            builder.jze(elseLabel);
            emitBlock(i.getThenBlock());
            builder.jump(endLabel);
            builder.nop().l(elseLabel);
            emitBlock(i.getElseBlock());
        } else {
            builder.jze(endLabel);
            emitBlock(i.getThenBlock());
        }
        builder.nop().l(endLabel);
    }

    private void emitWhile(WhileStmt w) {
        String startLabel = lgWhile.getNewLabel();
        String endLabel = lgWend.getNewLabel();
        builder.comment(w.toString());
        builder.nop().l(startLabel);
        builder.comment(w.getCond().toString());
        emitExpr(w.getCond());
        builder.jze(endLabel);
        emitBlock(w.getBody());
        builder.jump(startLabel);
        builder.nop().l(endLabel);
    }

    private void emitFor(ForStmt f) {
        String startLabel = lgFor.getNewLabel();
        String endLabel = lgNext.getNewLabel();
        if (f.getInit() != null) emitStmt(f.getInit());
        builder.nop().l(startLabel);
        if (f.getCond() != null) {
            emitExpr(f.getCond());
            builder.jze(endLabel);
        }
        emitBlock(f.getBody());
        if (f.getUpdate() != null) emitStmt(f.getUpdate());
        builder.jump(startLabel);
        builder.nop().l(endLabel);
    }

    private void emitReturn(ReturnStmt r) {
        builder.comment(r.toString());      
        if (r.getValue() != null) {
            emitExpr(r.getValue());
            builder.ld(GR0, GR1);
        }
        emitReturn();
    }

    private void emitMacro(MacroStmt m) {
        builder.comment(m.toString());
        if("_IN".equalsIgnoreCase(m.getOp())) {
            builder.in("IBUF", "ILEN");
        } else {
            builder.out("OBUF", "OLEN");
        }
    }
}
