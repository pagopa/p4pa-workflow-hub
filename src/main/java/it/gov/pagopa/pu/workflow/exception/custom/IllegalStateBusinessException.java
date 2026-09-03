package it.gov.pagopa.pu.workflow.exception.custom;

import it.gov.pagopa.payhub.activities.exception.common.BaseBusinessException;

public class IllegalStateBusinessException extends BaseBusinessException {
  public IllegalStateBusinessException(String code, String message) {
    this(code, message, null);
  }

  public IllegalStateBusinessException(String code, String message, Throwable cause) {
    super(code, message, cause);
  }
}
