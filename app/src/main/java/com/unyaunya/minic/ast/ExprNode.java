package com.unyaunya.minic.ast;

import com.unyaunya.minic.Location;

/**
 * Base class for expression nodes that provides location tracking.
 */
public abstract class ExprNode implements Expr {
    protected final Location location;

    protected ExprNode(Location location) {
        this.location = location;
    }

    @Override
    public Location getLocation() {
        return location;
    }
}
