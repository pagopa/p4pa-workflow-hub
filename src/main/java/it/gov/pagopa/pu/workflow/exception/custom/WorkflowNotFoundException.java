package it.gov.pagopa.pu.workflow.exception.custom;

import it.gov.pagopa.pu.workflow.utilities.ErrorCodeConstants;

/**
 * A custom exception that represents a not found workflow and extends {@link RuntimeException}.
 *
 */
public class WorkflowNotFoundException extends BaseBusinessException {

  /**
   * Constructs a new {@code WorkflowNotFoundException} with the specified detail message.
   *
   * @param message the detail message explaining the cause of the exception.
   */
  public WorkflowNotFoundException(String message) {
    super(ErrorCodeConstants.ERROR_CODE_WORKFLOW_NOT_FOUND, message);
  }
}
