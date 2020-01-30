package com.trmsys.clock.clockapplication.exceptions;

public class JwksException extends RuntimeException {

    private static final String INVALID_JWKS_EXCEPTION = "JWKS exception: ";
    private static final long serialVersionUID = -7708447901493903974L;

    public JwksException(Exception e) {
        super(INVALID_JWKS_EXCEPTION, e);
    }

    public JwksException(String message) {
        super(INVALID_JWKS_EXCEPTION + message);
    }
}
