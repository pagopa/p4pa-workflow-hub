package it.gov.pagopa.pu.workflow.controller;

import it.gov.pagopa.pu.workflow.controller.generated.PaymentNotificationApi;
import it.gov.pagopa.pu.workflow.dto.generated.WorkflowCreatedDTO;
import it.gov.pagopa.pu.workflow.wf.ingestionflow.paymentnotification.PaymentNotificationIngestionWFClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
public class PaymentNotificationControllerImpl implements PaymentNotificationApi {
  private final PaymentNotificationIngestionWFClient paymentNotificationIngestionWFClient;

	public PaymentNotificationControllerImpl(PaymentNotificationIngestionWFClient paymentNotificationIngestionWFClient) {
		this.paymentNotificationIngestionWFClient = paymentNotificationIngestionWFClient;
	}

  @Override
  public ResponseEntity<WorkflowCreatedDTO> createPaymentNotificationByIngestionFlowFileId(Long ingestionFlowFileId) {
    log.info("Creating create assessments Workflow for receipt id {} ", ingestionFlowFileId);
    String workflowId = paymentNotificationIngestionWFClient.ingest(ingestionFlowFileId);

    WorkflowCreatedDTO response = new WorkflowCreatedDTO(workflowId);
    log.info("workflow {} created successfully for ingestionFlowFileId {}", workflowId, ingestionFlowFileId);
    return ResponseEntity.status(201).body(response);
  }
}
