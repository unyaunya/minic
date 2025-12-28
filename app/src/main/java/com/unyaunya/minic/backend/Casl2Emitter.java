
package com.unyaunya.minic.backend;

import static com.unyaunya.minic.backend.Casl2Builder.*;

import com.unyaunya.minic.MinicException;
import com.unyaunya.minic.backend.Casl2Builder.Casl2LabelGenerator;
import com.unyaunya.minic.frontend.*;
import com.unyaunya.minic.semantics.SemanticInfo;
import com.unyaunya.minic.semantics.StorageClass;
import com.unyaunya.minic.semantics.Symbol;

import java.util.*;
import java.util.Map.Entry;

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
    private SortedMap<String, String> strings = new TreeMap<>();

    public String emit(Program program, SemanticInfo semanticInfo, int stackSize) {
        this.semanticInfo = semanticInfo;
        // prepare labels for string literals
        Casl2LabelGenerator lgStr = new Casl2LabelGenerator("STR", "String literal");
        for (String s : this.semanticInfo.getStrings()) {
            this.strings.put(s, lgStr.getNewLabel());
        }
        //
        boolean hasMain =  program.getFunctions().stream().anyMatch(f -> f.getName().equalsIgnoreCase("MAIN"));
        if (hasMain) {
            builder.start("ENTRY").l("PRG").c("Program start");
        } else {
            builder.start().l("PRG").c("Program start");
        }
        builder.comment("Data Section Start");
        emitGlobals(program.getGlobals());
        builder.comment("Data Section End");
        if (hasMain) {
            builder.lad(GR1, stackSize).l("ENTRY").c("Put stacksize to GR1");
            builder.lad(GR7, "STACK", GR1).c("GR7 plays a role of ESP in x86");
            builder.ld(GR6, GR7).c("GR6 plays a role of EBP in x86");
            builder.call("MAIN");
            builder.ret();
        } 
        for (FunctionDecl f : program.getFunctions()) {
            emitFunction(f);
        }
        if (hasMain) {
            builder.ds(stackSize).l("STACK").c("Stack Ares");
        }
        builder.end().c("Program end");
        return builder.build();
    }

    private void emitGlobals(List<GlobalDecl> globals) {
        for (GlobalDecl g : globals) {
            int size = (g.getType().getArraySize() != null) ? g.getType().getArraySize() : 1;
            builder.ds(size).l(g.getName().toUpperCase());
        }
        for (Entry<String, String> entry : this.strings.entrySet()) {
            String s = String.format("'%s'", entry.getKey());
            builder.dc(s).l(entry.getValue());
            builder.dc("0");
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
        builder.comment("%s(%s);", c.getName(), String.join(",", c.getArgs().stream().map(Object::toString).toList()));
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
        switch (s) {
          case Assign a ->     emitAssign(a);
          case ExprStmt e ->   emitExpr(e.getExpr());
          case IfStmt i ->     emitIf(i);
          case WhileStmt w ->  emitWhile(w);
          case ForStmt f ->    emitFor(f);
          case ReturnStmt r -> emitReturn(r);
          case MacroStmt m ->  emitMacro(m);
          case Block b ->      emitBlock(b);
          case VarDecl v ->    emitVarDecl(v);
          default ->           throw new MinicException(s.getLocation(), "Unimplemented: %s", s);
        }
    }

    private void emitVarDecl(VarDecl v) {
        builder.comment(v.toString());
        if (v.getInit() != null) {
            emitAssign(new LvVar(v.getLocation(), v.getName()), v.getInit());
        }
    }

    private void emitAssign(Assign a) {
        builder.comment(a.toString());
        emitAssign(a.getLvalue(), a.getExpr());
    }

    private void emitAssign(LValue lvalue, Expr expr) {
        // calculate the value to assign
        emitExpr(expr);
        // evacuate the value to assign
        builder.push("0", GR1).c("Push rvalue");
        // calculate the address to store
        emitLValueAddress(lvalue);
        // put the value to assign in GR1
        builder.pop(GR1).c("Pop rvalue to GR1");
        // store the assign to value
        builder.st(GR1, "0", GR5).c("Store rvalue in the addr of lvalue");
    }

    private void emitLValueAddress(LValue lvalue) {
        switch (lvalue) {
            case LvVar v -> {
                Symbol symbol = this.semanticInfo.getSymbol(this.currentFunction.getName(), v.getName());
                if (symbol.isArray()) {
                    throw new MinicException("Can't assign %s for it is not a variable.", v.getName());
                }
                emitSymbolAddress(v.getName(), symbol, GR5);
            }
            case LvPtrDeref p -> {
                emitExpr(p.getExpr());
                builder.lad(GR5, 0, GR1);        
            }
            case LvArrayElem lv -> {
                // put the index of the array in GR1
                emitExpr(lv.getExpr());
                // put the start address of the array in GR5
                Symbol symbol = this.semanticInfo.getSymbol(this.currentFunction.getName(), lv.getName());
                emitSymbolValue(lv.getName(), symbol, GR5);
                // put the address of the target element in GR5
                builder.adda(GR5, GR1);
            }
            default -> throw new MinicException(lvalue.getLocation(), "Unimplemented: %s", lvalue);
        }
    }

    private void emitSymbolAddress(String name, Symbol symbol, String reg) {
        String comment = String.format("Put Address of %s to %s", name, reg);
        switch (symbol.getStorageClass()) {
        case StorageClass.GLOBAL -> builder.lad(reg, name.toUpperCase()).c(comment);
        case StorageClass.LOCAL ->  builder.lad(reg, 65536 - symbol.getOffset(), GR6).c(comment);
        case StorageClass.PARAM ->  builder.lad(reg, symbol.getOffset(), GR6).c(comment);
        }
    }

    private void emitSymbolValue(String name, Symbol symbol, String reg) {
        String comment = String.format("Put Value of %s to %s", name, reg);
        if (symbol.isArray()) {
            switch (symbol.getStorageClass()) {
            case StorageClass.GLOBAL -> builder.lad(reg, name.toUpperCase()).c(comment);
            case StorageClass.LOCAL ->  builder.lad(reg, 65536 - symbol.getOffset(), GR6).c(comment);
            case StorageClass.PARAM ->  builder.lad(reg, symbol.getOffset(), GR6).c(comment);
            }
        } else {
            switch (symbol.getStorageClass()) {
            case StorageClass.GLOBAL -> builder.ld(reg, name.toUpperCase()).c(comment);
            case StorageClass.LOCAL ->  builder.ld(reg, 65536 - symbol.getOffset(), GR6).c(comment);
            case StorageClass.PARAM ->  builder.ld(reg, symbol.getOffset(), GR6).c(comment);
            }
        }
    }

    private void emitVarRef(String reg, String name) {
        Symbol symbol = this.semanticInfo.getSymbol(this.currentFunction.getName(), name);
        emitSymbolValue(name, symbol, reg);
    }

    private void emitPtrDeref(String reg, PtrDeref p) {
        emitExpr(p.getExpr());
        builder.ld(GR1, 0, GR1);
    }

    private void emitExpr(Expr e) {
        switch (e) {
            case IntLit lit -> builder.lad(GR1, lit.getValue()).c("Put the int lit to GR1");
            case StringLit lit -> builder.lad(GR1, this.strings.get(lit.getValue())).c("Put the addr of string");
            case VarRef v -> emitVarRef(GR1, v.getName());
            case PtrDeref p -> emitPtrDeref(GR1, p);
            case AddressOf a -> {
                Symbol symbol = this.semanticInfo.getSymbol(this.currentFunction.getName(), a.getName());
                emitSymbolAddress(a.getName(), symbol, GR1);
            }
            case Cast c -> {
                // NOP;
            }
            case ArrayElem v -> {
                builder.comment("    Calculate %s[%s]", v.getName(), v.getExpr());
                // put the index of the array in GR1
                emitExpr(v.getExpr());
                // put the start address of the array in GR5
                Symbol symbol = this.semanticInfo.getSymbol(this.currentFunction.getName(), v.getName());
                emitSymbolValue(v.getName(), symbol, GR5);
                // put the address of the target element in GR5
                builder.adda(GR5, GR1).c("Add index to the address");
                // put the value of the target element in GR1
                builder.ld(GR1, "0", GR5).c("Put val to GR1");
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
            default -> throw new MinicException(e.getLocation(), "Unimplemented: %s", e);
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
        builder.xor(GR0, GR0).c("False branch:ZF on");
        builder.jump(endLabel);
        builder.ld(GR0, GR7).l(trueLabel).c("True branch:ZF off");
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
