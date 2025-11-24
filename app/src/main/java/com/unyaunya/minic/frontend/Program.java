package com.unyaunya.minic.frontend;

import java.util.*;

public class Program implements Node {
    public final List<GlobalDecl> globals = new ArrayList<>();    
    public final List<FunctionDecl> functions = new ArrayList<>();
}