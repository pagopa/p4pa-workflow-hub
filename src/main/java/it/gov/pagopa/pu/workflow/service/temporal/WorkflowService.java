package it.gov.pagopa.pu.workflow.service.temporal;

import io.temporal.client.WorkflowStub;
import it.gov.pagopa.pu.workflow.dto.generated.WorkflowStatusDTO;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.Optional;

public interface WorkflowService {

  <T> T buildWorkflowStubToStartNew(Class<T> workflowClass, String taskQueue, String workflowId);

  <T> T buildWorkflowStub(Class<T> workflowClass, String workflowId, Optional<String> runId);

  WorkflowStub buildUntypedWorkflowStub(Class<?> workflowClass, String taskQueue, String workflowId);

  WorkflowStatusDTO getWorkflowStatus(String workflowId);

  <T> T buildWorkflowStubDelayed(Class<T> workflowClass, String taskQueue, String workflowId, Duration startDelay);

  <T> T buildWorkflowStubScheduled(Class<T> workflowClass, String taskQueue, String workflowId, LocalDate date);
  <T> T buildWorkflowStubScheduled(Class<T> workflowClass, String taskQueue, String workflowId, LocalDateTime dateTime);
  <T> T buildWorkflowStubScheduled(Class<T> workflowClass, String taskQueue, String workflowId, OffsetDateTime dateTime);

  void  cancelWorkflow(String workflowId);
}
