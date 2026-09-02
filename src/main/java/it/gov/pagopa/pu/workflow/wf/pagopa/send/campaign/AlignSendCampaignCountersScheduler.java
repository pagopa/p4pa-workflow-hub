package it.gov.pagopa.pu.workflow.wf.pagopa.send.campaign;

import io.temporal.client.schedules.ScheduleHandle;
import it.gov.pagopa.pu.workflow.enums.ScheduleEnum;
import it.gov.pagopa.pu.workflow.service.temporal.WorkflowScheduleService;
import it.gov.pagopa.pu.workflow.utilities.TaskQueueConstants;
import it.gov.pagopa.pu.workflow.wf.pagopa.send.campaign.wf.AlignCountersUpdatedCampaignsWF;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@Getter
public class AlignSendCampaignCountersScheduler {

  private final ScheduleHandle schedule;

  AlignSendCampaignCountersScheduler(
    WorkflowScheduleService workflowScheduleService,
    @Value("${schedule.align-send-campaign-counters.cron-expression}") String cronExpression) {
    schedule = workflowScheduleService.schedule(
      ScheduleEnum.ALIGN_SEND_CAMPAIGN_COUNTERS,
      AlignCountersUpdatedCampaignsWF.class,
      TaskQueueConstants.TASK_QUEUE_SEND_MEDIUM_PRIORITY,
      cronExpression,
      new Object[1]
    );
  }
}
