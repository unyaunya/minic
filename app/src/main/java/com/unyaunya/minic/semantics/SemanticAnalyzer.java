
package com.unyaunya.minic.semantics;

import com.unyaunya.minic.frontend.*;
import java.util.*;

/**
 * SemanticAnalyzer performs scope management, type checking,
 * and semantic validation of the AST produced by AstBuilder.
 */
public class SemanticAnalyzer {

    // Stack of scopes: each scope is a map of name -> Symbol
    private final Deque<Map<String, Symbol>> scopes = new ArrayDeque<>();

    // Global function table
    private final Map<String, FunctionDecl> functions = new HashMap<>();

    // New structures for SemanticInfo
    private final Map<String, Map<String, Symbol>> functionSymbols = new HashMap<>();
    private final Map<String, Integer> localSizes = new HashMap<>();

    private int localVarOffset;

    public SemanticInfo analyze(Program program) {
        // Collect function signatures
        for (FunctionDecl f : program.getFunctions()) {
            if (functions.containsKey(f.getName())) {
                error("Duplicate function: " + f.getName());
            }
            functions.put(f.getName(), f);
        }

        // Global scope
        enterScope();
        for (GlobalDecl g : program.getGlobals()) {
            declare(g.getName(), new Symbol(g.getType(), StorageClass.GLOBAL, 0));
        }

        // Analyze each function
        for (FunctionDecl f : program.getFunctions()) {
            analyzeFunction(f);
        }
        functionSymbols.put("_GLOBAL", scopes.peek());
        exitScope();

        return new SemanticInfo(functionSymbols, localSizes);
    }

    private void analyzeFunction(FunctionDecl f) {
        enterScope();
        localVarOffset = 0;

        // Parameters
        int paramOffset = 0;
        for (Param p : f.getParams()) {
            Symbol sym = new Symbol(p.getType(), StorageClass.PARAM, ++paramOffset);
            declare(p.getName(), sym);
        }

        // Analyze body and collect locals
        analyzeBlock(f.getBody(), f.getReturnType());

        // Save function-level info
        functionSymbols.put(f.getName(), scopes.peek());
        localSizes.put(f.getName(), localVarOffset);

        exitScope();
    }

    private void analyzeBlock(Block b, TypeSpec expectedReturn) {
        for (Stmt s : b.getStatements()) {
            analyzeStmt(s, expectedReturn);
        }
    }

    private void analyzeStmt(Stmt s, TypeSpec expectedReturn) {
        if (s instanceof VarDecl v) {
            localVarOffset += v.getType().getSize(); // assume getSize() returns word count
            Symbol sym = new Symbol(v.getType(), StorageClass.LOCAL, localVarOffset);
            declare(v.getName(), sym);

            if (v.getInit() != null) {
                TypeSpec rhs = checkExpr(v.getInit());
                if (!rhs.equals(v.getType())) {
                    // TODO
                    // error("Type mismatch in variable initialization: " + v.getName());
                }
            }
        } else if (s instanceof Assign a) {
            TypeSpec lhs = checkLValue(a.getLvalue());
            TypeSpec rhs = checkExpr(a.getExpr());
            if (!lhs.equals(rhs)) {
                // TODO
                //error("Type mismatch in assignment");
            }
        } else if (s instanceof ReturnStmt r) {
            if (expectedReturn.getBaseType() != BaseType.VOID) {
                if (r.getValue() == null) {
                    error("Missing return value in non-void function");
                } else {
                    TypeSpec retType = checkExpr(r.getValue());
                    if (!retType.equalType(expectedReturn)) {
                        error("Return type mismatch");
                    }
                }
            }
        } else if (s instanceof IfStmt i) {
            TypeSpec condType = checkExpr(i.getCond());
            if (condType.getBaseType() != BaseType.INT) {
                error("Condition in if must be int");
            }
            analyzeBlock(i.getThenBlock(), expectedReturn);
            if (i.getElseBlock() != null) {
                analyzeBlock(i.getElseBlock(), expectedReturn);
            }
        } else if (s instanceof WhileStmt w) {
            TypeSpec condType = checkExpr(w.getCond());
            if (condType.getBaseType() != BaseType.INT) {
                error("Condition in while must be int");
            }
            analyzeBlock(w.getBody(), expectedReturn);
        } else if (s instanceof ForStmt f) {
            if (f.getInit() != null) analyzeStmt(f.getInit(), expectedReturn);
            if (f.getCond() != null) {
                TypeSpec condType = checkExpr(f.getCond());
                if (condType.getBaseType() != BaseType.INT) {
                    error("Condition in for must be int");
                }
            }
            if (f.getUpdate() != null) analyzeStmt(f.getUpdate(), expectedReturn);
            analyzeBlock(f.getBody(), expectedReturn);
        } else if (s instanceof MacroStmt f) {
            if (!List.of("_IN", "_OUT").contains(f.getOp().toUpperCase())) {
                error(String.format("Illegal macro '%s'", f.getOp()));
            }
        } else if (s instanceof ExprStmt e) {
            checkExpr(e.getExpr()); // ensure function calls are valid
        }
    }

    private TypeSpec checkExpr(Expr e) {
        if (e instanceof IntLit) {
            return new TypeSpec(BaseType.INT, 0, null);
        } else if (e instanceof VarRef v) {
            return lookup(v.getName()).getType();
        } else if (e instanceof Binary b) {
            TypeSpec lt = checkExpr(b.getLeft());
            TypeSpec rt = checkExpr(b.getRight());
            if (!lt.equals(rt)) {
                // TODO
                // error("Type mismatch in binary expression");
            }
            return lt;
        } else if (e instanceof UnaryNeg u) {
            TypeSpec t = checkExpr(u.getExpr());
            if (t.getBaseType() != BaseType.INT) {
                error("Unary negation only valid on int");
            }
            return t;
        } else if (e instanceof AddressOf a) {
            Symbol sym = lookup(a.getName());
            return new TypeSpec(sym.getType().getBaseType(), sym.getType().getPointerDepth() + 1, null);
        } else if (e instanceof LvPtrDeref d) {
            TypeSpec t = checkExpr(d.getExpr());
            if (t.getPointerDepth() == 0) {
                error("Cannot dereference non-pointer");
            }
            return new TypeSpec(t.getBaseType(), t.getPointerDepth() - 1, null);
        } else if (e instanceof LvArrayElem arr) {
            Symbol sym = lookup(arr.getName());
            if (sym.getType().getArraySize() == 0) {
                error("Not an array: " + arr.getName());
            }
            TypeSpec idxType = checkExpr(arr.getExpr());
            if (idxType.getBaseType() != BaseType.INT) {
                error("Array index must be int");
            }
            return new TypeSpec(sym.getType().getBaseType(), sym.getType().getPointerDepth(), null);
        } else if (e instanceof Call c) {
            FunctionDecl f = functions.get(c.getName());
            if (f == null) {
                error("Call to undeclared function: " + c.getName());
            }
            if (c.getArgs().size() != f.getParams().size()) {
                error("Argument count mismatch in call to " + c.getName());
            }
            for (int i = 0; i < c.getArgs().size(); i++) {
                TypeSpec argType = checkExpr(c.getArgs().get(i));
                TypeSpec paramType = f.getParams().get(i).getType();
                if (!argType.equals(paramType)) {
                    error("Argument type mismatch in call to " + c.getName());
                }
            }
            return f.getReturnType();
        }
        error("Unknown expression type: " + e);
        return new TypeSpec(BaseType.INT, 0, null); // fallback
    }

    private TypeSpec checkLValue(LValue lv) {
        if (lv instanceof LvVar v) {
            return lookup(v.getName()).getType();
        } else if (lv instanceof LvArrayElem arr) {
            Symbol sym = lookup(arr.getName());
            return new TypeSpec(sym.getType().getBaseType(), sym.getType().getPointerDepth(), null);
        } else if (lv instanceof LvPtrDeref d) {
            TypeSpec t = checkExpr(d.getExpr());
            if (t.getPointerDepth() == 0) {
                error("Cannot dereference non-pointer");
            }
            return new TypeSpec(t.getBaseType(), t.getPointerDepth() - 1, 0);
        }
        error("Unknown lvalue: " + lv);
        return new TypeSpec(BaseType.INT, 0, null);
    }

    // ----------------------
    // Scope helpers
    // ----------------------
    private void enterScope() { scopes.push(new HashMap<>()); }
    private void exitScope() { scopes.pop(); }

    private void declare(String name, Symbol sym) {
        Map<String, Symbol> current = scopes.peek();
        if (current.containsKey(name)) {
            error("Redeclaration: " + name);
        }
        current.put(name, sym);
    }

    private Symbol lookup(String name) {
        for (Map<String, Symbol> scope : scopes) {
            if (scope.containsKey(name)) return scope.get(name);
        }
        error("Undeclared identifier: " + name);
        return null;
    }

    private void error(String msg) {
        throw new RuntimeException("Semantic error: " + msg);
    }
}
