package it.gov.pagopa.pu.workflow.wf.pagopa.send.campaign;

import io.temporal.client.schedules.ScheduleHandle;
import it.gov.pagopa.pu.workflow.enums.ScheduleEnum;
import it.gov.pagopa.pu.workflow.service.temporal.WorkflowScheduleService;
import it.gov.pagopa.pu.workflow.utilities.TaskQueueConstants;
import it.gov.pagopa.pu.workflow.wf.pagopa.send.stream.wf.SendNotificationStreamConsumeWF;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@Getter
public class SendCampaignCountersRefreshScheduler {

  private final ScheduleHandle schedule;

  public SendCampaignCountersRefreshScheduler(
    WorkflowScheduleService workflowScheduleService,
    @Value("${schedule.send-campaign-counters-refresh.cron-expression}") String cronExpression) {
    schedule = workflowScheduleService.schedule(
      ScheduleEnum.REFRESH_SEND_CAMPAIGN_COUNTERS,
      SendNotificationStreamConsumeWF.class, //TODO change WF reference to newly created WF
      TaskQueueConstants.TASK_QUEUE_LOW_PRIORITY, //TODO create/refer new task queue
      cronExpression);
  }
}
