package com.unyaunya.minic.frontend;
import lombok.Getter;

import com.unyaunya.minic.Location;


@Getter
public class VarRef extends ExprNode {
    String name;

    public VarRef(Location location, String name) {
        super(location);
        this.name = name;
    }

    public String toString() {
        return name;
    }
}