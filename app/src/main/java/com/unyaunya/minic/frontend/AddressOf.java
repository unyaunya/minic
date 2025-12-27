package com.unyaunya.minic.frontend;
import lombok.Getter;

import com.unyaunya.minic.Location;


@Getter
public class AddressOf extends ExprNode {
    String name;

    public AddressOf(Location location, String name) {
        super(location);
        this.name = name;
    }
}