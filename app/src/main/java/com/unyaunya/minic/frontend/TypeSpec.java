package com.unyaunya.minic.frontend;

import lombok.Value;

@Value
public class TypeSpec {
    BaseType baseType;
    int pointerDepth;   // 0 = scalar, 1 = *, 2 = **, etc.
    Integer arraySize;  // null if not array
}
