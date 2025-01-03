package it.gov.pagopa.pu.workflow.utilities.exception;

import it.gov.pagopa.payhub.activities.exception.NotRetryableActivityException;

public class ExtendedNotRetryableActivityException extends NotRetryableActivityException {
    public ExtendedNotRetryableActivityException(String message) {
        super(message);
    }
}
