package com.unyaunya.minic;

import lombok.Value;

/**
 * Represents the location of a source code element.
 * Used for error reporting.
 */
@Value
public class Location {
    String filename;
    int lineNumber;

    public Location(String filename, int lineNumber) {
        this.filename = filename;
        this.lineNumber = lineNumber;
    }

    @Override
    public String toString() {
        if (filename != null && !filename.isEmpty()) {
            return String.format("%s:%d", filename, lineNumber);
        } else {
            return String.format("line %d", lineNumber);
        }
    }
}
