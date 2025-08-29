package it.gov.pagopa.pu.workflow.controller;

import io.temporal.api.enums.v1.WorkflowExecutionStatus;
import it.gov.pagopa.pu.workflow.controller.generated.WorkflowApi;
import it.gov.pagopa.pu.workflow.dto.generated.WorkflowStatusDTO;
import it.gov.pagopa.pu.workflow.service.temporal.WorkflowCompletionService;
import it.gov.pagopa.pu.workflow.service.temporal.WorkflowService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
public class WorkflowControllerImpl implements WorkflowApi {

  private final WorkflowService service;
  private final WorkflowCompletionService workflowCompletionService;

  public WorkflowControllerImpl(WorkflowService service, WorkflowCompletionService workflowCompletionService) {
    this.service = service;
    this.workflowCompletionService = workflowCompletionService;
  }

  @Override
  public ResponseEntity<WorkflowStatusDTO> getWorkflowStatus(String workflowId) {
    log.info("Retrieving workflow status for workflowId: {}", workflowId);
    WorkflowStatusDTO status = service.getWorkflowStatus(workflowId);
    return new ResponseEntity<>(status, HttpStatus.OK);
  }

  @Override
  public ResponseEntity<WorkflowStatusDTO> waitWorkflowCompletion(String workflowId, Integer maxAttempts, Integer retryDelayMs) {
    log.info("Waiting for workflow with id: {} to complete", workflowId);
    WorkflowStatusDTO workflowStatusDTO = workflowCompletionService.waitTerminationStatus(workflowId, maxAttempts, retryDelayMs);
    return new ResponseEntity<>(workflowStatusDTO, HttpStatus.OK);
  }
}
