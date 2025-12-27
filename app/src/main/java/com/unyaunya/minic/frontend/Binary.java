package com.unyaunya.minic.frontend;

import com.unyaunya.minic.Location;
import lombok.Getter;


@Getter
public class Binary extends ExprNode {
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

    public Binary(Location location, Op op, Expr left, Expr right) {
        super(location);
        this.op = op;
        this.left = left;
        this.right = right;
    }

    public String toString() {
        return String.format("%s%s%s", left.toString(), op.toString(), right.toString());
    }
}