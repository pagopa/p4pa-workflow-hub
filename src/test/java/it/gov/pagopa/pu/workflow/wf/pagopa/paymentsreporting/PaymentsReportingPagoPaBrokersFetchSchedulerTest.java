package it.gov.pagopa.pu.workflow.wf.pagopa.paymentsreporting;

import io.temporal.client.schedules.ScheduleHandle;
import it.gov.pagopa.pu.workflow.enums.ScheduleEnum;
import it.gov.pagopa.pu.workflow.service.temporal.WorkflowScheduleService;
import it.gov.pagopa.pu.workflow.utilities.TaskQueueConstants;
import it.gov.pagopa.pu.workflow.utils.TemporalTestUtils;
import it.gov.pagopa.pu.workflow.wf.pagopa.paymentsreporting.wfbrokersfetch.PaymentsReportingPagoPaBrokersFetchWF;
import it.gov.pagopa.pu.workflow.wf.pagopa.paymentsreporting.wfbrokersfetch.PaymentsReportingPagoPaBrokersFetchWFImpl;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PaymentsReportingPagoPaBrokersFetchSchedulerTest {

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

    ScheduleHandle expectedResult = Mockito.mock(ScheduleHandle.class);
    String taskQueue = TaskQueueConstants.TASK_QUEUE_LOW_PRIORITY;
    Mockito.when(workflowScheduleServiceMock.schedule(
        ScheduleEnum.PAYMENTS_REPORTING_PAGOPA_BROKERS_FETCH,
        PaymentsReportingPagoPaBrokersFetchWF.class,
        taskQueue,
        cronExpression
      ))
      .thenReturn(expectedResult);

    // When
    PaymentsReportingPagoPaBrokersFetchScheduler scheduler = new PaymentsReportingPagoPaBrokersFetchScheduler(workflowScheduleServiceMock, cronExpression);
    ScheduleHandle result = scheduler.getSchedule();

    // Then
    Assertions.assertSame(expectedResult, result);

    TemporalTestUtils.verifyWorkflowTaskQueueConfiguration(taskQueue, PaymentsReportingPagoPaBrokersFetchWFImpl.class);
  }
}
