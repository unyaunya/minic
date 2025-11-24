package com.unyaunya.minic.frontend;

import lombok.Value;

@Value
public class AddressOf implements Expr {
    String name;
}