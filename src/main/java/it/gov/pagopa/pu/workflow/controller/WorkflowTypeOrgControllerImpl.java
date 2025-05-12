package it.gov.pagopa.pu.workflow.controller;

import it.gov.pagopa.pu.workflow.controller.generated.WorkflowTypeOrgApi;
import it.gov.pagopa.pu.workflow.model.WorkflowTypeOrg;
import it.gov.pagopa.pu.workflow.service.WorkflowTypeOrgSaveService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class WorkflowTypeOrgControllerImpl implements WorkflowTypeOrgApi {

  private final WorkflowTypeOrgSaveService service;

  public WorkflowTypeOrgControllerImpl(WorkflowTypeOrgSaveService service) {
    this.service = service;
  }

  @Override
  public ResponseEntity<WorkflowTypeOrg> saveWorkflowTypeOrg(WorkflowTypeOrg body) {
    return ResponseEntity.ok(service.save(body));
  }
}
