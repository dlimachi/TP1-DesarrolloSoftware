package edu.itba.class2.exchange.exception;

public class CouldNotLoadPropertiesException extends RuntimeException {
    public CouldNotLoadPropertiesException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
