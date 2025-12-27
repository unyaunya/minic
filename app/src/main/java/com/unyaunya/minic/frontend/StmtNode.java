package com.unyaunya.minic.frontend;

import com.unyaunya.minic.Location;

/**
 * Base class for statement nodes that provides location tracking.
 */
public abstract class StmtNode implements Stmt {
    protected final Location location;

    protected StmtNode(Location location) {
        this.location = location;
    }

    @Override
    public Location getLocation() {
        return location;
    }
}
