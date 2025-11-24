package com.unyaunya.minic.frontend;

public class Param implements Node {
    public final String name; public final TypeSpec type;
    public Param(String name, TypeSpec type) { this.name = name; this.type = type; }
}
