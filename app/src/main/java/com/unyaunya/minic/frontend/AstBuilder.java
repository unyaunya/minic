// frontend/AstBuilder.java
package com.unyaunya.minic.frontend;

import com.unyaunya.minic.frontend.Assign;
import com.unyaunya.minic.frontend.Binary;
import com.unyaunya.minic.frontend.Call;
import com.unyaunya.minic.frontend.IfStmt;
import com.unyaunya.minic.frontend.IntLit;
import com.unyaunya.minic.frontend.ReturnStmt;
import com.unyaunya.minic.frontend.VarDecl;
import com.unyaunya.minic.frontend.VarRef;
import com.unyaunya.minic.frontend.WhileStmt;
import com.unyaunya.minic.parser.*;
import com.unyaunya.minic.parser.MiniCParser.ArraySizeContext;
import com.unyaunya.minic.parser.MiniCParser.StatementContext;

import org.antlr.v4.runtime.tree.TerminalNode;
import java.util.*;

public class AstBuilder extends MiniCBaseVisitor<Node> {

    @Override
    public Node visitProgram(MiniCParser.ProgramContext ctx) {
        Program p = new Program();
        for (MiniCParser.FunctionDeclContext f : ctx.functionDecl()) {
            p.functions.add((FunctionDecl) visit(f));
        }
        for (MiniCParser.GlobalDeclContext f : ctx.globalDecl()) {
            p.globals.add((GlobalDecl) visit(f));
        }
        return p;
    }

    private BaseType toBaseType(MiniCParser.BaseTypeContext t) {
        if (t.VOID() != null) return BaseType.VOID;
        if (t.INT() != null) return BaseType.INT;
        if (t.SHORT() != null) return BaseType.SHORT;
        if (t.CHAR() != null) return BaseType.CHAR;
        throw new IllegalArgumentException("Unknown base type: " + t.getText());
    }

    private TypeSpec toTypeSpec(MiniCParser.TypeSpecContext t) {
        BaseType baseType = toBaseType(t.baseType());
        int pointerDepth = (t.pointer() == null) ? 0 : t.pointer().getText().length();
        int arraySize = 0;
        return new TypeSpec(baseType, pointerDepth, arraySize);
    }

    private int toInt(TerminalNode ctx) {
        return Integer.parseInt(ctx.getText());
    }

    @Override
    public Node visitGlobalDecl(MiniCParser.GlobalDeclContext ctx) {
        String name = ctx.IDENT().getText();
        TypeSpec type = toTypeSpec(ctx.typeSpec());
        ArraySizeContext asc = ctx.arraySize();
        int arraySize = (asc == null) ? 0 : toInt(asc.INTEGER());
        ctx.arraySize();
        return new GlobalDecl(type, name, arraySize);
    }


    @Override
    public Block visitBlock(MiniCParser.BlockContext b) {
        Block block = new Block();
        for (MiniCParser.StatementContext s : b.statement()) {
            block.statements.add((Stmt) visit(s));
        }
        return block;
    }

    @Override
    public Node visitVarDecl(MiniCParser.VarDeclContext ctx) {
        String name = ctx.IDENT().getText();
        TypeSpec type = toTypeSpec(ctx.typeSpec());
        Expr init = ctx.expr() == null ? null : (Expr)visit(ctx.expr());
        return new VarDecl(name, type, init);
    }

    @Override
    public Node visitLvVar(MiniCParser.LvVarContext ctx) {
        return new LvVar(ctx.IDENT().getText());
    }

    @Override
    public Node visitLvArrayElem(MiniCParser.LvArrayElemContext ctx) {
        return new LvArrayElem(ctx.IDENT().getText(), (Expr)visit(ctx.expr()));
    }

    @Override
    public Node visitLvPtrDeref(MiniCParser.LvPtrDerefContext ctx) {
        return new LvPtrDeref((Expr)visit(ctx.expr()));
    }

    @Override
    public Node visitAssignment(MiniCParser.AssignmentContext ctx) {
        return new Assign((LValue)ctx.lvalue(), (Expr)visit(ctx.expr()));
    }

    @Override
    public Node visitReturnStmt(MiniCParser.ReturnStmtContext ctx) {
        Expr v = ctx.expr() == null ? null : (Expr) visit(ctx.expr());
        return new ReturnStmt(v);
    }

    private Block toBlock(MiniCParser.StatementContext ctx) {
        if (ctx.block() != null) {
            return (Block) visitBlock(ctx.block());
        } else {
            Block block = new Block();
            block.statements.add((Stmt)visitStatement(ctx));
            return block;
        }
    }

    @Override
    public Node visitIfStmt(MiniCParser.IfStmtContext ctx) {
        Expr cond = (Expr) visit(ctx.expr());
        List<StatementContext> statements = ctx.statement();
        Block thenBlock = toBlock(statements.get(0));
        Block elseBlock = null;
        if(statements.size() >= 1) {
            elseBlock = toBlock(statements.get(1));
        }
        return new IfStmt(cond, thenBlock, elseBlock);
    }

    @Override
    public Node visitWhileStmt(MiniCParser.WhileStmtContext ctx) {
        return new WhileStmt((Expr) visit(ctx.expr()), toBlock(ctx.statement()));
    }

    /**


    @Override
    public Node visitCallExpr(MiniCParser.CallExprContext ctx) {
        String name = ctx.ID().getText();
        List<Expr> args = new ArrayList<>();
        for (MiniCParser.ExprContext e : ctx.argList().expr()) args.add((Expr) visit(e));
        return new Call(name, args);
    }

    @Override
    public Node visitParenExpr(MiniCParser.ParenExprContext ctx) {
        return visit(ctx.expr());
    }

    @Override
    public Node visitIntLiteral(MiniCParser.IntLiteralContext ctx) {
        return new IntLit(Integer.parseInt(ctx.INT().getText()));
    }

    @Override
    public Node visitVarExpr(MiniCParser.VarExprContext ctx) {
        return new VarRef(ctx.ID().getText());
    }

    @Override
    public Node visitBinaryExpr(MiniCParser.BinaryExprContext ctx) {
        Expr left = (Expr) visit(ctx.expr(0));
        Expr right = (Expr) visit(ctx.expr(1));
        Binary.Op op = switch (ctx.op.getText()) {
            case "+" -> Binary.Op.ADD; case "-" -> Binary.Op.SUB;
            case "*" -> Binary.Op.MUL; case "/" -> Binary.Op.DIV;
            case "<" -> Binary.Op.LT;  case "<=" -> Binary.Op.LE;
            case ">" -> Binary.Op.GT;  case ">=" -> Binary.Op.GE;
            case "==" -> Binary.Op.EQ; case "!=" -> Binary.Op.NE;
            default -> throw new IllegalArgumentException("Unknown op: " + ctx.op.getText());
        };
        return new Binary(op, left, right);
    }
     */
}
