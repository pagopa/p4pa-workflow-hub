package it.gov.pagopa.pu.workflow.controller;

import it.gov.pagopa.pu.workflow.controller.generated.AssessmentsApi;
import it.gov.pagopa.pu.workflow.dto.generated.WorkflowCreatedDTO;
import it.gov.pagopa.pu.workflow.wf.assessments.CreateAssessmentsWFClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
public class AssessmentsControllerImpl implements AssessmentsApi {
  private final CreateAssessmentsWFClient createAssessmentsWFClient;

	public AssessmentsControllerImpl(CreateAssessmentsWFClient createAssessmentsWFClient) {
		this.createAssessmentsWFClient = createAssessmentsWFClient;
	}

  @Override
  public ResponseEntity<WorkflowCreatedDTO> createAssessmentsByReceiptId(Long receiptId) {
    log.info("Creating create assessments Workflow for receipt id {} ", receiptId);
    String workflowId = createAssessmentsWFClient.createAssessments(receiptId);

    WorkflowCreatedDTO response = new WorkflowCreatedDTO(workflowId);
    log.info("workflow {} created successfully for receipt id {}", workflowId, receiptId);
    return ResponseEntity.status(201).body(response);
  }
}
