package com.unyaunya.minic.ast;

import com.unyaunya.minic.Location;

/**
 * Base interface for all AST nodes.
 * All nodes can have location information for error reporting.
 */
public interface Node {
    /**
     * Gets the location of this node in the source code.
     * @return the location, or null if not available
     */
    default Location getLocation() {
        return null;
    }
}
