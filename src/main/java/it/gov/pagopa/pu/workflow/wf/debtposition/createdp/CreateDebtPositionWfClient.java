package it.gov.pagopa.pu.workflow.wf.debtposition.createdp;

import io.temporal.client.WorkflowClient;
import it.gov.pagopa.payhub.activities.dto.debtposition.DebtPositionDTO;
import it.gov.pagopa.pu.workflow.service.WorkflowService;
import it.gov.pagopa.pu.workflow.wf.debtposition.createdp.wfsync.CreateDebtPositionSyncWF;
import it.gov.pagopa.pu.workflow.wf.debtposition.createdp.wfsync.CreateDebtPositionSyncWFImpl;
import org.springframework.stereotype.Service;

@Service
public class CreateDebtPositionWfClient {
  private final WorkflowService workflowService;

  public CreateDebtPositionWfClient(WorkflowService workflowService) {
    this.workflowService = workflowService;
  }

  public String createDPSync(DebtPositionDTO debtPosition) {
    String workflowId = String.valueOf(debtPosition.getDebtPositionId());
    CreateDebtPositionSyncWF workflow = workflowService.buildWorkflowStub(
      CreateDebtPositionSyncWF.class,
      CreateDebtPositionSyncWFImpl.TASK_QUEUE,
      workflowId);
    WorkflowClient.start(workflow::createDPSync, debtPosition);
    return workflowId;
  }
}
