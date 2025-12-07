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

    public TypeSpec(BaseType baseType) {
        this(baseType, 0);
    }

    public TypeSpec(BaseType baseType, int pointerDepth) {
        this(baseType, pointerDepth, (Integer)null);
    }

    public TypeSpec(BaseType baseType, int pointerDepth, Integer arraySize) {
        this.baseType = baseType;
        this.pointerDepth = pointerDepth;
        this.arraySize = arraySize;
    }

    public String toString() {
        return String.format("%s%s", baseType.toString(), "*".repeat(pointerDepth));
    }

    public boolean equalType(TypeSpec t) {
        if (!this.baseType.equals(t.getBaseType())) {
            return false;
        }
        if (this.arraySize != null && t.getArraySize() == null) {
            return false;
        }
        if (this.arraySize == null && t.getArraySize() != null) {
            return false;
        }
        return true;
    }
}
