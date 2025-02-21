package it.gov.pagopa.pu.workflow.wf.ingestionflow.debtposition;

import io.temporal.client.WorkflowClient;
import it.gov.pagopa.pu.workflow.service.WorkflowService;
import it.gov.pagopa.pu.workflow.wf.ingestionflow.debtposition.wfingestion.DebtPositionIngestionFlowWF;
import it.gov.pagopa.pu.workflow.wf.ingestionflow.debtposition.wfingestion.DebtPositionIngestionFlowWFImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import static it.gov.pagopa.pu.workflow.utilities.Utilities.generateWorkflowId;

@Slf4j
@Service
public class DebtPositionIngestionWFClient {

  private final WorkflowService workflowService;

  public DebtPositionIngestionWFClient(WorkflowService workflowService) {
    this.workflowService = workflowService;
  }

  public String ingest(Long ingestionFlowFileId) {
    log.info("Starting debt position ingestion flow file having id {}", ingestionFlowFileId);
    String taskQueue = DebtPositionIngestionFlowWFImpl.TASK_QUEUE_DEBT_POSITION_INGESTION_FLOW;
    String workflowId = generateWorkflowId(ingestionFlowFileId, taskQueue);

    DebtPositionIngestionFlowWF workflow = workflowService.buildWorkflowStub(
      DebtPositionIngestionFlowWF.class,
      taskQueue,
      workflowId);
    WorkflowClient.start(workflow::ingest, ingestionFlowFileId);
    return workflowId;
  }
}
