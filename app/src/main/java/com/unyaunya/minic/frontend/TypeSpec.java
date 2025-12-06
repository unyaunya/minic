package com.unyaunya.minic.frontend;

import lombok.Value;

@Value
public class TypeSpec {
    BaseType baseType;
    int pointerDepth;   // 0 = scalar, 1 = *, 2 = **, etc.
    Integer arraySize;  // null if not array
    public int getSize() {
        return (arraySize == null) ? 1 : arraySize.intValue();
    }

    public String toString() {
        return String.format("%s%s", baseType.toString(), "*".repeat(pointerDepth));
    }
}
