package it.gov.pagopa.pu.workflow.service;

import io.temporal.client.WorkflowStub;
import it.gov.pagopa.pu.workflow.dto.generated.WorkflowStatusDTO;

import java.time.Duration;

public interface WorkflowService {

  <T> T buildWorkflowStub(Class<T> workflowClass, String taskQueue, String workflowId);

  WorkflowStub buildUntypedWorkflowStub( String taskQueue, String workflowId);

  WorkflowStatusDTO getWorkflowStatus(String workflowId);

  <T> T buildWorkflowStubScheduled(Class<T> workflowClass, String taskQueue, String workflowId, Duration startDelay);
}
