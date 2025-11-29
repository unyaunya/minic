package com.unyaunya.minic.frontend;

import lombok.Value;

@Value
public class LvVar implements LValue {
    String name;

    public String toString() {
        return name;
    }
}
