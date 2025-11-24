package com.unyaunya.minic.frontend;

public class LvVar implements LValue {
    public final String name;
    public LvVar(String name) {
        this.name = name;
    }
}
