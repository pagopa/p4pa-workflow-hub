package it.gov.pagopa.pu.workflow.controller;

import it.gov.pagopa.pu.workflow.controller.generated.WorkflowHubApi;
import it.gov.pagopa.pu.workflow.dto.generated.WorkflowStatusDTO;
import it.gov.pagopa.pu.workflow.service.WorkflowService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class WorkflowControllerImpl implements WorkflowHubApi {

  private final WorkflowService service;

  public WorkflowControllerImpl(WorkflowService service) {
    this.service = service;
  }

  @Override
  public ResponseEntity<WorkflowStatusDTO> getWorkflowStatus(String workflowId) {
    WorkflowStatusDTO status = service.getWorkflowStatus(workflowId);
    return new ResponseEntity<>(status, HttpStatus.OK);
  }
}
