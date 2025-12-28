package com.unyaunya.minic.ast;

import lombok.Value;

@Value
public class GlobalDecl implements Node {
    TypeSpec type;
    String name;
}