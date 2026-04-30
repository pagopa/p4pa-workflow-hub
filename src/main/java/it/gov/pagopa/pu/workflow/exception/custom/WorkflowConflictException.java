package it.gov.pagopa.pu.workflow.exception.custom;

import it.gov.pagopa.pu.workflow.utilities.ErrorCodeConstants;

/**
 * A custom exception that represents a not found workflow and extends {@link RuntimeException}.
 *
 */
public class WorkflowConflictException extends BaseBusinessException {

  /**
   * Constructs a new {@code WorkflowConflictException} with the specified detail message.
   *
   * @param message the detail message explaining the cause of the exception.
   */
  public WorkflowConflictException(String message) {
    super(ErrorCodeConstants.ERROR_CODE_WF_ALREADY_EXISTS, message);
  }
}
