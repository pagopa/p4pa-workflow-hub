package it.gov.pagopa.pu.workflow.wf.debtposition.massive;

import it.gov.pagopa.pu.workflow.dto.generated.WorkflowCreatedDTO;
import it.gov.pagopa.pu.workflow.service.temporal.WorkflowClientService;
import it.gov.pagopa.pu.workflow.service.temporal.WorkflowService;
import it.gov.pagopa.pu.workflow.utilities.TaskQueueConstants;
import it.gov.pagopa.pu.workflow.wf.debtposition.massive.wfmassiveibanupdate.MassiveIbanUpdateWF;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import static it.gov.pagopa.pu.workflow.utilities.Utilities.generateWorkflowId;

@Slf4j
@Service
public class MassiveDebtPositionWFClient {

  private final WorkflowService workflowService;
  private final WorkflowClientService workflowClientService;

  public MassiveDebtPositionWFClient(WorkflowService workflowService, WorkflowClientService workflowClientService) {
    this.workflowService = workflowService;
    this.workflowClientService = workflowClientService;
  }

  public WorkflowCreatedDTO startMassiveIbanUpdate(Long orgId, Long dptoId, String oldIban, String newIban, String oldPostalIban, String newPostalIban){
    log.debug("Starting massiveIbanUpdate process having orgId {}", orgId);
    String taskQueue = TaskQueueConstants.TASK_QUEUE_DP_LOW_PRIORITY;
    String workflowId = generateWorkflowId(orgId, MassiveIbanUpdateWF.class);

    MassiveIbanUpdateWF workflow = workflowService.buildWorkflowStubToStartNew(
      MassiveIbanUpdateWF.class,
      taskQueue,
      workflowId);

    return workflowClientService.start(workflow::massiveIbanUpdate, orgId, dptoId, oldIban, newIban, oldPostalIban, newPostalIban);
  }
}
