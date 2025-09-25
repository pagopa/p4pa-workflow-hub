package it.gov.pagopa.pu.workflow.wf.ingestionflow.treasury.xls;

import it.gov.pagopa.pu.workflow.dto.generated.WorkflowCreatedDTO;
import it.gov.pagopa.pu.workflow.service.temporal.WorkflowClientService;
import it.gov.pagopa.pu.workflow.service.temporal.WorkflowService;
import it.gov.pagopa.pu.workflow.utilities.TaskQueueConstants;
import it.gov.pagopa.pu.workflow.wf.ingestionflow.treasury.xls.wfingestion.TreasuryXlsIngestionWF;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import static it.gov.pagopa.pu.workflow.utilities.Utilities.generateWorkflowId;

@Slf4j
@Service
@RequiredArgsConstructor
public class TreasuryXlsIngestionWFClient {
  private final WorkflowService workflowService;
  private final WorkflowClientService workflowClientService;

  public WorkflowCreatedDTO ingest(Long ingestionFlowFileId) {
    log.info("Starting treasury xls ingestion flow file having id {}", ingestionFlowFileId);
    String taskQueue = TaskQueueConstants.TASK_QUEUE_IMPORT_MEDIUM_PRIORITY;
    String workflowId = generateWorkflowId(ingestionFlowFileId, TreasuryXlsIngestionWF.class);

    TreasuryXlsIngestionWF workflow = workflowService.buildWorkflowStub(
      TreasuryXlsIngestionWF.class,
      taskQueue,
      workflowId);
    return workflowClientService.start(workflow::ingest, ingestionFlowFileId);
  }
}
