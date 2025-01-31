package it.gov.pagopa.pu.workflow.wf.debtposition.aligndp;

import io.temporal.client.WorkflowClient;
import it.gov.pagopa.pu.debtposition.dto.generated.DebtPositionDTO;
import it.gov.pagopa.pu.workflow.service.WorkflowService;
import it.gov.pagopa.pu.workflow.wf.debtposition.aligndp.wfsyncstandin.SynchronizeSyncAcaWF;
import it.gov.pagopa.pu.workflow.wf.debtposition.aligndp.wfsyncstandin.SynchronizeSyncAcaWFImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import static it.gov.pagopa.pu.workflow.utilities.Utilities.generateWorkflowId;

@Slf4j
@Service
public class SynchronizeSyncAcaWfClientImpl implements SynchronizeSyncAcaWfClient {
  private final WorkflowService workflowService;

  public SynchronizeSyncAcaWfClientImpl(WorkflowService workflowService) {
    this.workflowService = workflowService;
  }

  @Override
  public String synchronizeDPSyncAca(DebtPositionDTO debtPosition) {
    log.info("Starting sync ACA debt position WF: {}", debtPosition.getDebtPositionId());
    String workflowId = generateWorkflowId(debtPosition.getDebtPositionId(), SynchronizeSyncAcaWFImpl.TASK_QUEUE_SYNCHRONIZE_SYNC_ACA_WF);
    SynchronizeSyncAcaWF workflow = workflowService.buildWorkflowStub(
      SynchronizeSyncAcaWF.class,
      SynchronizeSyncAcaWFImpl.TASK_QUEUE_SYNCHRONIZE_SYNC_ACA_WF,
      workflowId);
    WorkflowClient.start(workflow::synchronizeDPSyncAca, debtPosition);
    return workflowId;  }
}
