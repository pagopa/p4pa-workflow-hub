package it.gov.pagopa.pu.workflow.wf.ingestionflow.treasury.opi;

import io.temporal.client.WorkflowClient;
import it.gov.pagopa.pu.workflow.service.WorkflowService;
import it.gov.pagopa.pu.workflow.wf.ingestionflow.treasury.opi.wfingestion.TreasuryOpiIngestionWF;
import it.gov.pagopa.pu.workflow.wf.ingestionflow.treasury.opi.wfingestion.TreasuryOpiIngestionWFImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import static it.gov.pagopa.pu.workflow.utilities.Utilities.generateWorkflowId;

@Slf4j
@Service
public class TreasuryOpiIngestionWFClient {

  private final WorkflowService workflowService;

  public TreasuryOpiIngestionWFClient(WorkflowService workflowService) {
    this.workflowService = workflowService;
  }

  public String ingest(Long ingestionFlowFileId) {
    log.info("Starting treasury OPI ingestion flow file having id {}", ingestionFlowFileId);
    String workflowId = generateWorkflowId(ingestionFlowFileId, TreasuryOpiIngestionWFImpl.TASK_QUEUE_TREASURY_OPI_INGESTION_WF);
    TreasuryOpiIngestionWF workflow = workflowService.buildWorkflowStub(
      TreasuryOpiIngestionWF.class,
      TreasuryOpiIngestionWFImpl.TASK_QUEUE_TREASURY_OPI_INGESTION_WF,
      workflowId);
    WorkflowClient.start(workflow::ingest, ingestionFlowFileId);
    return workflowId;
  }
}
