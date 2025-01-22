package it.gov.pagopa.pu.workflow.wf.debtposition.createdp;

import io.temporal.client.WorkflowClient;
import it.gov.pagopa.pu.debtposition.dto.generated.DebtPositionDTO;
import it.gov.pagopa.pu.workflow.service.WorkflowService;
import it.gov.pagopa.pu.workflow.wf.debtposition.createdp.wfsync.CreateDebtPositionSyncWF;
import it.gov.pagopa.pu.workflow.wf.debtposition.createdp.wfsync.CreateDebtPositionSyncWFImpl;
import it.gov.pagopa.pu.workflow.wf.debtposition.createdp.wfsyncstandin.CreateDebtPositionSyncAcaWF;
import it.gov.pagopa.pu.workflow.wf.debtposition.createdp.wfsyncstandin.CreateDebtPositionSyncAcaWFImpl;
import org.springframework.stereotype.Service;

import static it.gov.pagopa.pu.workflow.utilities.Utilities.generateWorkflowId;

@Service
public class CreateDebtPositionWfClientImpl implements CreateDebtPositionWfClient{
  private final WorkflowService workflowService;

  public CreateDebtPositionWfClientImpl(WorkflowService workflowService) {
    this.workflowService = workflowService;
  }

  @Override
  public String createDPSync(DebtPositionDTO debtPosition) {
    String workflowId = generateWorkflowId(debtPosition.getDebtPositionId(), CreateDebtPositionSyncWFImpl.TASK_QUEUE);
    CreateDebtPositionSyncWF workflow = workflowService.buildWorkflowStub(
      CreateDebtPositionSyncWF.class,
      CreateDebtPositionSyncWFImpl.TASK_QUEUE,
      workflowId);
    WorkflowClient.start(workflow::createDPSync, debtPosition);
    return workflowId;
  }

  @Override
  public String createDPSyncAca(DebtPositionDTO debtPosition) {
    String workflowId = generateWorkflowId(debtPosition.getDebtPositionId(), CreateDebtPositionSyncAcaWFImpl.TASK_QUEUE);
    CreateDebtPositionSyncAcaWF workflow = workflowService.buildWorkflowStub(
      CreateDebtPositionSyncAcaWF.class,
      CreateDebtPositionSyncAcaWFImpl.TASK_QUEUE,
      workflowId);
    WorkflowClient.start(workflow::createDPSyncAca, debtPosition);
    return workflowId;  }
}
