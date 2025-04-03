package it.gov.pagopa.pu.workflow.exception.custom;

/**
 * A custom exception that represents an invalid or missing execution config related to workflows and extends {@link RuntimeException}.
 *
 */
public class InvalidWfExecutionConfigException  extends RuntimeException {

  /**
   * Constructs a new {@code InvalidWfExecutionConfigException} with the specified detail message.
   *
   * @param message the detail message explaining the cause of the exception.
   */
  public InvalidWfExecutionConfigException(String message) {
    super(message);
  }
}

