package it.gov.pagopa.pu.workflow.wf.debtposition.massive.activity;

import io.temporal.spring.boot.ActivityImpl;
import it.gov.pagopa.pu.workflow.service.temporal.WorkflowClientService;
import it.gov.pagopa.pu.workflow.service.temporal.WorkflowService;
import it.gov.pagopa.pu.workflow.utilities.TaskQueueConstants;
import it.gov.pagopa.pu.workflow.wf.debtposition.massive.wfmassiveibanupdate.MassiveIbanUpdateWF;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Duration;

import static it.gov.pagopa.pu.workflow.utilities.Utilities.generateWorkflowId;

@Service
@Slf4j
@ActivityImpl(taskQueues = TaskQueueConstants.TASK_QUEUE_DP_LOW_PRIORITY_LOCAL)
public class ScheduleToSyncMassiveIbanUpdateWFActivityImpl implements ScheduleToSyncMassiveIbanUpdateWFActivity {
  private final WorkflowService workflowService;
  private final WorkflowClientService workflowClientService;
  private final Duration scheduleDuration;

  public ScheduleToSyncMassiveIbanUpdateWFActivityImpl(
    WorkflowService workflowService,
    WorkflowClientService workflowClientService,
    @Value("${workflow.massive-debt-position.schedule-minutes-massive-iban-update-to-sync}") int scheduleMinutes
  ) {
    this.workflowService = workflowService;
    this.workflowClientService = workflowClientService;
    this.scheduleDuration = Duration.ofMinutes(scheduleMinutes);
  }

  @Override
  public void scheduleToSyncMassiveIbanUpdateWF(Long orgId, Long dptoId, String oldIban, String newIban, String oldPostalIban, String newPostalIban) {
    log.info("Start of scheduling to sync massive iban update WF for debtPositionTypeOrgId {} or organizationId {}, with delay of {} minutes", dptoId, orgId, this.scheduleDuration.toMinutes());
    String workflowId = generateWorkflowId(orgId + "_TO_SYNC", MassiveIbanUpdateWF.class);
    MassiveIbanUpdateWF workflow = workflowService.buildWorkflowStubDelayed(
      MassiveIbanUpdateWF.class,
      TaskQueueConstants.TASK_QUEUE_DP_LOW_PRIORITY,
      workflowId,
      this.scheduleDuration
    );
    workflowClientService.start(workflow::massiveIbanUpdate, orgId, dptoId, oldIban, newIban, oldPostalIban, newPostalIban);
  }
}
