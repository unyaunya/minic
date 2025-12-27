package com.unyaunya.minic.frontend;
import lombok.Getter;

import com.unyaunya.minic.Location;


@Getter
public class LvVar extends ExprNode implements LValue {
    String name;

    public LvVar(Location location, String name) {
        super(location);
        this.name = name;
    }

    public String toString() {
        return name;
    }
}