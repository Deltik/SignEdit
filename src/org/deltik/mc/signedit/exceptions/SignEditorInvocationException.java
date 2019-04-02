package org.deltik.mc.signedit.exceptions;

public class SignEditorInvocationException extends RuntimeException {
    private final Exception originalException;

    public SignEditorInvocationException(Exception originalException) {
        this.originalException = originalException;
    }

    public Exception getOriginalException() {
        return originalException;
    }
}
