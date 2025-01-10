package it.gov.pagopa.pu.workflow.controller;

import it.gov.pagopa.pu.workflow.controller.generated.IngestionFlowApi;
import it.gov.pagopa.pu.workflow.dto.generated.WorkflowCreatedDTO;
import it.gov.pagopa.pu.workflow.wf.ingestionflow.paymentsreporting.PaymentsReportingIngestionWFClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
public class IngestionFlowControllerImpl implements IngestionFlowApi {

    private final PaymentsReportingIngestionWFClient paymentsReportingIngestionWFClient;

    public IngestionFlowControllerImpl(PaymentsReportingIngestionWFClient paymentsReportingIngestionWFClient) {
        this.paymentsReportingIngestionWFClient = paymentsReportingIngestionWFClient;
    }

    @Override
    public ResponseEntity<WorkflowCreatedDTO> ingestPaymentsReportingFile(@PathVariable("ingestionFlowFileId") Long ingestionFlowFileId) {
        log.info("Creating Payments Reporting Ingestion Workflow for ingestionFlowFileId: {}", ingestionFlowFileId);

        String workflowId = paymentsReportingIngestionWFClient.ingest(ingestionFlowFileId);

        WorkflowCreatedDTO response = new WorkflowCreatedDTO(workflowId);
        response.setWorkflowId(workflowId);

        log.info("Payments Reporting Ingestion workflow {} created successfully for ingestionFileId: {}", workflowId, ingestionFlowFileId);

        return ResponseEntity.status(201).body(response);
    }

}
