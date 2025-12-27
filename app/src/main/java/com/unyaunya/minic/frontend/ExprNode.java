package com.unyaunya.minic.frontend;

import com.unyaunya.minic.Location;

/**
 * Base class for expression nodes that provides location tracking.
 */
public abstract class ExprNode implements Expr {
    protected final Location location;

    public ExprNode(Location location) {
        this.location = location;
    }

    @Override
    public Location getLocation() {
        return location;
    }
}
