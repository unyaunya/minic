package com.unyaunya.minic.frontend;

import lombok.Value;

@Value
public class Binary implements Expr {
    public enum Op {
        ADD, SUB, MUL, DIV, LT, LE, GT, GE, EQ, NE
    }
    Op op;
    Expr left;
    Expr right;
}