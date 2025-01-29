package it.gov.pagopa.pu.workflow.wf.debtposition.createdp;

import io.temporal.client.WorkflowClient;
import it.gov.pagopa.pu.debtposition.dto.generated.DebtPositionDTO;
import it.gov.pagopa.pu.workflow.service.WorkflowService;
import it.gov.pagopa.pu.workflow.wf.debtposition.createdp.wfsync.CreateDebtPositionSyncWF;
import it.gov.pagopa.pu.workflow.wf.debtposition.createdp.wfsync.CreateDebtPositionSyncWFImpl;
import it.gov.pagopa.pu.workflow.wf.debtposition.aligndp.wfsyncstandin.SynchronizeSyncAcaWF;
import it.gov.pagopa.pu.workflow.wf.debtposition.aligndp.wfsyncstandin.SynchronizeSyncAcaWFImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import static it.gov.pagopa.pu.workflow.utilities.Utilities.generateWorkflowId;

@Slf4j
@Service
public class CreateDebtPositionWfClientImpl implements CreateDebtPositionWfClient{
  private final WorkflowService workflowService;

  public CreateDebtPositionWfClientImpl(WorkflowService workflowService) {
    this.workflowService = workflowService;
  }

  @Override
  public String createDPSync(DebtPositionDTO debtPosition) {
    log.info("Starting SYNC debt position creation WF: {}", debtPosition.getDebtPositionId());
    String workflowId = generateWorkflowId(debtPosition.getDebtPositionId(), CreateDebtPositionSyncWFImpl.TASK_QUEUE);
    CreateDebtPositionSyncWF workflow = workflowService.buildWorkflowStub(
      CreateDebtPositionSyncWF.class,
      CreateDebtPositionSyncWFImpl.TASK_QUEUE,
      workflowId);
    WorkflowClient.start(workflow::createDPSync, debtPosition);
    return workflowId;
  }
}
