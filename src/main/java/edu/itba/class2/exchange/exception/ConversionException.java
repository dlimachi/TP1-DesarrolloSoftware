package edu.itba.class2.exchange.exception;

public abstract class ConversionException extends RuntimeException {
    public ConversionException(String message) {
        super(message);
    }

    public ConversionException() {
        super();
    }
}
