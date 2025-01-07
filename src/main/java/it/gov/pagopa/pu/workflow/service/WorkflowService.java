package it.gov.pagopa.pu.workflow.service;

import it.gov.pagopa.pu.workflow.dto.generated.WorkflowStatusDTO;

public interface WorkflowService {

  <T> T buildWorkflowStub(Class<T> workflowClass, String taskQueue, String workflowId);

  WorkflowStatusDTO getWorkflowStatus(String workflowId);
}
