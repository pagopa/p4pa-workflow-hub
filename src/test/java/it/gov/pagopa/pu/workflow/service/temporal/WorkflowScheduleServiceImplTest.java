package it.gov.pagopa.pu.workflow.service.temporal;

import io.temporal.client.WorkflowNotFoundException;
import io.temporal.client.schedules.*;
import it.gov.pagopa.payhub.activities.util.Utilities;
import it.gov.pagopa.pu.workflow.dto.generated.RecentScheduleExecutionInfoDTO;
import it.gov.pagopa.pu.workflow.dto.generated.ScheduleInfoDTO;
import it.gov.pagopa.pu.workflow.dto.generated.WorkflowStatusDTO;
import it.gov.pagopa.pu.workflow.enums.ScheduleEnum;
import it.gov.pagopa.pu.workflow.mapper.ScheduleInfoDTOMapper;
import it.gov.pagopa.pu.workflow.utilities.TaskQueueConstants;
import it.gov.pagopa.pu.workflow.wf.pagopa.paymentsreporting.wfbrokersfetch.PaymentsReportingPagoPaBrokersFetchWF;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class WorkflowScheduleServiceImplTest {
  @Mock
  private ScheduleClient scheduleClientMock;
  @Mock
  private ScheduleInfoDTOMapper mapperMock;
  @Mock
  private WorkflowService workflowServiceMock;

  private WorkflowScheduleService workflowScheduleService;

  @BeforeEach
  void setUp() {
    workflowScheduleService = new WorkflowScheduleServiceImpl(scheduleClientMock, mapperMock, workflowServiceMock);
  }

  @AfterEach
  void verifyNoMoreInteractions() {
    Mockito.verifyNoMoreInteractions(scheduleClientMock, mapperMock, workflowServiceMock);
  }

  @Test
  void givenNewScheduleWhenScheduleThenCreateIt() {
    // Given
    Class<?> workflowInterface = PaymentsReportingPagoPaBrokersFetchWF.class;
    String taskQueue = TaskQueueConstants.TASK_QUEUE_LOW_PRIORITY;
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
    String taskQueue = TaskQueueConstants.TASK_QUEUE_LOW_PRIORITY;
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
    ScheduleEnum scheduleId = ScheduleEnum.PAYMENTS_REPORTING_PAGOPA_BROKERS_FETCH;

    WorkflowStatusDTO workflowStatusDTO = new WorkflowStatusDTO();
    Mockito.when(workflowServiceMock.getWorkflowStatus("SynchronizeTaxonomyPagoPaFetchWF-ON-DEMAND"))
      .thenReturn(workflowStatusDTO);

    ScheduleHandle scheduleHandle = Mockito.mock(ScheduleHandle.class, Mockito.RETURNS_DEEP_STUBS);
    ScheduleInfo scheduleInfo = scheduleHandle.describe().getInfo();

    Mockito.when(scheduleClientMock.getHandle(scheduleId.getValue()))
      .thenReturn(scheduleHandle);

    ScheduleInfoDTO mapped = new ScheduleInfoDTO();
    mapped.setRecentActions(List.of());

    Mockito.when(mapperMock.map(Mockito.eq(scheduleId), Mockito.same(scheduleInfo)))
      .thenReturn(mapped);

    ScheduleInfoDTO result = workflowScheduleService.getScheduleInfo(scheduleId);

    assertSame(mapped, result);
    assertSame(workflowStatusDTO, result.getLastManualExecution());
    assertNull(result.getLastExecution());
  }

  @Test
  void whenRecentActionsPresentThenLastExecutionIsMaxOfThem() {
    ScheduleEnum scheduleId = ScheduleEnum.PAYMENTS_REPORTING_PAGOPA_BROKERS_FETCH;

    WorkflowStatusDTO workflowStatusDTO = new WorkflowStatusDTO();
    Mockito.when(workflowServiceMock.getWorkflowStatus(Mockito.any()))
      .thenReturn(workflowStatusDTO);

    ScheduleHandle scheduleHandle = Mockito.mock(ScheduleHandle.class, Mockito.RETURNS_DEEP_STUBS);
    ScheduleInfo scheduleInfo = scheduleHandle.describe().getInfo();

    Mockito.when(scheduleClientMock.getHandle(scheduleId.getValue()))
      .thenReturn(scheduleHandle);

    OffsetDateTime t1 = OffsetDateTime.now().minusHours(2);
    OffsetDateTime t2 = OffsetDateTime.now();

    RecentScheduleExecutionInfoDTO r1 = new RecentScheduleExecutionInfoDTO();
    r1.setStartedAt(t1);

    RecentScheduleExecutionInfoDTO r2 = new RecentScheduleExecutionInfoDTO();
    r2.setStartedAt(t2);

    ScheduleInfoDTO mapped = new ScheduleInfoDTO();
    mapped.setRecentActions(List.of(r1, r2));

    Mockito.when(mapperMock.map(Mockito.eq(scheduleId), Mockito.same(scheduleInfo)))
      .thenReturn(mapped);

    ScheduleInfoDTO result = workflowScheduleService.getScheduleInfo(scheduleId);

    assertEquals(t2, result.getLastExecution());
  }

  @Test
  void whenExecutionDateTimeIsGreaterThanRecentActionsThenLastExecutionIsExecutionDateTime() {
    ScheduleEnum scheduleId = ScheduleEnum.PAYMENTS_REPORTING_PAGOPA_BROKERS_FETCH;

    OffsetDateTime recent = OffsetDateTime.now().minusHours(1);
    OffsetDateTime executionDate = OffsetDateTime.now();

    WorkflowStatusDTO workflowStatusDTO = new WorkflowStatusDTO();
    workflowStatusDTO.setExecutionDateTime(executionDate);

    Mockito.when(workflowServiceMock.getWorkflowStatus(Mockito.any()))
      .thenReturn(workflowStatusDTO);

    ScheduleHandle scheduleHandle = Mockito.mock(ScheduleHandle.class, Mockito.RETURNS_DEEP_STUBS);
    ScheduleInfo scheduleInfo = scheduleHandle.describe().getInfo();

    Mockito.when(scheduleClientMock.getHandle(scheduleId.getValue()))
      .thenReturn(scheduleHandle);

    RecentScheduleExecutionInfoDTO r = new RecentScheduleExecutionInfoDTO();
    r.setStartedAt(recent);

    ScheduleInfoDTO mapped = new ScheduleInfoDTO();
    mapped.setRecentActions(List.of(r));

    Mockito.when(mapperMock.map(Mockito.eq(scheduleId), Mockito.same(scheduleInfo)))
      .thenReturn(mapped);

    ScheduleInfoDTO result = workflowScheduleService.getScheduleInfo(scheduleId);

    assertEquals(executionDate, result.getLastExecution());
  }

  @Test
  void whenNoRecentActionsAndNoExecutionDateTimeThenLastExecutionIsNull() {
    ScheduleEnum scheduleId = ScheduleEnum.PAYMENTS_REPORTING_PAGOPA_BROKERS_FETCH;

    WorkflowStatusDTO workflowStatusDTO = new WorkflowStatusDTO();
    Mockito.when(workflowServiceMock.getWorkflowStatus(Mockito.any()))
      .thenReturn(workflowStatusDTO);

    ScheduleHandle scheduleHandle = Mockito.mock(ScheduleHandle.class, Mockito.RETURNS_DEEP_STUBS);
    ScheduleInfo scheduleInfo = scheduleHandle.describe().getInfo();

    Mockito.when(scheduleClientMock.getHandle(scheduleId.getValue()))
      .thenReturn(scheduleHandle);

    ScheduleInfoDTO mapped = new ScheduleInfoDTO();
    mapped.setRecentActions(List.of());

    Mockito.when(mapperMock.map(Mockito.eq(scheduleId), Mockito.same(scheduleInfo)))
      .thenReturn(mapped);

    ScheduleInfoDTO result = workflowScheduleService.getScheduleInfo(scheduleId);

    assertNull(result.getLastExecution());
  }

  @Test
  void whenManualWorkflowNotFoundThenLastExecutionComesFromRecentActions() {
    ScheduleEnum scheduleId = ScheduleEnum.PAYMENTS_REPORTING_PAGOPA_BROKERS_FETCH;

    Mockito.when(workflowServiceMock.getWorkflowStatus(Mockito.any()))
      .thenThrow(Mockito.mock(WorkflowNotFoundException.class));

    ScheduleHandle scheduleHandle = Mockito.mock(ScheduleHandle.class, Mockito.RETURNS_DEEP_STUBS);
    ScheduleInfo scheduleInfo = scheduleHandle.describe().getInfo();

    Mockito.when(scheduleClientMock.getHandle(scheduleId.getValue()))
      .thenReturn(scheduleHandle);

    OffsetDateTime t1 = OffsetDateTime.now().minusHours(2);
    OffsetDateTime t2 = OffsetDateTime.now();

    RecentScheduleExecutionInfoDTO r1 = new RecentScheduleExecutionInfoDTO();
    r1.setStartedAt(t1);
    RecentScheduleExecutionInfoDTO r2 = new RecentScheduleExecutionInfoDTO();
    r2.setStartedAt(t2);

    ScheduleInfoDTO mapped = new ScheduleInfoDTO();
    mapped.setRecentActions(List.of(r1, r2));

    Mockito.when(mapperMock.map(Mockito.eq(scheduleId), Mockito.same(scheduleInfo)))
      .thenReturn(mapped);

    ScheduleInfoDTO result = workflowScheduleService.getScheduleInfo(scheduleId);

    assertNull(result.getLastManualExecution());
    assertEquals(t2, result.getLastExecution());
  }
}
