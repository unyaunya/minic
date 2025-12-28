package com.unyaunya.minic.ast;
import lombok.Getter;

import com.unyaunya.minic.Location;


@Getter
public class IntLit extends ExprNode {
    int value;

    public IntLit(Location location, int value) {
        super(location);
        this.value = value;
    }

    public String toString() {
        return Integer.toString(value);
    }
}