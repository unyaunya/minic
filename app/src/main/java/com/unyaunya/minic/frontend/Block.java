package com.unyaunya.minic.frontend;

import java.util.ArrayList;
import java.util.List;

import lombok.Value;

@Value
public class Block implements Node {
    List<Stmt> statements = new ArrayList<>();    
}
