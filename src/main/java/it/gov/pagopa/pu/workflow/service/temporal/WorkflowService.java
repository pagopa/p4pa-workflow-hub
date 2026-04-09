package it.gov.pagopa.pu.workflow.service.temporal;

import io.temporal.client.WorkflowNotFoundException;
import io.temporal.client.WorkflowStub;
import it.gov.pagopa.pu.workflow.dto.generated.WorkflowStatusDTO;
import it.gov.pagopa.pu.workflow.exception.custom.WorkflowConflictException;
import it.gov.pagopa.pu.workflow.exception.custom.WorkflowInternalErrorException;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.Optional;

public interface WorkflowService {

  <T> T buildWorkflowStubToStartNew(Class<T> workflowClass, String taskQueue, String workflowId);

  <T> T buildWorkflowStub(Class<T> workflowClass, String workflowId, Optional<String> runId);

  WorkflowStub buildUntypedWorkflowStub(Class<?> workflowClass, String taskQueue, String workflowId);

  /**
   * Check Workflow status.
   *
   * @param workflowId The ID of the workflow to check.
   * @throws WorkflowInternalErrorException
   * @throws WorkflowNotFoundException
   */
  WorkflowStatusDTO getWorkflowStatus(String workflowId);

  <T> T buildWorkflowStubDelayed(Class<T> workflowClass, String taskQueue, String workflowId, Duration startDelay);

  <T> T buildWorkflowStubScheduled(Class<T> workflowClass, String taskQueue, String workflowId, LocalDate date);
  <T> T buildWorkflowStubScheduled(Class<T> workflowClass, String taskQueue, String workflowId, LocalDateTime dateTime);
  <T> T buildWorkflowStubScheduled(Class<T> workflowClass, String taskQueue, String workflowId, OffsetDateTime dateTime);

  void  cancelWorkflow(String workflowId);
}
