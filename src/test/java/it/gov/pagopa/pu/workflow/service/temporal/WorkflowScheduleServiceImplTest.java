package it.gov.pagopa.pu.workflow.service.temporal;

import io.temporal.client.WorkflowNotFoundException;
import io.temporal.client.schedules.*;
import it.gov.pagopa.payhub.activities.util.Utilities;
import it.gov.pagopa.pu.workflow.dto.generated.RecentScheduleExecutionInfoDTO;
import it.gov.pagopa.pu.workflow.dto.generated.ScheduleInfoDTO;
import it.gov.pagopa.pu.workflow.dto.generated.WorkflowStatusDTO;
import it.gov.pagopa.pu.workflow.enums.ScheduleEnum;
import it.gov.pagopa.pu.workflow.mapper.ScheduleInfoDTOMapper;
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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WorkflowScheduleServiceImplTest {

  public static final ScheduleEnum SCHEDULE_ID = ScheduleEnum.PAYMENTS_REPORTING_PAGOPA_BROKERS_FETCH;

  @Mock
  private ScheduleClient scheduleClientMock;
  @Mock
  private ScheduleInfoDTOMapper mapperMock;
  @Mock
  private WorkflowService workflowServiceMock;

  private WorkflowScheduleService workflowScheduleService;

  @BeforeEach
  void setUp() {
    workflowScheduleService = new WorkflowScheduleServiceImpl(
      scheduleClientMock,
      mapperMock,
      workflowServiceMock
    );
  }

  @AfterEach
  void verifyNoMoreInteractions() {
    Mockito.verifyNoMoreInteractions(
      scheduleClientMock,
      mapperMock,
      workflowServiceMock
    );
  }

  @Test
  void givenNewScheduleWhenScheduleThenCreateIt() {
    // Given
    Class<?> workflowInterface = PaymentsReportingPagoPaBrokersFetchWF.class;
    String taskQueue = "DUMMYTASKQUEUE";
    ScheduleEnum scheduleId = SCHEDULE_ID;
    String cronExpression = "0/5 * * * *";
    ScheduleHandle previousHandle = mock(ScheduleHandle.class);

    when(previousHandle.describe())
        .thenThrow(new ScheduleException(null));
    when(scheduleClientMock.getHandle(scheduleId.getValue()))
      .thenReturn(previousHandle);

    ScheduleHandle expectedHandle = configureCreateScheduleMock(scheduleId, workflowInterface, taskQueue, cronExpression, null);

    // When
    ScheduleHandle actualHandle = workflowScheduleService.schedule(scheduleId, workflowInterface, taskQueue, cronExpression);

    // Then
    assertSame(expectedHandle, actualHandle);

    Mockito.verifyNoMoreInteractions(previousHandle);
  }

  private ScheduleHandle configureCreateScheduleMock(ScheduleEnum scheduleId, Class<?> workflowInterface, String taskQueue, String cronExpression, String argument) {
    ScheduleHandle expectedHandle = mock(ScheduleHandle.class);
    ScheduleSpec expectedScheduleSpec = ScheduleSpec.newBuilder()
      .setCronExpressions(List.of(cronExpression))
      .setTimeZoneName(Utilities.ZONEID.getId())
      .build();

    when(scheduleClientMock.createSchedule(
        Mockito.eq(scheduleId.getValue()),
        Mockito.argThat(schedule -> schedule.getAction() instanceof ScheduleActionStartWorkflow scheduleAction &&
          scheduleAction.getWorkflowType().equals(workflowInterface.getSimpleName()) &&
          checkArgument(scheduleAction, argument) &&
          scheduleAction.getOptions().getTaskQueue().equals(taskQueue) &&
          scheduleAction.getOptions().getWorkflowId().equals(scheduleId.getValue())
          && schedule.getSpec().equals(expectedScheduleSpec)),
        Mockito.any()
    ))
    .thenReturn(expectedHandle);

    return expectedHandle;
  }

  @Test
  void givenAlreadyExistentScheduleWhenScheduleThenReturnIt() {
    // Given
    Class<?> workflowInterface = PaymentsReportingPagoPaBrokersFetchWF.class;
    String taskQueue = "DUMMYTASKQUEUE";
    ScheduleEnum scheduleId = SCHEDULE_ID;
    String cronExpression = "0/5 * * * *";
    ScheduleHandle previousHandle = mock(ScheduleHandle.class, Mockito.RETURNS_DEEP_STUBS);

    when(previousHandle.describe().getSchedule().getSpec().getCronExpressions())
      .thenReturn(List.of(cronExpression));
    when(scheduleClientMock.getHandle(scheduleId.getValue()))
      .thenReturn(previousHandle);

    // When
    ScheduleHandle actualHandle = workflowScheduleService.schedule(scheduleId, workflowInterface, taskQueue, cronExpression);

    // Then
    assertSame(previousHandle, actualHandle);
  }

  @Test
  void givenAlreadyExistentScheduleWithDifferentScheduleWhenScheduleThenReturnIt() {
    // Given
    Class<?> workflowInterface = PaymentsReportingPagoPaBrokersFetchWF.class;
    String taskQueue = "DUMMYTASKQUEUE";
    ScheduleEnum scheduleId = SCHEDULE_ID;
    String cronExpression = "0/5 * * * *";
    ScheduleHandle previousHandle = mock(ScheduleHandle.class, Mockito.RETURNS_DEEP_STUBS);

    when(previousHandle.describe().getSchedule().getSpec().getCronExpressions())
      .thenReturn(List.of("OTHERSCHEDULE"));
    when(scheduleClientMock.getHandle(scheduleId.getValue()))
      .thenReturn(previousHandle);

    configureCreateScheduleMock(scheduleId, workflowInterface, taskQueue, cronExpression, null);

    // When
    ScheduleHandle actualHandle = workflowScheduleService.schedule(scheduleId, workflowInterface, taskQueue, cronExpression);

    // Then
    assertSame(previousHandle, actualHandle);

    verify(previousHandle).delete();
  }

  @Test
  void whenGetScheduleInfoThenGetHandleAndCallMapper() {
    //Given
    ScheduleEnum scheduleId = SCHEDULE_ID;

    WorkflowStatusDTO workflowStatusDTO = new WorkflowStatusDTO();
    when(workflowServiceMock.getWorkflowStatus("WFTYPE-ON-DEMAND"))
      .thenReturn(workflowStatusDTO);

    ScheduleHandle scheduleHandle = mock(ScheduleHandle.class, Mockito.RETURNS_DEEP_STUBS);
    ScheduleInfo scheduleInfo = scheduleHandle.describe().getInfo();

    when(scheduleHandle.describe().getSchedule().getAction())
      .thenReturn(ScheduleActionStartWorkflow.newBuilder().setWorkflowType("WFTYPE").build());

    when(scheduleClientMock.getHandle(scheduleId.getValue()))
      .thenReturn(scheduleHandle);

    ScheduleInfoDTO mapped = new ScheduleInfoDTO();
    mapped.setRecentActions(List.of());

    when(mapperMock.map(Mockito.eq(scheduleId), Mockito.same(scheduleInfo)))
      .thenReturn(mapped);

    // When
    ScheduleInfoDTO result = workflowScheduleService.getScheduleInfo(scheduleId);

    // Then
    assertSame(mapped, result);
    assertSame(workflowStatusDTO, result.getLastManualExecution());
    assertNull(result.getLastExecution());
  }

  @Test
  void givenRecentActionsPresentWhenGetScheduleInfoThenLastExecutionIsMaxOfThem() {
    // Given
    ScheduleEnum scheduleId = SCHEDULE_ID;

    WorkflowStatusDTO workflowStatusDTO = new WorkflowStatusDTO();
    when(workflowServiceMock.getWorkflowStatus("WFTYPE-ON-DEMAND"))
      .thenReturn(workflowStatusDTO);

    ScheduleHandle scheduleHandle = mock(ScheduleHandle.class, Mockito.RETURNS_DEEP_STUBS);
    ScheduleInfo scheduleInfo = scheduleHandle.describe().getInfo();

    when(scheduleHandle.describe().getSchedule().getAction())
      .thenReturn(ScheduleActionStartWorkflow.newBuilder().setWorkflowType("WFTYPE").build());

    when(scheduleClientMock.getHandle(scheduleId.getValue()))
      .thenReturn(scheduleHandle);

    OffsetDateTime t1 = OffsetDateTime.now().minusHours(2);
    OffsetDateTime t2 = OffsetDateTime.now();

    RecentScheduleExecutionInfoDTO r1 = new RecentScheduleExecutionInfoDTO();
    r1.setStartedAt(t1);

    RecentScheduleExecutionInfoDTO r2 = new RecentScheduleExecutionInfoDTO();
    r2.setStartedAt(t2);

    ScheduleInfoDTO mapped = new ScheduleInfoDTO();
    mapped.setRecentActions(List.of(r1, r2));

    when(mapperMock.map(Mockito.eq(scheduleId), Mockito.same(scheduleInfo)))
      .thenReturn(mapped);

    // When
    ScheduleInfoDTO result = workflowScheduleService.getScheduleInfo(scheduleId);

    // Then
    assertEquals(t2, result.getLastExecution());
  }

  @Test
  void givenExecutionDateTimeIsGreaterThanRecentActionsWhenGetScheduleInfoThenLastExecutionIsExecutionDateTime() {
    // Given
    ScheduleEnum scheduleId = SCHEDULE_ID;

    OffsetDateTime recent = OffsetDateTime.now().minusHours(1);
    OffsetDateTime executionDate = OffsetDateTime.now();

    WorkflowStatusDTO workflowStatusDTO = new WorkflowStatusDTO();
    workflowStatusDTO.setExecutionDateTime(executionDate);

    when(workflowServiceMock.getWorkflowStatus("WFTYPE-ON-DEMAND"))
      .thenReturn(workflowStatusDTO);

    ScheduleHandle scheduleHandle = mock(ScheduleHandle.class, Mockito.RETURNS_DEEP_STUBS);
    ScheduleInfo scheduleInfo = scheduleHandle.describe().getInfo();

    when(scheduleHandle.describe().getSchedule().getAction())
      .thenReturn(ScheduleActionStartWorkflow.newBuilder().setWorkflowType("WFTYPE").build());

    when(scheduleClientMock.getHandle(scheduleId.getValue()))
      .thenReturn(scheduleHandle);

    RecentScheduleExecutionInfoDTO r = new RecentScheduleExecutionInfoDTO();
    r.setStartedAt(recent);

    ScheduleInfoDTO mapped = new ScheduleInfoDTO();
    mapped.setRecentActions(List.of(r));

    when(mapperMock.map(Mockito.eq(scheduleId), Mockito.same(scheduleInfo)))
      .thenReturn(mapped);

    // When
    ScheduleInfoDTO result = workflowScheduleService.getScheduleInfo(scheduleId);

    // Then
    assertEquals(executionDate, result.getLastExecution());
  }

  @Test
  void givenNoRecentActionsAndNoExecutionDateTimeWhenGetScheduleInfoThenLastExecutionIsNull() {
    // Given
    ScheduleEnum scheduleId = SCHEDULE_ID;

    WorkflowStatusDTO workflowStatusDTO = new WorkflowStatusDTO();
    when(workflowServiceMock.getWorkflowStatus("WFTYPE-ON-DEMAND"))
      .thenReturn(workflowStatusDTO);

    ScheduleHandle scheduleHandle = mock(ScheduleHandle.class, Mockito.RETURNS_DEEP_STUBS);
    ScheduleInfo scheduleInfo = scheduleHandle.describe().getInfo();

    when(scheduleHandle.describe().getSchedule().getAction())
      .thenReturn(ScheduleActionStartWorkflow.newBuilder().setWorkflowType("WFTYPE").build());

    when(scheduleClientMock.getHandle(scheduleId.getValue()))
      .thenReturn(scheduleHandle);

    ScheduleInfoDTO mapped = new ScheduleInfoDTO();
    mapped.setRecentActions(List.of());

    when(mapperMock.map(Mockito.eq(scheduleId), Mockito.same(scheduleInfo)))
      .thenReturn(mapped);

    // When
    ScheduleInfoDTO result = workflowScheduleService.getScheduleInfo(scheduleId);

    // Then
    assertNull(result.getLastExecution());
  }

  @Test
  void givenManualWorkflowNotFoundWhenGetScheduleInfoThenLastExecutionComesFromRecentActions() {
    // Given
    ScheduleEnum scheduleId = SCHEDULE_ID;

    when(workflowServiceMock.getWorkflowStatus("WFTYPE-ON-DEMAND"))
      .thenThrow(mock(WorkflowNotFoundException.class));

    ScheduleHandle scheduleHandle = mock(ScheduleHandle.class, Mockito.RETURNS_DEEP_STUBS);
    ScheduleInfo scheduleInfo = scheduleHandle.describe().getInfo();

    when(scheduleHandle.describe().getSchedule().getAction())
      .thenReturn(ScheduleActionStartWorkflow.newBuilder().setWorkflowType("WFTYPE").build());

    when(scheduleClientMock.getHandle(scheduleId.getValue()))
      .thenReturn(scheduleHandle);

    OffsetDateTime t1 = OffsetDateTime.now().minusHours(2);
    OffsetDateTime t2 = OffsetDateTime.now();

    RecentScheduleExecutionInfoDTO r1 = new RecentScheduleExecutionInfoDTO();
    r1.setStartedAt(t1);
    RecentScheduleExecutionInfoDTO r2 = new RecentScheduleExecutionInfoDTO();
    r2.setStartedAt(t2);

    ScheduleInfoDTO mapped = new ScheduleInfoDTO();
    mapped.setRecentActions(List.of(r1, r2));

    when(mapperMock.map(Mockito.eq(scheduleId), Mockito.same(scheduleInfo)))
      .thenReturn(mapped);

    // When
    ScheduleInfoDTO result = workflowScheduleService.getScheduleInfo(scheduleId);

    // Then
    assertNull(result.getLastManualExecution());
    assertEquals(t2, result.getLastExecution());
  }

  @Test
  void test() {
    // Given
    Class<?> workflowInterface = PaymentsReportingPagoPaBrokersFetchWF.class;
    String taskQueue = "DUMMYTASKQUEUE";
    ScheduleEnum scheduleId = SCHEDULE_ID;
    String cronExpression = "0/5 * * * *";
    ScheduleHandle previousHandle = mock(ScheduleHandle.class);

    when(previousHandle.describe())
      .thenThrow(new ScheduleException(null));
    when(scheduleClientMock.getHandle(scheduleId.getValue()))
      .thenReturn(previousHandle);

    ScheduleHandle expectedHandle = configureCreateScheduleMock(scheduleId, workflowInterface, taskQueue, cronExpression, "argument1");

    // When
    ScheduleHandle actualHandle = workflowScheduleService.schedule(scheduleId, workflowInterface, taskQueue, cronExpression, "argument1");

    // Then
    assertSame(expectedHandle, actualHandle);

    Mockito.verifyNoMoreInteractions(previousHandle);
  }

  private static boolean checkArgument(ScheduleActionStartWorkflow scheduleAction, String actualArgument) {
    if(actualArgument==null) {
      return true;
    }
    String expectedArgument = scheduleAction.getArguments().get(0, String.class);
    return expectedArgument.equals(actualArgument);
  }

}
