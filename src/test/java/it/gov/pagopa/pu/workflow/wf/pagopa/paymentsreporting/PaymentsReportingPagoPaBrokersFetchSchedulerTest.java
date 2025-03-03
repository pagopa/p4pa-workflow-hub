package it.gov.pagopa.pu.workflow.wf.pagopa.paymentsreporting;

import io.temporal.client.schedules.ScheduleHandle;
import it.gov.pagopa.pu.workflow.service.WorkflowScheduleService;
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
    Mockito.when(workflowScheduleServiceMock.schedule(
        PaymentsReportingPagoPaBrokersFetchScheduler.PAYMENTS_REPORTING_PAGOPA_FETCH_SCHEDULEID,
        PaymentsReportingPagoPaBrokersFetchWF.class,
        PaymentsReportingPagoPaBrokersFetchWFImpl.TASK_QUEUE_BROKERS_PAYMENTS_REPORTING_PAGOPA_FETCH,
        cronExpression
      ))
      .thenReturn(expectedResult);

    // When
    PaymentsReportingPagoPaBrokersFetchScheduler scheduler = new PaymentsReportingPagoPaBrokersFetchScheduler(workflowScheduleServiceMock, cronExpression);
    ScheduleHandle result = scheduler.getSchedule();

    // Then
    Assertions.assertSame(expectedResult, result);
  }
}
