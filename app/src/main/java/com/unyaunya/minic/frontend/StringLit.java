package com.unyaunya.minic.frontend;
import lombok.Getter;

import com.unyaunya.minic.Location;


@Getter
public class StringLit extends ExprNode {
    String value;

    public StringLit(Location location, String value) {
        super(location);
        this.value = value;
    }

    public String toString() {
        return String.format("'%s'", this.value);
    }
}