
package com.unyaunya.minic.semantics;

import com.unyaunya.minic.Location;
import com.unyaunya.minic.MinicException;
import com.unyaunya.minic.ast.*;
import com.unyaunya.minic.ast.Binary.Op;

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
    private final TreeSet<String> strings = new TreeSet<>();

    private int localVarOffset;

    public SemanticInfo analyze(Program program) throws MinicException {
        // Collect function signatures
        for (FunctionDecl f : program.getFunctions()) {
            if (functions.containsKey(f.getName())) {
                error(f.getLocation(), "Duplicate function: " + f.getName());
            }
            functions.put(f.getName(), f);
        }

        // Global scope
        enterScope();
        for (GlobalDecl g : program.getGlobals()) {
            declare(g.getName(), new Symbol(g.getType(), StorageClass.GLOBAL, 0), g.getLocation());
        }

        // Analyze each function
        for (FunctionDecl f : program.getFunctions()) {
            analyzeFunction(f);
        }
        functionSymbols.put("_GLOBAL", scopes.peek());
        exitScope();

        return new SemanticInfo(functionSymbols, localSizes, this.strings);
    }

    private void analyzeFunction(FunctionDecl f) throws MinicException {
        enterScope();
        localVarOffset = 0;

        // Parameters
        int paramOffset = 0;
        for (Param p : f.getParams()) {
            Symbol sym = new Symbol(p.getType(), StorageClass.PARAM, ++paramOffset);
            declare(p.getName(), sym, p.getLocation());
        }

        // Analyze body and collect locals
        analyzeBlock(f.getBody(), f.getReturnType());

        // Save function-level info
        functionSymbols.put(f.getName(), scopes.peek());
        localSizes.put(f.getName(), localVarOffset);

        exitScope();
    }

    private void analyzeBlock(Block b, TypeSpec expectedReturn) throws MinicException {
        for (Stmt s : b.getStatements()) {
            analyzeStmt(s, expectedReturn);
        }
    }

    private void analyzeStmt(Stmt s, TypeSpec expectedReturn) throws MinicException {
        if (s instanceof VarDecl v) {
            localVarOffset += v.getType().getSize(); // assume getSize() returns word count
            Symbol sym = new Symbol(v.getType(), StorageClass.LOCAL, localVarOffset);
            declare(v.getName(), sym, v.getLocation());

            if (v.getInit() != null) {
                TypeSpec rhs = checkExpr(v.getInit());
                if (!rhs.isCompatible(v.getType())) {
                    error(v.getLocation(), "Type mismatch in variable initialization: " + v.getName());
                }
            }
        } else if (s instanceof Assign a) {
            TypeSpec lhs = checkLValue(a.getLvalue());
            TypeSpec rhs = checkExpr(a.getExpr());
            if (!lhs.isCompatible(rhs)) {
                error(a.getLocation(), "Type mismatch in assignment");
            }
        } else if (s instanceof ReturnStmt r) {
            if (expectedReturn.getBaseType() != BaseType.VOID) {
                if (r.getValue() == null) {
                    error(r.getLocation(), "Missing return value in non-void function");
                } else {
                    TypeSpec retType = checkExpr(r.getValue());
                    if (!retType.isCompatible(expectedReturn)) {
                        error(r.getLocation(), "Return type mismatch");
                    }
                }
            } else {
                if (r.getValue() != null) {
                    error(r.getLocation(), "Can't return value in void function");
                }
            }
        } else if (s instanceof IfStmt i) {
            TypeSpec condType = checkExpr(i.getCond());
            if (condType.getBaseType() != BaseType.INT) {
                error(i.getLocation(), "Condition in if must be int");
            }
            analyzeBlock(i.getThenBlock(), expectedReturn);
            if (i.getElseBlock() != null) {
                analyzeBlock(i.getElseBlock(), expectedReturn);
            }
        } else if (s instanceof WhileStmt w) {
            TypeSpec condType = checkExpr(w.getCond());
            if (condType.getBaseType() != BaseType.INT) {
                error(w.getLocation(),"Condition in while must be int");
            }
            analyzeBlock(w.getBody(), expectedReturn);
        } else if (s instanceof ForStmt f) {
            if (f.getInit() != null) analyzeStmt(f.getInit(), expectedReturn);
            if (f.getCond() != null) {
                TypeSpec condType = checkExpr(f.getCond());
                if (condType.getBaseType() != BaseType.INT) {
                    error(f.getLocation(),"Condition in for must be int");
                }
            }
            if (f.getUpdate() != null) analyzeStmt(f.getUpdate(), expectedReturn);
            analyzeBlock(f.getBody(), expectedReturn);
        } else if (s instanceof MacroStmt f) {
            if (!List.of("_IN", "_OUT").contains(f.getOp().toUpperCase())) {
                error(f.getLocation(),"Illegal macro '%s'", f.getOp());
            }
        } else if (s instanceof ExprStmt e) {
            checkExpr(e.getExpr()); // ensure function calls are valid
        }
    }

    private TypeSpec checkExpr(Expr e) throws MinicException {
        if (e instanceof IntLit) {
            return new TypeSpec(BaseType.INT);
        } else if (e instanceof StringLit s) {
            this.strings.add(s.getValue());
            return new TypeSpec(BaseType.INT, 1);
        } else if (e instanceof VarRef v) {
            return lookup(v.getName(), v.getLocation()).getType();
        } else if (e instanceof Binary b) {
            return checkBinary(b);
        } else if (e instanceof UnaryNeg u) {
            TypeSpec t = checkExpr(u.getExpr());
            if (t.getBaseType() != BaseType.INT) {
                error(u.getLocation(),"Unary negation only valid on int");
            }
            return t;
        } else if (e instanceof LogicalNot n) {
            TypeSpec t = checkExpr(n.getExpr());
            if (t.getBaseType() != BaseType.INT) {
                error(n.getLocation(), "Logical not (!) only valid on int");
            }
            return new TypeSpec(BaseType.INT);
        } else if (e instanceof AddressOf a) {
            Symbol sym = lookup(a.getName(), a.getLocation());
            return sym.getType().getAddressType();
        } else if (e instanceof PtrDeref d) {
            TypeSpec t = checkExpr(d.getExpr());
            if (t.getEffectivePointerDepth() == 0) {
                error(d.getLocation(),"Cannot dereference non-pointer");
            }
            return t.getDerefType();
        } else if (e instanceof Cast d) {
            return d.getType();
        } else if (e instanceof ArrayElem arr) {
            Symbol sym = lookup(arr.getName(), arr.getLocation());
            TypeSpec idxType = checkExpr(arr.getExpr());
            if (idxType.getBaseType() != BaseType.INT) {
                error(arr.getLocation(), "Array index must be int");
            }
            return sym.getType().getDerefType();
        } else if (e instanceof Call c) {
            FunctionDecl f = functions.get(c.getName());
            if (f == null) {
                error(c.getLocation(), "Call to undeclared function: " + c.getName());
            }
            if (c.getArgs().size() != f.getParams().size()) {
                error(c.getLocation(), "Argument count mismatch in call to " + c.getName());
            }
            for (int i = 0; i < c.getArgs().size(); i++) {
                TypeSpec argType = checkExpr(c.getArgs().get(i));
                TypeSpec paramType = f.getParams().get(i).getType();
                if (!argType.isCompatible(paramType)) {
                    error(c.getLocation(), "Argument type mismatch in call to " + c.getName());
                }
            }
            return f.getReturnType();
        }
        error(e.getLocation(), "Unknown expression type: " + e);
        return new TypeSpec(BaseType.INT); // fallback
    }

    private TypeSpec checkBinary(Binary b) throws MinicException {
        TypeSpec lt = checkExpr(b.getLeft());
        TypeSpec rt = checkExpr(b.getRight());
        switch (b.getOp()) {
            case Op.ADD -> { return checkAdd(lt, rt, b.getLocation()); }
            case Op.SUB -> { return checkSub(lt, rt, b.getLocation()); }
            case Op.AND, Op.OR -> {
                if (lt.getBaseType() != BaseType.INT || rt.getBaseType() != BaseType.INT) {
                    error(b.getLocation(), "Logical &&/|| operands must be int");
                }
                return new TypeSpec(BaseType.INT);
            }
            case Op.LT, Op.LE, Op.GT, Op.GE, Op.EQ, Op.NE -> {
                if (!lt.isCompatible(rt)) {
                    error(b.getLocation(), "Type mismatch in comparison");
                }
                return new TypeSpec(BaseType.INT);
            }
            default -> error(b.getLocation(), "Unknown binary operator: " + b.getOp());
        }
        return lt;
    }

     private TypeSpec checkAdd(TypeSpec lt, TypeSpec rt, Location loc) throws MinicException {
        if(lt.isSimpleInt()) {
            return rt;
        } else {
            if(rt.isSimpleInt()) {
                return lt;
            } else {
                error(loc, "Type mismatch in binary expression");
                return new TypeSpec(BaseType.INT);
            }
        }
    }

     private TypeSpec checkSub(TypeSpec lt, TypeSpec rt, Location loc) throws MinicException {
        if(rt.isSimpleInt()) {
            return lt;
        } else {
            if (lt.isCompatible(rt)) {
                return new TypeSpec(BaseType.INT);
            } else {
                error(loc, "Type mismatch in binary expression");
                return new TypeSpec(BaseType.INT);
            }
        }
    }
    
    private TypeSpec checkLValue(LValue lv) throws MinicException {
        if (lv instanceof LvVar v) {
            return lookup(v.getName(), v.getLocation()).getType();
        } else if (lv instanceof LvArrayElem arr) {
            Symbol sym = lookup(arr.getName(), arr.getLocation());
            if (sym.getType().getEffectivePointerDepth() == 0) {
                error(lv.getLocation(), "Cannot dereference non-pointer");
            }
            return sym.getType().getDerefType();
        } else if (lv instanceof LvPtrDeref d) {
            TypeSpec t = checkExpr(d.getExpr());
            if (t.getEffectivePointerDepth() == 0) {
                error(d.getLocation(), "Cannot dereference non-pointer");
            }
            return t.getDerefType();
        }
        error(lv.getLocation(), "Unknown lvalue: " + lv);
        return new TypeSpec(BaseType.INT);
    }

    // ----------------------
    // Scope helpers
    // ----------------------
    private void enterScope() { scopes.push(new HashMap<>()); }
    private void exitScope() { scopes.pop(); }

    private void declare(String name, Symbol sym, Location loc) throws MinicException {
        Map<String, Symbol> current = scopes.peek();
        if (current.containsKey(name)) {
            error(loc, "Redeclaration: " + name);
        }
        current.put(name, sym);
    }

    private Symbol lookup(String name, Location loc) throws MinicException {
        for (Map<String, Symbol> scope : scopes) {
            if (scope.containsKey(name)) return scope.get(name);
        }
        error(loc, "Undeclared identifier: " + name);
        return null;
    }

    private void error(Location location, String msg) throws MinicException {
        throw new MinicException(location, msg);
    }

    private void error(Location location, String fmt, Object... args) throws MinicException {
        throw new MinicException(location, fmt, args);
    }
}
