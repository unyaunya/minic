package com.unyaunya.minic.ast;

import java.util.ArrayList;
import java.util.List;

import lombok.Value;

@Value
public class Block implements Stmt {
    List<Stmt> statements = new ArrayList<>();    
}
