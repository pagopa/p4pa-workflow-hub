package it.gov.pagopa.pu.workflow.wf.ingestionflow.receipt;

import it.gov.pagopa.pu.workflow.dto.generated.WorkflowCreatedDTO;
import it.gov.pagopa.pu.workflow.service.temporal.WorkflowClientService;
import it.gov.pagopa.pu.workflow.service.temporal.WorkflowService;
import it.gov.pagopa.pu.workflow.wf.ingestionflow.receipt.wfingestion.ReceiptIngestionWF;
import it.gov.pagopa.pu.workflow.wf.ingestionflow.receipt.wfingestion.ReceiptIngestionWFImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import static it.gov.pagopa.pu.workflow.utilities.Utilities.generateWorkflowId;

@Slf4j
@Service
public class ReceiptIngestionWFClient {

  private final WorkflowService workflowService;
  private final WorkflowClientService workflowClientService;


  public ReceiptIngestionWFClient(
    WorkflowService workflowService,
    WorkflowClientService workflowClientService
  ) {
    this.workflowService = workflowService;
    this.workflowClientService = workflowClientService;
  }

  public WorkflowCreatedDTO ingest(Long ingestionFlowFileId) {
    log.info("Starting receipt ingestion flow file having id {}", ingestionFlowFileId);
    String taskQueue = ReceiptIngestionWFImpl.TASK_QUEUE_RECEIPT_INGESTION_WF;
    String workflowId = generateWorkflowId(ingestionFlowFileId, ReceiptIngestionWF.class);

    ReceiptIngestionWF workflow = workflowService.buildWorkflowStub(
      ReceiptIngestionWF.class,
      taskQueue,
      workflowId);
    return workflowClientService.start(workflow::ingest, ingestionFlowFileId);
  }

}
