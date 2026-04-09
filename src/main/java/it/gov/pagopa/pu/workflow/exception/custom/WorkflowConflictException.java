package it.gov.pagopa.pu.workflow.exception.custom;

/**
 * A custom exception that represents a not found workflow and extends {@link RuntimeException}.
 *
 */
public class WorkflowConflictException extends RuntimeException {

  /**
   * Constructs a new {@code WorkflowConflictException} with the specified detail message.
   *
   * @param message the detail message explaining the cause of the exception.
   */
  public WorkflowConflictException(String message) {
    super(message);
  }
}
