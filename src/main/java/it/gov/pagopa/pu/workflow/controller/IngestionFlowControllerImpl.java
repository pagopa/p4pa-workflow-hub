package it.gov.pagopa.pu.workflow.controller;

import it.gov.pagopa.pu.workflow.controller.generated.IngestionFlowApi;
import it.gov.pagopa.pu.workflow.dto.generated.WorkflowCreatedDTO;
import it.gov.pagopa.pu.workflow.wf.ingestionflow.paymentsreporting.PaymentsReportingIngestionWFClient;
import it.gov.pagopa.pu.workflow.wf.ingestionflow.treasury.opi.TreasuryOpiIngestionWFClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
public class IngestionFlowControllerImpl implements IngestionFlowApi {

    private final PaymentsReportingIngestionWFClient paymentsReportingIngestionWFClient;
    private final TreasuryOpiIngestionWFClient treasuryOpiIngestionWFClient;

    public IngestionFlowControllerImpl(PaymentsReportingIngestionWFClient paymentsReportingIngestionWFClient,
                                       TreasuryOpiIngestionWFClient treasuryOpiIngestionWFClient) {
        this.paymentsReportingIngestionWFClient = paymentsReportingIngestionWFClient;
        this.treasuryOpiIngestionWFClient = treasuryOpiIngestionWFClient;
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

    @Override
    public ResponseEntity<WorkflowCreatedDTO> ingestTreasuryOpi(@PathVariable("ingestionFlowFileId") Long ingestionFlowFileId) {
        log.info("Creating Treasury OPI Ingestion Workflow for ingestionFlowFileId: {}", ingestionFlowFileId);
        String workflowId = treasuryOpiIngestionWFClient.ingest(ingestionFlowFileId);

        WorkflowCreatedDTO response = new WorkflowCreatedDTO(workflowId);
        response.setWorkflowId(workflowId);

        log.info("Treasury OPI Ingestion workflow {} created successfully for ingestionFlowFileId: {}", workflowId, ingestionFlowFileId);
        return ResponseEntity.status(201).body(response);
    }
}
