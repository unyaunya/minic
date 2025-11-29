package com.unyaunya.minic.frontend;

import lombok.Value;

@Value
public class Binary implements Expr {
    public enum Op {
        ADD("+"),
        SUB("-"),
        MUL("*"),
        DIV("/"),
        LT("<"),
        LE("<="),
        GT(">"),
        GE(">="),
        EQ("=="),
        NE("!=");

        private final String symbol;
        Op(String symbol) {
            this.symbol = symbol;
        }
        @Override
        public String toString() {
            return symbol;
        }
    }
    Op op;
    Expr left;
    Expr right;

    public String toString() {
        return String.format("%s%s%s", left.toString(), op.toString(), right.toString());
    }
}