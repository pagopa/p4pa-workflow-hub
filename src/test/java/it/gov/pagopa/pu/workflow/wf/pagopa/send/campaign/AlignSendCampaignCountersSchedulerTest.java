package it.gov.pagopa.pu.workflow.wf.pagopa.send.campaign;

import io.temporal.client.schedules.ScheduleHandle;
import it.gov.pagopa.pu.workflow.enums.ScheduleEnum;
import it.gov.pagopa.pu.workflow.service.temporal.WorkflowScheduleService;
import it.gov.pagopa.pu.workflow.utilities.TaskQueueConstants;
import it.gov.pagopa.pu.workflow.utils.TemporalTestUtils;
import it.gov.pagopa.pu.workflow.wf.pagopa.send.campaign.wf.AlignCountersUpdatedCampaignsWF;
import it.gov.pagopa.pu.workflow.wf.pagopa.send.campaign.wf.AlignCountersUpdatedCampaignsWFImpl;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AlignSendCampaignCountersSchedulerTest {

  @Mock
  private WorkflowScheduleService workflowScheduleServiceMock;

  @AfterEach
  void verifyNoMoreInteractions() {
    Mockito.verifyNoMoreInteractions(workflowScheduleServiceMock);
  }

  @Test
  void givenServiceCreationThenInvokeSchedule() {
    // Given
    String cronExpression = "cron";

    ScheduleHandle expectedResult = mock(ScheduleHandle.class);
    String taskQueue = TaskQueueConstants.TASK_QUEUE_SEND_MEDIUM_PRIORITY;
    when(workflowScheduleServiceMock.schedule(
        ScheduleEnum.ALIGN_SEND_CAMPAIGN_COUNTERS,
        AlignCountersUpdatedCampaignsWF.class,
        taskQueue,
        cronExpression,
        new Object[1]
      ))
      .thenReturn(expectedResult);

    // When
    AlignSendCampaignCountersScheduler scheduler = new AlignSendCampaignCountersScheduler(workflowScheduleServiceMock, cronExpression);
    ScheduleHandle result = scheduler.getSchedule();

    // Then
    Assertions.assertSame(expectedResult, result);

    TemporalTestUtils.verifyWorkflowTaskQueueConfiguration(taskQueue, AlignCountersUpdatedCampaignsWFImpl.class);
  }
}
