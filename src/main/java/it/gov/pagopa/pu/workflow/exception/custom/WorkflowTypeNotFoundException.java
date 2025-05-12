package it.gov.pagopa.pu.workflow.exception.custom;

public class WorkflowTypeNotFoundException extends RuntimeException {
  public WorkflowTypeNotFoundException(String message) {
    super(message);
  }
}
