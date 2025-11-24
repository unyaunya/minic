package com.unyaunya.minic.frontend;

public class TypeSpec {
    public final BaseType base;
    public final int pointerDepth;   // 0 = scalar, 1 = *, 2 = **, etc.
    public final Integer arraySize;  // null if not array

    public TypeSpec(BaseType base, int pointerDepth, Integer arraySize) {
        this.base = base;
        this.pointerDepth = pointerDepth;
        this.arraySize = arraySize;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(base.toString().toLowerCase());
        for (int i = 0; i < pointerDepth; i++) sb.append("*");
        if (arraySize != null) sb.append("[").append(arraySize).append("]");
        return sb.toString();
    }
}
