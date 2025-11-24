package com.unyaunya.minic.frontend;

import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public class LvPtrDeref implements LValue {
    private Expr expr;
}
