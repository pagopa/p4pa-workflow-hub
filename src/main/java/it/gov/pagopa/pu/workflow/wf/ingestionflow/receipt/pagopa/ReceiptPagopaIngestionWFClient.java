package it.gov.pagopa.pu.workflow.wf.ingestionflow.receipt.pagopa;

import io.temporal.client.WorkflowClient;
import it.gov.pagopa.pu.workflow.dto.generated.WorkflowCreatedDTO;
import it.gov.pagopa.pu.workflow.mapper.WorkflowCreatedMapper;
import it.gov.pagopa.pu.workflow.service.WorkflowService;
import it.gov.pagopa.pu.workflow.wf.ingestionflow.receipt.pagopa.wfingestion.ReceiptPagopaIngestionWF;
import it.gov.pagopa.pu.workflow.wf.ingestionflow.receipt.pagopa.wfingestion.ReceiptPagopaIngestionWFImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import static it.gov.pagopa.pu.workflow.utilities.Utilities.generateWorkflowId;

@Slf4j
@Service
public class ReceiptPagopaIngestionWFClient {

  private final WorkflowService workflowService;

  public ReceiptPagopaIngestionWFClient(WorkflowService workflowService) {
    this.workflowService = workflowService;
  }

  public WorkflowCreatedDTO ingest(Long ingestionFlowFileId) {
    log.info("Starting Receipt Pagopa ingestion flow file having id {}", ingestionFlowFileId);
    String workflowId = generateWorkflowId(ingestionFlowFileId, ReceiptPagopaIngestionWF.class);
    ReceiptPagopaIngestionWF workflow = workflowService.buildWorkflowStub(
      ReceiptPagopaIngestionWF.class,
      ReceiptPagopaIngestionWFImpl.TASK_QUEUE_RECEIPT_PAGOPA_INGESTION_WF,
      workflowId);
    WorkflowCreatedDTO wfExec = WorkflowCreatedMapper.map(WorkflowClient.start(workflow::ingest, ingestionFlowFileId));
    log.info("Started workflow: {}", wfExec);
    return wfExec;
  }
}
