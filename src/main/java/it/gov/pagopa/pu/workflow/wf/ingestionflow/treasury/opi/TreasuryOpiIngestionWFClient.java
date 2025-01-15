package it.gov.pagopa.pu.workflow.wf.ingestionflow.treasury.opi;

import io.temporal.client.WorkflowClient;
import it.gov.pagopa.pu.workflow.service.WorkflowService;
import it.gov.pagopa.pu.workflow.wf.ingestionflow.treasury.opi.wfingestion.TreasuryOpiIngestionWF;
import it.gov.pagopa.pu.workflow.wf.ingestionflow.treasury.opi.wfingestion.TreasuryOpiIngestionWFImpl;
import org.springframework.stereotype.Service;

import static it.gov.pagopa.pu.workflow.utilities.Utilities.generateWorkflowId;

@Service
public class TreasuryOpiIngestionWFClient {

  private final WorkflowService workflowService;

  public TreasuryOpiIngestionWFClient(WorkflowService workflowService) {
    this.workflowService = workflowService;
  }

  public String ingest(Long ingestionFlowFileId) {
    String workflowId = generateWorkflowId(ingestionFlowFileId, TreasuryOpiIngestionWFImpl.TASK_QUEUE);
    TreasuryOpiIngestionWF workflow = workflowService.buildWorkflowStub(
      TreasuryOpiIngestionWF.class,
      TreasuryOpiIngestionWFImpl.TASK_QUEUE,
      workflowId);
    WorkflowClient.start(workflow::ingest, ingestionFlowFileId);
    return workflowId;
  }
}
