// frontend/AstBuilder.java
package com.unyaunya.minic.backend;

import java.util.List;

import com.unyaunya.minic.Location;
import com.unyaunya.minic.ast.*;
import com.unyaunya.minic.parser.*;
import com.unyaunya.minic.parser.MiniCParser.ArraySizeContext;
import com.unyaunya.minic.parser.MiniCParser.StatementContext;
import com.unyaunya.minic.preprocess.Preprocessor;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.TerminalNode;

import java.util.*;

public class AstBuilder extends MiniCBaseVisitor<Node> {
    private final Preprocessor.Result preprocessed;

    public AstBuilder(Preprocessor.Result preprocessed) {
        this.preprocessed = preprocessed;
    }

    // ----------------------
    // Helper to extract location from ANTLR context
    // ----------------------
    private Location getLocation(ParserRuleContext ctx) {
        return this.preprocessed.getLocation(ctx.getStart().getLine());
    }

    private Location getLocation(TerminalNode node) {
        return this.preprocessed.getLocation(node.getSymbol().getLine());
    }

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

    private TypeSpec toTypeSpec(MiniCParser.TypeSpecContext t, Integer arraySize) {
        BaseType baseType = toBaseType(t.baseType());
        int pointerDepth = (t.pointer() == null) ? 0 : t.pointer().getText().length(); // single '*' for now
        return new TypeSpec(baseType, pointerDepth, arraySize);
    }

    private TypeSpec toTypeSpec(MiniCParser.TypeSpecContext t) {
        return toTypeSpec(t, null);
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
        ArraySizeContext asc = ctx.arraySize();
        Integer arraySize = (asc == null) ? null : toInt(asc.INTEGER());
        TypeSpec type = toTypeSpec(ctx.typeSpec(), arraySize);
        return new GlobalDecl(type, name);
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
        if (ctx.macroStmt() != null)  return visit(ctx.macroStmt());
        if (ctx.expr() != null)       return new ExprStmt(getLocation(ctx), (Expr) visit(ctx.expr()));
        throw new IllegalStateException("Unknown statement alternative: " + ctx.getText());
    }

    private Block toBlock(MiniCParser.StatementContext ctx) {
        if (ctx.block() != null) {
            return visitBlock(ctx.block());
        } else {
            Block block = new Block();
            block.getStatements().add((Stmt) visitStatement(ctx));
            return block;
        }
    }

    @Override
    public Node visitVarDecl(MiniCParser.VarDeclContext ctx) {
        String name = ctx.IDENT().getText();
        ArraySizeContext asc = ctx.arraySize();
        Integer arraySize = (asc == null) ? null : toInt(asc.INTEGER());
        TypeSpec type = toTypeSpec(ctx.typeSpec(), arraySize);
        // If you want local arrays encoded in TypeSpec, you can read ctx.arraySize() here and update TypeSpec accordingly.
        Expr init = (ctx.expr() == null) ? null : (Expr) visit(ctx.expr());
        return new VarDecl(getLocation(ctx), type, name, init);
    }

    @Override
    public Node visitAssignment(MiniCParser.AssignmentContext ctx) {
        return new Assign(getLocation(ctx), (LValue) visit(ctx.lvalue()), (Expr) visit(ctx.expr()));
    }

    @Override
    public Node visitReturnStmt(MiniCParser.ReturnStmtContext ctx) {
        Expr v = (ctx.expr() == null) ? null : (Expr) visit(ctx.expr());
        return new ReturnStmt(getLocation(ctx), v);
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
        return new IfStmt(getLocation(ctx), cond, thenBlock, elseBlock);
    }

    @Override
    public Node visitWhileStmt(MiniCParser.WhileStmtContext ctx) {
        return new WhileStmt(getLocation(ctx), (Expr) visit(ctx.expr()), toBlock(ctx.statement()));
    }

    @Override
    public Node visitForStmt(MiniCParser.ForStmtContext ctx) {
        Stmt init = (ctx.forInit() == null) ? null : (Stmt) visit(ctx.forInit());
        Expr cond = (ctx.expr() == null) ? null : (Expr) visit(ctx.expr());
        Stmt update = (ctx.forUpdate() == null) ? null : (Stmt) visit(ctx.forUpdate());
        Block body = toBlock(ctx.statement());
        return new ForStmt(getLocation(ctx), init, cond, update, body);
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

    @Override
    public Node visitMacroStmt(MiniCParser.MacroStmtContext ctx) {
        return new MacroStmt(getLocation(ctx), ctx.MACRO().getText());
    }


    // ----------------------
    // LValues
    // ----------------------
    @Override
    public Node visitLvVar(MiniCParser.LvVarContext ctx) {
        return new LvVar(getLocation(ctx), ctx.IDENT().getText());
    }

    @Override
    public Node visitLvArrayElem(MiniCParser.LvArrayElemContext ctx) {
        return new LvArrayElem(getLocation(ctx), ctx.IDENT().getText(), (Expr) visit(ctx.expr()));
    }

    @Override
    public Node visitLvPtrDeref(MiniCParser.LvPtrDerefContext ctx) {
        return new LvPtrDeref(getLocation(ctx), (Expr) visit(ctx.expr()));
    }

    // ----------------------
    // Expressions
    // ----------------------
    @Override
    public Node visitMulDiv(MiniCParser.MulDivContext ctx) {
        Expr left = (Expr) visit(ctx.expr(0));
        Expr right = (Expr) visit(ctx.expr(1));
        Binary.Op op = ctx.op.getText().equals("*") ? Binary.Op.MUL : Binary.Op.DIV;
        return new Binary(getLocation(ctx), op, left, right);
    }

    @Override
    public Node visitAddSub(MiniCParser.AddSubContext ctx) {
        Expr left = (Expr) visit(ctx.expr(0));
        Expr right = (Expr) visit(ctx.expr(1));
        Binary.Op op = ctx.op.getText().equals("+") ? Binary.Op.ADD : Binary.Op.SUB;
        return new Binary(getLocation(ctx), op, left, right);
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
        return new Binary(getLocation(ctx), op, left, right);
    }

    @Override
    public Node visitParen(MiniCParser.ParenContext ctx) {
        return visit(ctx.expr());
    }

    @Override
    public Node visitUnaryNeg(MiniCParser.UnaryNegContext ctx) {
        return new UnaryNeg(getLocation(ctx), (Expr) visit(ctx.expr()));
    }

    @Override
    public Node visitAddressOf(MiniCParser.AddressOfContext ctx) {
        return new AddressOf(getLocation(ctx), ctx.IDENT().getText());
    }

    @Override
    public Node visitDeref(MiniCParser.DerefContext ctx) {
        return new PtrDeref(getLocation(ctx), (Expr) visit(ctx.expr()));
    }

    @Override
    public Node visitCast(MiniCParser.CastContext ctx) {
        return new Cast(getLocation(ctx), toTypeSpec(ctx.typeSpec()), (Expr) visit(ctx.expr()));
    }

    @Override
    public Node visitArrayAccess(MiniCParser.ArrayAccessContext ctx) {
        return new ArrayElem(getLocation(ctx), ctx.IDENT().getText(), (Expr) visit(ctx.expr()));
    }

    @Override
    public Node visitFuncCall(MiniCParser.FuncCallContext ctx) {
        String name = ctx.IDENT().getText();
        List<Expr> args = new ArrayList<>();
        if (ctx.expr() != null) {
            for (var e : ctx.expr()) args.add((Expr) visit(e));
        }
        return new Call(getLocation(ctx), name, args);
    }

    @Override
    public Node visitVarRef(MiniCParser.VarRefContext ctx) {
        return new VarRef(getLocation(ctx), ctx.IDENT().getText());
    }

    @Override
    public Node visitIntLit(MiniCParser.IntLitContext ctx) {
        return new IntLit(getLocation(ctx), Integer.parseInt(ctx.INTEGER().getText()));
    }

    @Override
    public Node visitStringLit(MiniCParser.StringLitContext ctx) {
        String text = ctx.getText();  // e.g., "\"hello\\n\""
        if (text.length() < 2 || text.charAt(0) != '"' || text.charAt(text.length() - 1) != '"') {
            throw new IllegalArgumentException("Invalid string literal: " + text);
        }
        String inner = text.substring(1, text.length() - 1);
        String value = unescapeString(inner);
        return new StringLit(getLocation(ctx), value);
    }

    /**
     * Unescapes the content matched by:
     *   ( ~["\\\r\n] | '\\' . )*
     *
     * Note:
     * - The lexer allows ANY escape after backslash because '.' was used.
     * - We choose to be STRICT here and reject unknown escapes to avoid surprises.
     * - If you want permissive behavior, change the 'default' to append the next char.
     */
    private static String unescapeString(String s) {
        StringBuilder out = new StringBuilder(s.length());
        for (int i = 0; i < s.length(); ) {
            char c = s.charAt(i);

            if (c != '\\') {
                // Normal char (the lexer already excluded " \r \n)
                out.append(c);
                i++;
                continue;
            }

            // Escaped sequence: backslash + next char(s)
            if (i + 1 >= s.length()) {
                throw new IllegalArgumentException("Dangling backslash at end of string");
            }

            char esc = s.charAt(i + 1);
            switch (esc) {
                case 'n': out.append('\n'); i += 2; break;
                case 'r': out.append('\r'); i += 2; break;
                case 't': out.append('\t'); i += 2; break;
                case 'b': out.append('\b'); i += 2; break;
                case 'f': out.append('\f'); i += 2; break;
                case '\'': out.append('\''); i += 2; break;
                case '"': out.append('\"'); i += 2; break;
                case '\\': out.append('\\'); i += 2; break;

                // If you decide to support \\uXXXX and/or \\xNN, handle them below.
                case 'u': {
                    // Expect 4 hex digits: \\uXXXX
                    if (i + 6 > s.length()) {
                        throw new IllegalArgumentException("Invalid \\u escape length at index " + i);
                    }
                    String hex = s.substring(i + 2, i + 6);
                    int cp = parseHex(hex, 4);
                    out.append((char) cp); // BMP only; for full Unicode, consider code points/surrogates
                    i += 6;
                    break;
                }
                case 'x': {
                    // Expect 2 hex digits: \xNN
                    if (i + 4 > s.length()) {
                        throw new IllegalArgumentException("Invalid \\x escape length at index " + i);
                    }
                    String hex = s.substring(i + 2, i + 4);
                    int cp = parseHex(hex, 2);
                    out.append((char) cp);
                    i += 4;
                    break;
                }

                default:
                    // STRICT: reject unknown single-char escapes
                    throw new IllegalArgumentException("Unknown escape: \\" + esc);

                    // PERMISSIVE alternative:
                    // out.append(esc);
                    // i += 2;
                    // break;
            }
        }
        return out.toString();
    }

    private static int parseHex(String hex, int expectedDigits) {
        if (hex.length() != expectedDigits) {
            throw new IllegalArgumentException("Expected " + expectedDigits + " hex digits, got: " + hex);
        }
        try {
            return Integer.parseInt(hex, 16);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid hex digits: " + hex, e);
        }
    }

    @Override
    public Node visitCharacterLit(MiniCParser.CharacterLitContext ctx) {
        //return new IntLit(Integer.parseInt(ctx.CHARACTER().getText()));
        // TEXT like: 'a' or '\n' or '\x' etc. from your lexer rule
        String text = ctx.getText();  // includes surrounding single quotes
        // Sanity checks
        if (text.length() < 3 || text.charAt(0) != '\'' || text.charAt(text.length() - 1) != '\'') {
            throw new IllegalArgumentException("Invalid character literal: " + text);
        }
        String inner = text.substring(1, text.length() - 1);  // content between quotes
        // Your lexer guarantees inner is either 1 char or '\' + 1 char
        int codePoint = decodeCharacterInner(inner);
        return new IntLit(getLocation(ctx), codePoint);
    }

    /**
     * Decodes the inner portion of a character literal per your lexer rule:
     * ( ~['\\\r\n] | '\\' . )
     *
     * IMPORTANT: This allows ANY escape of the form '\' + one char (including newline),
     * because the lexer used '.'. If you want stricter validation, enforce it here.
     */
    private static int decodeCharacterInner(String inner) {
        if (inner.isEmpty()) {
            throw new IllegalArgumentException("Empty character literal content");
        }

        if (inner.charAt(0) != '\\') {
            // Plain single code point (not a quote, backslash, or newline per lexer)
            // Return its code point; inner length should be 1 here.
            if (inner.length() != 1) {
                throw new IllegalArgumentException("Too many characters in literal: " + inner);
            }
            return inner.codePointAt(0);
        }

        // Escaped: '\' + one char (per your lexer)
        if (inner.length() != 2) {
            // If you later allow \\uXXXX or \\xNN, change the lexer and expand handling here.
            throw new IllegalArgumentException("Invalid escape sequence in literal: " + inner);
        }

        char esc = inner.charAt(1);
        switch (esc) {
            case '0':  return '\0';
            case 'n':  return '\n';
            case 'r':  return '\r';
            case 't':  return '\t';
            case 'b':  return '\b';
            case 'f':  return '\f';
            case '\'': return '\'';
            case '"':  return '"';
            case '\\': return '\\';
            // If you want to allow other single-char escapes, add cases above.

            // Currently your lexer accepts ANY '.' after '\'.
            // You can either:
            //  - allow everything (return that char as-is), or
            //  - reject unknown escapes to tighten semantics.
            default:
                // Option A (permissive): return the char as-is
                // return esc;

                // Option B (strict): reject unknown escape
                throw new IllegalArgumentException("Unknown escape: \\" + esc);
        }
    }

}
