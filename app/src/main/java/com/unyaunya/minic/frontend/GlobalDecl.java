package com.unyaunya.minic.frontend;

public class GlobalDecl implements Node {
    public final TypeSpec type;
    public final String name;
    public final Integer arraySize; // null if not array
    public GlobalDecl(TypeSpec type, String name, Integer arraySize) {
        this.type = type; this.name = name; this.arraySize = arraySize;
    }
}