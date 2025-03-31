package it.gov.pagopa.pu.workflow.exception.custom;

/**
 * A custom exception that represents an internal error related to workflows and extends {@link RuntimeException}.
 *
 */
public class WorkflowInternalErrorException extends RuntimeException {

  /**
   * Constructs a new {@code WorkflowInternalErrorException} with the specified detail message.
   *
   * @param message the detail message explaining the cause of the exception.
   */
  public WorkflowInternalErrorException(String message) {
    super(message);
  }
}
