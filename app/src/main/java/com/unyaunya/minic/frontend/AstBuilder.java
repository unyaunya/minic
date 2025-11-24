// frontend/AstBuilder.java
package com.unyaunya.minic.frontend;

import com.unyaunya.minic.parser.*;
import com.unyaunya.minic.parser.MiniCParser.ArraySizeContext;
import com.unyaunya.minic.parser.MiniCParser.StatementContext;
import org.antlr.v4.runtime.tree.TerminalNode;

import java.util.*;

public class AstBuilder extends MiniCBaseVisitor<Node> {

    // ----------------------
    // Entry point
    // ----------------------
    @Override
    public Node visitProgram(MiniCParser.ProgramContext ctx) {
        Program p = new Program();
        for (MiniCParser.FunctionDeclContext f : ctx.functionDecl()) {
            p.getFunctions().add((FunctionDecl) visit(f));
        }
        for (MiniCParser.GlobalDeclContext g : ctx.globalDecl()) {
            p.getGlobals().add((GlobalDecl) visit(g));
        }
        return p;
    }

    // ----------------------
    // Types
    // ----------------------
    private BaseType toBaseType(MiniCParser.BaseTypeContext t) {
        if (t.VOID() != null) return BaseType.VOID;
        if (t.INT() != null)  return BaseType.INT;
        if (t.SHORT() != null) return BaseType.SHORT;
        if (t.CHAR() != null) return BaseType.CHAR;
        throw new IllegalArgumentException("Unknown base type: " + t.getText());
    }

    private TypeSpec toTypeSpec(MiniCParser.TypeSpecContext t) {
        BaseType baseType = toBaseType(t.baseType());
        int pointerDepth = (t.pointer() == null) ? 0 : t.pointer().getText().length(); // single '*' for now
        int arraySize = 0; // locals/globals can carry array via separate field (GlobalDecl), keep 0 here unless you encode it in TypeSpec
        return new TypeSpec(baseType, pointerDepth, arraySize);
    }

    private int toInt(TerminalNode n) {
        return Integer.parseInt(n.getText());
    }

    // ----------------------
    // Globals
    // ----------------------
    @Override
    public Node visitGlobalDecl(MiniCParser.GlobalDeclContext ctx) {
        String name = ctx.IDENT().getText();
        TypeSpec type = toTypeSpec(ctx.typeSpec());
        ArraySizeContext asc = ctx.arraySize();
        int arraySize = (asc == null) ? 0 : toInt(asc.INTEGER());
        return new GlobalDecl(type, name, arraySize);
    }

    // ----------------------
    // Functions
    // ----------------------
    @Override
    public Node visitFunctionDecl(MiniCParser.FunctionDeclContext ctx) {
        TypeSpec retType = toTypeSpec(ctx.typeSpec());
        String name = ctx.IDENT().getText();

        List<Param> params = new ArrayList<>();
        if (ctx.paramList() != null) {
            for (MiniCParser.ParamContext pc : ctx.paramList().param()) {
                params.add(visitParamToAst(pc));
            }
        }

        Block body = (Block) visit(ctx.block());
        return new FunctionDecl(retType, name, params, body);
    }

    private Param visitParamToAst(MiniCParser.ParamContext pc) {
        TypeSpec t = toTypeSpec(pc.typeSpec());
        String name = pc.IDENT().getText();
        return new Param(t, name);
    }

    // ----------------------
    // Blocks and statements
    // ----------------------
    @Override
    public Block visitBlock(MiniCParser.BlockContext b) {
        Block block = new Block();
        for (MiniCParser.StatementContext s : b.statement()) {
            block.getStatements().add((Stmt) visit(s));
        }
        return block;
    }

    @Override
    public Node visitStatement(MiniCParser.StatementContext ctx) {
        if (ctx.varDecl() != null)    return visit(ctx.varDecl());
        if (ctx.assignment() != null) return visit(ctx.assignment());
        if (ctx.ifStmt() != null)     return visit(ctx.ifStmt());
        if (ctx.whileStmt() != null)  return visit(ctx.whileStmt());
        if (ctx.forStmt() != null)    return visit(ctx.forStmt());
        if (ctx.block() != null)      return visit(ctx.block());
        if (ctx.returnStmt() != null) return visit(ctx.returnStmt());
        if (ctx.expr() != null)       return new ExprStmt((Expr) visit(ctx.expr()));
        throw new IllegalStateException("Unknown statement alternative: " + ctx.getText());
    }

    private Block toBlock(MiniCParser.StatementContext ctx) {
        if (ctx.block() != null) {
            return (Block) visitBlock(ctx.block());
        } else {
            Block block = new Block();
            block.getStatements().add((Stmt) visitStatement(ctx));
            return block;
        }
    }

    @Override
    public Node visitVarDecl(MiniCParser.VarDeclContext ctx) {
        String name = ctx.IDENT().getText();
        TypeSpec type = toTypeSpec(ctx.typeSpec());
        // If you want local arrays encoded in TypeSpec, you can read ctx.arraySize() here and update TypeSpec accordingly.
        Expr init = (ctx.expr() == null) ? null : (Expr) visit(ctx.expr());
        return new VarDecl(type, name, init);
    }

    @Override
    public Node visitAssignment(MiniCParser.AssignmentContext ctx) {
        return new Assign((LValue) visit(ctx.lvalue()), (Expr) visit(ctx.expr()));
    }

    @Override
    public Node visitReturnStmt(MiniCParser.ReturnStmtContext ctx) {
        Expr v = (ctx.expr() == null) ? null : (Expr) visit(ctx.expr());
        return new ReturnStmt(v);
    }

    @Override
    public Node visitIfStmt(MiniCParser.IfStmtContext ctx) {
        Expr cond = (Expr) visit(ctx.expr());
        List<StatementContext> statements = ctx.statement();
        Block thenBlock = toBlock(statements.get(0));
        Block elseBlock = null;
        if (statements.size() > 1) {
            elseBlock = toBlock(statements.get(1));
        }
        return new IfStmt(cond, thenBlock, elseBlock);
    }

    @Override
    public Node visitWhileStmt(MiniCParser.WhileStmtContext ctx) {
        return new WhileStmt((Expr) visit(ctx.expr()), toBlock(ctx.statement()));
    }

    @Override
    public Node visitForStmt(MiniCParser.ForStmtContext ctx) {
        Stmt init = (ctx.forInit() == null) ? null : (Stmt) visit(ctx.forInit());
        Expr cond = (ctx.expr() == null) ? null : (Expr) visit(ctx.expr());
        Stmt update = (ctx.forUpdate() == null) ? null : (Stmt) visit(ctx.forUpdate());
        Block body = toBlock(ctx.statement());
        return new ForStmt(init, cond, update, body);
    }

    @Override
    public Node visitForInit(MiniCParser.ForInitContext ctx) {
        if (ctx.varDecl() != null)    return visit(ctx.varDecl());
        if (ctx.assignment() != null) return visit(ctx.assignment());
        throw new IllegalStateException("Unknown forInit: " + ctx.getText());
    }

    @Override
    public Node visitForUpdate(MiniCParser.ForUpdateContext ctx) {
        return visit(ctx.assignment());
    }

    // ----------------------
    // LValues
    // ----------------------
    @Override
    public Node visitLvVar(MiniCParser.LvVarContext ctx) {
        return new LvVar(ctx.IDENT().getText());
    }

    @Override
    public Node visitLvArrayElem(MiniCParser.LvArrayElemContext ctx) {
        return new LvArrayElem(ctx.IDENT().getText(), (Expr) visit(ctx.expr()));
    }

    @Override
    public Node visitLvPtrDeref(MiniCParser.LvPtrDerefContext ctx) {
        return new LvPtrDeref((Expr) visit(ctx.expr()));
    }

    // ----------------------
    // Expressions
    // ----------------------
    @Override
    public Node visitMulDiv(MiniCParser.MulDivContext ctx) {
        Expr left = (Expr) visit(ctx.expr(0));
        Expr right = (Expr) visit(ctx.expr(1));
        Binary.Op op = ctx.op.getText().equals("*") ? Binary.Op.MUL : Binary.Op.DIV;
        return new Binary(op, left, right);
    }

    @Override
    public Node visitAddSub(MiniCParser.AddSubContext ctx) {
        Expr left = (Expr) visit(ctx.expr(0));
        Expr right = (Expr) visit(ctx.expr(1));
        Binary.Op op = ctx.op.getText().equals("+") ? Binary.Op.ADD : Binary.Op.SUB;
        return new Binary(op, left, right);
    }

    @Override
    public Node visitCompare(MiniCParser.CompareContext ctx) {
        Expr left = (Expr) visit(ctx.expr(0));
        Expr right = (Expr) visit(ctx.expr(1));
        Binary.Op op = switch (ctx.op.getText()) {
            case "<"  -> Binary.Op.LT;
            case ">"  -> Binary.Op.GT;
            case "<=" -> Binary.Op.LE;
            case ">=" -> Binary.Op.GE;
            case "==" -> Binary.Op.EQ;
            case "!=" -> Binary.Op.NE;
            default -> throw new IllegalArgumentException("Unknown compare op: " + ctx.op.getText());
        };
        return new Binary(op, left, right);
    }

    @Override
    public Node visitParen(MiniCParser.ParenContext ctx) {
        return visit(ctx.expr());
    }

    @Override
    public Node visitUnaryNeg(MiniCParser.UnaryNegContext ctx) {
        return new UnaryNeg((Expr) visit(ctx.expr()));
    }

    @Override
    public Node visitAddressOf(MiniCParser.AddressOfContext ctx) {
        return new AddressOf(ctx.IDENT().getText());
    }

    @Override
    public Node visitDeref(MiniCParser.DerefContext ctx) {
        // As an expression, deref can reuse the same node class as lvalue deref if it implements Expr
        return new LvPtrDeref((Expr) visit(ctx.expr()));
    }

    @Override
    public Node visitArrayAccess(MiniCParser.ArrayAccessContext ctx) {
        // As an expression, array access can reuse the lvalue class if it implements Expr
        return new LvArrayElem(ctx.IDENT().getText(), (Expr) visit(ctx.expr()));
    }

    @Override
    public Node visitFuncCall(MiniCParser.FuncCallContext ctx) {
        String name = ctx.IDENT().getText();
        List<Expr> args = new ArrayList<>();
        if (ctx.expr() != null) {
            for (var e : ctx.expr()) args.add((Expr) visit(e));
        }
        return new Call(name, args);
    }

    @Override
    public Node visitVarRef(MiniCParser.VarRefContext ctx) {
        return new VarRef(ctx.IDENT().getText());
    }

    @Override
    public Node visitIntLit(MiniCParser.IntLitContext ctx) {
        return new IntLit(Integer.parseInt(ctx.INTEGER().getText()));
    }
}
