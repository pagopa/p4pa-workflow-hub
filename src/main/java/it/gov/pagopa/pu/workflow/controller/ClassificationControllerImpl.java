package it.gov.pagopa.pu.workflow.controller;

import it.gov.pagopa.pu.workflow.controller.generated.ClassificationApi;
import it.gov.pagopa.pu.workflow.dto.generated.WorkflowCreatedDTO;
import it.gov.pagopa.pu.workflow.wf.classification.transfer.TransferClassificationWFClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
public class ClassificationControllerImpl implements ClassificationApi {
  private final TransferClassificationWFClient transferClassificationWFClient;

	public ClassificationControllerImpl(TransferClassificationWFClient transferClassificationWFClient) {
		this.transferClassificationWFClient = transferClassificationWFClient;
	}

	@Override
  public ResponseEntity<WorkflowCreatedDTO> transferClassification(Long orgId, String iuv, String iur, Integer transferIndex) {
    log.info("Creating Treasury OPI Ingestion Workflow for for organization id {} and iuv {} and iur {} and transfer index {}", orgId, iuv, iur, transferIndex);
    String workflowId = transferClassificationWFClient.classify(orgId, iuv, iur, transferIndex);

    WorkflowCreatedDTO response = new WorkflowCreatedDTO(workflowId);
    log.info("workflow {} created successfully for organization id {} and iuv {} and iur {} and transfer index {}", workflowId, orgId, iuv, iur, transferIndex);
    return ResponseEntity.status(201).body(response);
  }
}
