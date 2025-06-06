package it.gov.pagopa.pu.workflow.wf.ingestionflow.debtpositiontype;

import it.gov.pagopa.pu.workflow.dto.generated.WorkflowCreatedDTO;
import it.gov.pagopa.pu.workflow.service.temporal.WorkflowClientService;
import it.gov.pagopa.pu.workflow.service.temporal.WorkflowService;
import it.gov.pagopa.pu.workflow.wf.ingestionflow.debtpositiontype.wfingestion.DebtPositionTypeIngestionWF;
import it.gov.pagopa.pu.workflow.wf.ingestionflow.debtpositiontype.wfingestion.DebtPositionTypeIngestionWFImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import static it.gov.pagopa.pu.workflow.utilities.Utilities.generateWorkflowId;

@Slf4j
@Service
@RequiredArgsConstructor
public class DebtPositionTypeIngestionWFClient {

  private final WorkflowService workflowService;
  private final WorkflowClientService workflowClientService;

  public WorkflowCreatedDTO ingest(Long ingestionFlowFileId) {
    log.info("Starting debt position type ingestion flow file having id {}", ingestionFlowFileId);
    String taskQueue = DebtPositionTypeIngestionWFImpl.TASK_QUEUE_DEBT_POSITION_TYPE_INGESTION_WF;
    String workflowId = generateWorkflowId(ingestionFlowFileId, DebtPositionTypeIngestionWF.class);

    DebtPositionTypeIngestionWF workflow = workflowService.buildWorkflowStub(
      DebtPositionTypeIngestionWF.class,
      taskQueue,
      workflowId);
    return workflowClientService.start(workflow::ingest, ingestionFlowFileId);
  }
}
