package com.unyaunya.minic.semantics;

import com.unyaunya.minic.frontend.TypeSpec;
import lombok.Value;

/**
 * Represents a symbol in the symbol table.
 * Stores type information and can be extended for offsets, etc.
 */
@Value
public class Symbol {
    TypeSpec type;
    StorageClass storageClass;
    int offset; // For PARAM and LOCAL: offset from GR7
}
