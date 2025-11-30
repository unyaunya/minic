package com.unyaunya.minic.frontend;

import lombok.Value;

@Value
public class GlobalDecl implements Node {
    TypeSpec type;
    String name;
}