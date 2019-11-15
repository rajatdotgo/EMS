package com.rajat.ems.exception;

public class ValidationError extends RuntimeException {

    public final String problem;
    public final String cause;

    public ValidationError(String problem, String cause) {
        super("Validation error in " + problem + " because it is " + cause);
        this.problem = problem;
        this.cause = cause;
    }
}
