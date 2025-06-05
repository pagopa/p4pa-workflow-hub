package it.gov.pagopa.pu.workflow.exception.custom;

public class TooManyAttemptsException extends RuntimeException {

    public TooManyAttemptsException(String message) {
        super(message);
    }
}
