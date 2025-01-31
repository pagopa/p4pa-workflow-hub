package it.gov.pagopa.pu.workflow.wf.debtposition.handledp;

import io.temporal.client.WorkflowClient;
import it.gov.pagopa.pu.debtposition.dto.generated.DebtPositionDTO;
import it.gov.pagopa.pu.workflow.service.WorkflowService;
import it.gov.pagopa.pu.workflow.wf.debtposition.handledp.wfsync.HandleDebtPositionSyncWF;
import it.gov.pagopa.pu.workflow.wf.debtposition.handledp.wfsync.HandleDebtPositionSyncWFImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import static it.gov.pagopa.pu.workflow.utilities.Utilities.generateWorkflowId;

@Slf4j
@Service
public class HandleDebtPositionWfClientImpl implements HandleDebtPositionWfClient {
  private final WorkflowService workflowService;

  public HandleDebtPositionWfClientImpl(WorkflowService workflowService) {
    this.workflowService = workflowService;
  }

  @Override
  public String handleDPSync(DebtPositionDTO debtPosition) {
    log.info("Starting SYNC debt position handling WF: {}", debtPosition.getDebtPositionId());
    String workflowId = generateWorkflowId(debtPosition.getDebtPositionId(), HandleDebtPositionSyncWFImpl.TASK_QUEUE_HANDLE_DEBT_POSITION_SYNC_WF);
    HandleDebtPositionSyncWF workflow = workflowService.buildWorkflowStub(
      HandleDebtPositionSyncWF.class,
      HandleDebtPositionSyncWFImpl.TASK_QUEUE_HANDLE_DEBT_POSITION_SYNC_WF,
      workflowId);
    WorkflowClient.start(workflow::handleDPSync, debtPosition);
    return workflowId;
  }
}
