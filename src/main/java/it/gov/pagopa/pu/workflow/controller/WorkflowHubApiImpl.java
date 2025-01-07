package it.gov.pagopa.pu.workflow.controller;

import it.gov.pagopa.pu.workflow.controller.generated.IngestionFlowApi;
import it.gov.pagopa.pu.workflow.dto.generated.WorkflowCreatedDTO;
import it.gov.pagopa.pu.workflow.wf.ingestionflow.paymentsreporting.PaymentsReportingIngestionWFClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

/**
 * WorkflowHubApiImpl class
 * Implements the methods of the WorkflowHubApi interface
 * This class is used to create a Payment Ingestion Workflow
 * by its ingestionFileId and return the workflowId
 *
 * @see IngestionFlowApi
 */
@Slf4j
@RestController
public class WorkflowHubApiImpl implements IngestionFlowApi {

    private final PaymentsReportingIngestionWFClient paymentsReportingIngestionWFClient;

    public WorkflowHubApiImpl(PaymentsReportingIngestionWFClient paymentsReportingIngestionWFClient) {
        this.paymentsReportingIngestionWFClient = paymentsReportingIngestionWFClient;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseEntity<WorkflowCreatedDTO> ingestPaymentsReportingFile(@PathVariable("ingestionFlowFileId") Long ingestionFlowFileId) {
        log.info("Creating Payment Ingestion Workflow for ingestionFileId: {}", ingestionFlowFileId);

        String workflowId = paymentsReportingIngestionWFClient.ingest(ingestionFlowFileId);

        WorkflowCreatedDTO response = new WorkflowCreatedDTO(workflowId);
        response.setWorkflowId(workflowId);

        log.info("Payment Ingestion Workflow {} created successfully for ingestionFileId: {}", workflowId, ingestionFlowFileId);

        return ResponseEntity.status(201).body(response);
    }

}
