package it.gov.pagopa.pu.workflow.exception.custom;

import it.gov.pagopa.pu.workflow.utilities.ErrorCodeConstants;

public class TooManyAttemptsException extends BaseBusinessException {

  public TooManyAttemptsException(String message) {
        super(ErrorCodeConstants.ERROR_CODE_TOO_MANY_ATTEMPTS, message);
    }
}
