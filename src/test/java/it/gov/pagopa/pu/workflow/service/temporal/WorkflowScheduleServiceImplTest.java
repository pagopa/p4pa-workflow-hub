package it.gov.pagopa.pu.workflow.service.temporal;

import io.temporal.client.schedules.*;
import it.gov.pagopa.payhub.activities.util.Utilities;
import it.gov.pagopa.pu.workflow.dto.generated.ScheduleInfoDTO;
import it.gov.pagopa.pu.workflow.enums.ScheduleEnum;
import it.gov.pagopa.pu.workflow.mapper.ScheduleInfoDTOMapper;
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
  @Mock
  private ScheduleInfoDTOMapper mapperMock;

  private WorkflowScheduleService workflowScheduleService;

  @BeforeEach
  void setUp() {
    workflowScheduleService = new WorkflowScheduleServiceImpl(scheduleClientMock, mapperMock);
  }

  @AfterEach
  void verifyNoMoreInteractions() {
    Mockito.verifyNoMoreInteractions(scheduleClientMock, mapperMock);
  }

  @Test
  void givenNewScheduleWhenScheduleThenCreateIt() {
    // Given
    Class<?> workflowInterface = PaymentsReportingPagoPaBrokersFetchWF.class;
    String taskQueue = PaymentsReportingPagoPaBrokersFetchWFImpl.TASK_QUEUE_BROKERS_PAYMENTS_REPORTING_PAGOPA_FETCH;
    ScheduleEnum scheduleId = ScheduleEnum.PAYMENTS_REPORTING_PAGOPA_BROKERS_FETCH;
    String cronExpression = "0/5 * * * *";
    ScheduleHandle previousHandle = Mockito.mock(ScheduleHandle.class);
    ScheduleHandle expectedHandle = Mockito.mock(ScheduleHandle.class);

    ScheduleSpec expectedScheduleSpec = ScheduleSpec.newBuilder()
      .setCronExpressions(List.of(cronExpression))
      .setTimeZoneName(Utilities.ZONEID.getId())
      .build();

    Mockito.when(previousHandle.describe())
        .thenThrow(new ScheduleException(null));
    Mockito.when(scheduleClientMock.getHandle(scheduleId.getValue()))
      .thenReturn(previousHandle);

    Mockito.when(scheduleClientMock.createSchedule(
        Mockito.eq(scheduleId.getValue()),
        Mockito.argThat(schedule -> schedule.getAction() instanceof ScheduleActionStartWorkflow scheduleAction &&
          scheduleAction.getWorkflowType().equals(workflowInterface.getSimpleName()) &&
          scheduleAction.getOptions().getTaskQueue().equals(taskQueue) &&
          scheduleAction.getOptions().getWorkflowId().equals(scheduleId.getValue())
          && schedule.getSpec().equals(expectedScheduleSpec)),
        Mockito.any()
    ))
    .thenReturn(expectedHandle);

    // When
    ScheduleHandle actualHandle = workflowScheduleService.schedule(scheduleId, workflowInterface, taskQueue, cronExpression);

    // Then
    assertSame(expectedHandle, actualHandle);
  }

  @Test
  void givenAlreadyExistentScheduleWhenScheduleThenReturnIt() {
    // Given
    Class<?> workflowInterface = PaymentsReportingPagoPaBrokersFetchWF.class;
    String taskQueue = PaymentsReportingPagoPaBrokersFetchWFImpl.TASK_QUEUE_BROKERS_PAYMENTS_REPORTING_PAGOPA_FETCH;
    ScheduleEnum scheduleId = ScheduleEnum.PAYMENTS_REPORTING_PAGOPA_BROKERS_FETCH;
    String cronExpression = "0/5 * * * *";
    ScheduleHandle previousHandle = Mockito.mock(ScheduleHandle.class);

    Mockito.when(previousHandle.describe())
      .thenReturn(Mockito.mock(ScheduleDescription.class));
    Mockito.when(scheduleClientMock.getHandle(scheduleId.getValue()))
      .thenReturn(previousHandle);

    // When
    ScheduleHandle actualHandle = workflowScheduleService.schedule(scheduleId, workflowInterface, taskQueue, cronExpression);

    // Then
    assertSame(previousHandle, actualHandle);
  }

  @Test
  void whenGetScheduleThenInvokeClient() {
    // Given
    ScheduleEnum scheduleId = ScheduleEnum.PAYMENTS_REPORTING_PAGOPA_BROKERS_FETCH;
    ScheduleHandle expectedHandle = Mockito.mock(ScheduleHandle.class);

    Mockito.when(scheduleClientMock.getHandle(scheduleId.getValue()))
      .thenReturn(expectedHandle);

    // When
    ScheduleHandle actualHandle = workflowScheduleService.getSchedule(scheduleId);

    // Then
    assertSame(expectedHandle, actualHandle);
  }

  @Test
  void whenGetScheduleInfoThenGetHandleAndCallMapper() {
    // Given
    ScheduleEnum scheduleId = ScheduleEnum.PAYMENTS_REPORTING_PAGOPA_BROKERS_FETCH;
    ScheduleHandle scheduleHandle = Mockito.mock(ScheduleHandle.class, Mockito.RETURNS_DEEP_STUBS);
    ScheduleInfoDTO expectedResult = new ScheduleInfoDTO();
    ScheduleInfo scheduleInfo = scheduleHandle.describe().getInfo();

    Mockito.when(scheduleClientMock.getHandle(scheduleId.getValue()))
      .thenReturn(scheduleHandle);
    Mockito.when(mapperMock.map(Mockito.eq(scheduleId), Mockito.same(scheduleInfo)))
      .thenReturn(expectedResult);

    // When
    ScheduleInfoDTO result = workflowScheduleService.getScheduleInfo(scheduleId);

    // Then
    assertSame(expectedResult, result);
  }
}
