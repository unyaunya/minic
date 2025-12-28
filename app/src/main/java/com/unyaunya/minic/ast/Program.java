package com.unyaunya.minic.ast;

import java.util.*;

import lombok.Value;

@Value
public class Program implements Node {
    List<GlobalDecl> globals = new ArrayList<>();    
    List<FunctionDecl> functions = new ArrayList<>();
}