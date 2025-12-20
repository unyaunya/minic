package com.unyaunya.minic;

public class MinicException extends RuntimeException{

    public MinicException(String string) {
        super(string);
    }

    public MinicException(String fmt, Object... args) {
        this(String.format(fmt, args));
    }
}
