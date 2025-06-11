package it.gov.pagopa.pu.workflow.wf.ingestionflow.receipt.pagopa;

import it.gov.pagopa.pu.workflow.dto.generated.WorkflowCreatedDTO;
import it.gov.pagopa.pu.workflow.service.temporal.WorkflowClientService;
import it.gov.pagopa.pu.workflow.service.temporal.WorkflowService;
import it.gov.pagopa.pu.workflow.utilities.TaskQueueConstants;
import it.gov.pagopa.pu.workflow.wf.ingestionflow.receipt.pagopa.wfingestion.ReceiptPagopaIngestionWF;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import static it.gov.pagopa.pu.workflow.utilities.Utilities.generateWorkflowId;

@Slf4j
@Service
public class ReceiptPagopaIngestionWFClient {

  private final WorkflowService workflowService;
  private final WorkflowClientService workflowClientService;

  public ReceiptPagopaIngestionWFClient(WorkflowService workflowService, WorkflowClientService workflowClientService) {
    this.workflowService = workflowService;
    this.workflowClientService = workflowClientService;
  }

  public WorkflowCreatedDTO ingest(Long ingestionFlowFileId) {
    log.info("Starting Receipt Pagopa ingestion flow file having id {}", ingestionFlowFileId);
    String workflowId = generateWorkflowId(ingestionFlowFileId, ReceiptPagopaIngestionWF.class);
    ReceiptPagopaIngestionWF workflow = workflowService.buildWorkflowStub(
      ReceiptPagopaIngestionWF.class,
      TaskQueueConstants.TASK_QUEUE_IMPORT_MEDIUM_PRIORITY,
      workflowId);
    return workflowClientService.start(workflow::ingest, ingestionFlowFileId);
  }
}
