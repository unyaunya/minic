// semantics/SemanticAnalyzer.java
package com.unyaunya.minic.semantics;

import com.unyaunya.minic.frontend.*;
import java.util.*;

public class SemanticAnalyzer {
    private static class Scope {
        final Map<String, BaseType> vars = new HashMap<>();
        final Scope parent;
        Scope(Scope parent) { this.parent = parent; }
        BaseType get(String name) { return vars.containsKey(name) ? vars.get(name) : (parent == null ? null : parent.get(name)); }
    }

    private final Map<String, FunctionDecl> functions = new HashMap<>();

    public void check(Program p) {
        // collect functions
        for (FunctionDecl f : p.functions) {
            if (functions.putIfAbsent(f.name, f) != null) throw error("Duplicate function: " + f.name);
        }
        // check bodies
        for (FunctionDecl f : p.functions) checkFunction(f);
    }

    private void checkFunction(FunctionDecl f) {
        Scope scope = new Scope(null);
        for (Param param : f.params) {
            if (scope.vars.put(param.name, param.type) != null) throw error("Param redeclared: " + param.name);
        }
        boolean returnsOk = false;
        for (Stmt s : f.body) {
            returnsOk |= checkStmt(s, scope, f.returnType);
        }
        if (f.returnType != BaseType.VOID && !returnsOk) throw error("Missing return in function: " + f.name);
    }

    private boolean checkStmt(Stmt s, Scope scope, BaseType expectedReturn) {
        if (s instanceof VarDecl vd) {
            if (scope.vars.put(vd.name, vd.type) != null) throw error("Variable redeclared: " + vd.name);
            if (vd.init != null) expectType(vd.type, infer(vd.init, scope), "Initializer type mismatch for " + vd.name);
            return false;
        }
        if (s instanceof Assign a) {
            BaseType t = scope.get(a.name);
            if (t == null) throw error("Undefined variable: " + a.name);
            expectType(t, infer(a.value, scope), "Assignment type mismatch for " + a.name);
            return false;
        }
        if (s instanceof ReturnStmt r) {
            BaseType actual = r.value == null ? BaseType.VOID : infer(r.value, scope);
            expectType(expectedReturn, actual, "Return type mismatch");
            return true;
        }
        if (s instanceof IfStmt i) {
            expectType(BaseType.INT, infer(i.cond, scope), "If condition must be int");
            boolean thenRet = blockReturns(i.thenBlock, scope, expectedReturn);
            boolean elseRet = blockReturns(i.elseBlock, scope, expectedReturn);
            return thenRet && elseRet;
        }
        if (s instanceof WhileStmt w) {
            expectType(BaseType.INT, infer(w.cond, scope), "While condition must be int");
            blockReturns(w.body, scope, expectedReturn);
            return false;
        }
        if (s instanceof Call c) {
            checkCall(c, scope);
            return false;
        }
        throw error("Unknown statement kind: " + s.getClass());
    }

    private boolean blockReturns(List<Stmt> block, Scope parent, BaseType expectedReturn) {
        Scope inner = new Scope(parent);
        boolean ret = false;
        for (Stmt s : block) ret |= checkStmt(s, inner, expectedReturn);
        return ret;
    }

    private BaseType infer(Expr e, Scope scope) {
        if (e instanceof IntLit) return BaseType.INT;
        if (e instanceof VarRef v) {
            BaseType t = scope.get(v.name);
            if (t == null) throw error("Undefined variable: " + v.name);
            return t;
        }
        if (e instanceof Binary b) {
            BaseType lt = infer(b.left, scope);
            BaseType rt = infer(b.right, scope);
            expectType(BaseType.INT, lt, "Binary left must be int");
            expectType(BaseType.INT, rt, "Binary right must be int");
            return BaseType.INT;
        }
        if (e instanceof Call c) {
            FunctionDecl f = functions.get(c.name);
            if (f == null) throw error("Undefined function: " + c.name);
            if (c.args.size() != f.params.size()) throw error("Arg count mismatch for " + c.name);
            for (int i = 0; i < c.args.size(); i++) {
                expectType(f.params.get(i).type, infer(c.args.get(i), scope), "Arg type mismatch for " + c.name);
            }
            return f.returnType;
        }
        throw error("Unknown expression kind: " + e.getClass());
    }

    private void checkCall(Call c, Scope scope) { infer(c, scope); }

    private void expectType(BaseType expected, BaseType actual, String msg) {
        if (expected != actual) throw error(msg + " (expected " + expected + ", got " + actual + ")");
    }

    private RuntimeException error(String msg) { return new RuntimeException("[semantic] " + msg); }
}
