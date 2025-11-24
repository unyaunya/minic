package com.unyaunya.minic.frontend;

import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public class LvArrayElem implements LValue {
    private String name;
    private Expr expr;
}
