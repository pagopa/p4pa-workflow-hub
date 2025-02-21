package it.gov.pagopa.pu.workflow.service;

import io.temporal.client.schedules.ScheduleActionStartWorkflow;
import io.temporal.client.schedules.ScheduleClient;
import io.temporal.client.schedules.ScheduleHandle;
import io.temporal.client.schedules.ScheduleSpec;
import it.gov.pagopa.payhub.activities.util.Utilities;
import it.gov.pagopa.pu.workflow.wf.pagopa.paymentsreporting.wfbrokersfetch.PaymentsReportingPagoPaBrokersFetchWF;
import it.gov.pagopa.pu.workflow.wf.pagopa.paymentsreporting.wfbrokersfetch.PaymentsReportingPagoPaBrokersFetchWFImpl;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertSame;

@ExtendWith(MockitoExtension.class)
class WorkflowScheduleServiceImplTest {
  @Mock
  private ScheduleClient scheduleClientMock;

  private WorkflowScheduleService workflowScheduleService;

  @BeforeEach
  void setUp() {
    workflowScheduleService = new WorkflowScheduleServiceImpl(scheduleClientMock);
  }

  @AfterEach
  void verifyNoMoreInteractions() {
    Mockito.verifyNoMoreInteractions(scheduleClientMock);
  }

  @Test
  void buildSchedule() {
    // Given
    Class<?> workflowInterface = PaymentsReportingPagoPaBrokersFetchWF.class;
    String taskQueue = PaymentsReportingPagoPaBrokersFetchWFImpl.TASK_QUEUE_BROKERS_PAYMENTS_REPORTING_PAGOPA_FETCH;
    String workflowId = "test-workflow-id";
    String scheduleId = "test-schedule-id";
    String cronExpression = "0/5 * * * *";
    ScheduleHandle expectedHandle = Mockito.mock(ScheduleHandle.class);

    ScheduleSpec expectedScheduleSpec = ScheduleSpec.newBuilder()
      .setCronExpressions(List.of(cronExpression))
      .setTimeZoneName(Utilities.ZONEID.getId())
      .build();

    Mockito.when(scheduleClientMock.createSchedule(
        Mockito.eq(scheduleId),
        Mockito.argThat(schedule -> schedule.getAction() instanceof ScheduleActionStartWorkflow scheduleAction &&
          scheduleAction.getWorkflowType().equals(workflowInterface.getSimpleName()) &&
          scheduleAction.getOptions().getTaskQueue().equals(taskQueue) &&
          scheduleAction.getOptions().getWorkflowId().equals(workflowId)
          && schedule.getSpec().equals(expectedScheduleSpec)),
        Mockito.any()
    ))
    .thenReturn(expectedHandle);

    // When
    ScheduleHandle actualHandle = workflowScheduleService.buildSchedule(
      workflowInterface, taskQueue, workflowId, scheduleId, cronExpression);

    // Then
    assertSame(expectedHandle, actualHandle);
  }

  @Test
  void getSchedule() {
    // Given
    String scheduleId = "test-schedule-id";
    ScheduleHandle expectedHandle = Mockito.mock(ScheduleHandle.class);

    Mockito.when(scheduleClientMock.getHandle(scheduleId))
      .thenReturn(expectedHandle);

    // When
    ScheduleHandle actualHandle = workflowScheduleService.getSchedule(scheduleId);

    // Then
    assertSame(expectedHandle, actualHandle);
  }
}
