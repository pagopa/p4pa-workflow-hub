package it.gov.pagopa.pu.workflow.wf.debtposition.massive;

import it.gov.pagopa.pu.workflow.dto.generated.WorkflowCreatedDTO;
import it.gov.pagopa.pu.workflow.service.temporal.WorkflowClientService;
import it.gov.pagopa.pu.workflow.service.temporal.WorkflowService;
import it.gov.pagopa.pu.workflow.utilities.TaskQueueConstants;
import it.gov.pagopa.pu.workflow.wf.debtposition.massive.dto.MassiveIbanUpdateToSyncSignalDTO;
import it.gov.pagopa.pu.workflow.wf.debtposition.massive.wfmassiveibanupdate.MassiveIbanUpdateWF;

import it.gov.pagopa.pu.workflow.wf.debtposition.massive.wfmassiveibanupdatetosync.MassiveIbanUpdateToSyncWF;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Duration;

import static it.gov.pagopa.pu.workflow.utilities.Utilities.generateWorkflowId;

@Slf4j
@Service
public class MassiveDebtPositionWFClient {

  private final WorkflowService workflowService;
  private final WorkflowClientService workflowClientService;

  public MassiveDebtPositionWFClient(
    WorkflowService workflowService,
    WorkflowClientService workflowClientService
  ) {
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

  public WorkflowCreatedDTO scheduleMassiveIbanUpdateToSync(MassiveIbanUpdateToSyncSignalDTO signalDTO, Duration scheduleDuration) {
    Long orgId = signalDTO.getOrgId();
    Long dptoId = signalDTO.getDptoId();

    log.info("Start of scheduling to sync massive iban update WF for debtPositionTypeOrgId {} or organizationId {}, with delay of {} minutes", dptoId, orgId, scheduleDuration.toMinutes());

    String workflowId = generateWorkflowId(orgId, MassiveIbanUpdateToSyncWF.class);
    MassiveIbanUpdateToSyncWF workflow = workflowService.buildWorkflowStubDelayed(
      MassiveIbanUpdateToSyncWF.class,
      TaskQueueConstants.TASK_QUEUE_DP_LOW_PRIORITY,
      workflowId,
      scheduleDuration
    );

    return workflowClientService.start(workflow::massiveIbanUpdate, orgId, dptoId, signalDTO.getOldIban(), signalDTO.getNewIban(), signalDTO.getOldPostalIban(), signalDTO.getNewPostalIban());
  }
}
