package com.unyaunya.minic.frontend;

public enum BaseType {
    VOID("void"),
    INT("int"),
    SHORT("short"),
    CHAR("char");

    private String symbol;
    BaseType(String symbol) {
        this.symbol = symbol;
    }
    public String toString() {
        return symbol;
    }
}
