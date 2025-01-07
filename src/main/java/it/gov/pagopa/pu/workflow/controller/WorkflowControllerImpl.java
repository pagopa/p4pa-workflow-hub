package it.gov.pagopa.pu.workflow.controller;

import it.gov.pagopa.pu.workflow.controller.generated.WorkflowApi;
import it.gov.pagopa.pu.workflow.dto.generated.WorkflowStatusDTO;
import it.gov.pagopa.pu.workflow.service.WorkflowService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
public class WorkflowControllerImpl implements WorkflowApi {

  private final WorkflowService service;

  public WorkflowControllerImpl(WorkflowService service) {
    this.service = service;
  }

  @Override
  public ResponseEntity<WorkflowStatusDTO> getWorkflowStatus(String workflowId) {
    log.info("Retrieving workflow status for workflowId: {}", workflowId);
    WorkflowStatusDTO status = service.getWorkflowStatus(workflowId);
    return new ResponseEntity<>(status, HttpStatus.OK);
  }
}
