
package com.unyaunya.minic.semantics;

import com.unyaunya.minic.frontend.TypeSpec;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Represents a symbol in the symbol table.
 * Stores type information and can be extended for offsets, etc.
 */
@Data
@AllArgsConstructor
public class Symbol {
    private TypeSpec type;
}
