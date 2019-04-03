package org.deltik.mc.signedit.exceptions;

public class ForbiddenSignEditException extends RuntimeException {
    public ForbiddenSignEditException() {
        super();
    }

    public ForbiddenSignEditException(String s) {
        super(s);
    }
}
