package com.unyaunya.minic;

public class MinicException extends RuntimeException{
    private Location location;

    public MinicException(String string) {
        super(string);
        this.location = null;
    }

    public MinicException(String string, Location location) {
        super(formatMessage(string, location));
        this.location = location;
    }

    public MinicException(String fmt, Object... args) {
        this(String.format(fmt, args));
    }

    public MinicException(Location location, String fmt, Object... args) {
        this(String.format(fmt, args), location);
    }

    public Location getLocation() {
        return location;
    }

    private static String formatMessage(String message, Location location) {
        if (location != null) {
            return location + ": " + message;
        }
        return message;
    }
}
