package it.gov.pagopa.pu.workflow.exception.custom;

import it.gov.pagopa.pu.workflow.utilities.ErrorCodeConstants;

public class WorkflowTypeNotFoundException extends BaseBusinessException {

  public WorkflowTypeNotFoundException(String message) {
    super(ErrorCodeConstants.ERROR_CODE_WORKFLOW_TYPE_NOT_FOUND, message);
  }
}
