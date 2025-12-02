package com.unyaunya.minic.semantics;

public enum StorageClass {
    GLOBAL,    // Declared at the top level, stored as a label in memory
    PARAM,     // Function parameter, passed via stack
    LOCAL      // Local variable inside a function, stored at offset from frame pointer (GR7)
}
