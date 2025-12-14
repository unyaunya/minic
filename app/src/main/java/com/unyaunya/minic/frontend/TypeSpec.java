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

    public TypeSpec getAddressType() {
        return new TypeSpec(this.getBaseType(), this.getEffectivePointerDepth() + 1);
    }

    public TypeSpec getDerefType() {
        return new TypeSpec(this.getBaseType(), this.getEffectivePointerDepth() - 1);
    }

    public boolean isCompatible(TypeSpec t) {
        return (this.baseType.equals(t.getBaseType()) && this.getEffectivePointerDepth() == t.getEffectivePointerDepth());
    }

    public boolean isSimpleInt() {
        return (this.baseType.equals(BaseType.INT) && this.getEffectivePointerDepth() == 0);
    }

    public int getEffectivePointerDepth() {
        return this.pointerDepth + ((this.arraySize == null) ? 0 : 1);
    }

    public String toString() {
        return String.format("%s%s", baseType.toString(), "*".repeat(pointerDepth));
    }
}
