package com.unyaunya.minic.frontend;

public class Binary implements Expr {
    public enum Op { ADD, SUB, MUL, DIV, LT, LE, GT, GE, EQ, NE }
    public final Op op; public final Expr left; public final Expr right;
    public Binary(Op op, Expr left, Expr right) { this.op = op; this.left = left; this.right = right; }
}