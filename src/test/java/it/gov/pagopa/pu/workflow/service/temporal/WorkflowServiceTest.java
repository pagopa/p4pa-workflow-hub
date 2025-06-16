package it.gov.pagopa.pu.workflow.service.temporal;

import io.temporal.api.common.v1.WorkflowExecution;
import io.temporal.api.workflow.v1.WorkflowExecutionInfo;
import io.temporal.client.WorkflowClient;
import io.temporal.client.WorkflowOptions;
import io.temporal.client.WorkflowServiceException;
import io.temporal.client.WorkflowStub;
import io.temporal.internal.client.WorkflowClientHelper;
import io.temporal.serviceclient.WorkflowServiceStubs;
import it.gov.pagopa.pu.workflow.dto.generated.WorkflowStatusDTO;
import it.gov.pagopa.pu.workflow.exception.custom.WorkflowInternalErrorException;
import it.gov.pagopa.pu.workflow.exception.custom.WorkflowNotFoundException;
import it.gov.pagopa.pu.workflow.mapper.WorkflowStatusDTOMapper;
import it.gov.pagopa.pu.workflow.utilities.TaskQueueConstants;
import it.gov.pagopa.pu.workflow.wf.debtposition.expirationdp.wfexpiration.CheckDebtPositionExpirationWF;
import it.gov.pagopa.pu.workflow.wf.ingestionflow.paymentsreporting.wfingestion.PaymentsReportingIngestionWF;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WorkflowServiceTest {

  @Mock
  private WorkflowClient workflowClientMock;
  @Mock
  private WorkflowStatusDTOMapper mapperMock;
  @Mock
  private PaymentsReportingIngestionWF wfMock;
  @Mock
  private CheckDebtPositionExpirationWF checkDebtPositionExpirationWFMock;
  @Mock(answer = Answers.RETURNS_DEEP_STUBS)
  private WorkflowExecutionInfo workflowExecutionInfoMock;
  @Mock
  private WorkflowServiceStubs workflowServiceStubsMock;

  private final String namespace = "NAMESPACE";
  private WorkflowService workflowService;

  @BeforeEach
  void init() {
    workflowService = Mockito.spy(new WorkflowServiceImpl(namespace, workflowClientMock, mapperMock));
  }

  @AfterEach
  void verifyNoMoreInteractions() {
    Mockito.verifyNoMoreInteractions(workflowClientMock, wfMock, checkDebtPositionExpirationWFMock, mapperMock);
  }

  @Test
  void whenIngestThenOk() {
    // Given
    long ingestionFlowFileId = 1L;
    String workflowId = String.valueOf(ingestionFlowFileId);

    String taskQueue = TaskQueueConstants.TASK_QUEUE_IMPORT_MEDIUM_PRIORITY;
    when(workflowClientMock.newWorkflowStub(
      Mockito.eq(PaymentsReportingIngestionWF.class),
      Mockito.<WorkflowOptions>argThat(options ->
        taskQueue.equals(options.getTaskQueue()) &&
          workflowId.equals(options.getWorkflowId())
      )))
      .thenReturn(wfMock);

    // When
    PaymentsReportingIngestionWF result = workflowService.buildWorkflowStub(PaymentsReportingIngestionWF.class, taskQueue, workflowId);

    // Then
    Assertions.assertSame(wfMock, result);
  }

  @Test
  void givenGetWorkflowStatusThenSuccess() {
    // Given
    String workflowId = "test-workflow-id";
    WorkflowStatusDTO expectedResult = new WorkflowStatusDTO();

    when(workflowClientMock.getWorkflowServiceStubs()).thenReturn(workflowServiceStubsMock);
    when(mapperMock.map(workflowId, workflowExecutionInfoMock))
      .thenReturn(expectedResult);

    try (MockedStatic<WorkflowClientHelper> mockedStatic = mockStatic(WorkflowClientHelper.class)) {
      mockedStatic.when(() -> WorkflowClientHelper.describeWorkflowInstance(
          any(),
          eq(namespace),
          any(),
          any()))
        .thenReturn(workflowExecutionInfoMock);

      // When
      WorkflowStatusDTO result = workflowService.getWorkflowStatus(workflowId);

      // Then
      assertSame(expectedResult, result);
    }
  }

  @Test
  void givenGetWorkflowStatusWhenWorkflowNotFoundThenThrowWorkflowNotFoundException() {
    String workflowId = "test-workflow-id";

    when(mapperMock.map(workflowId, workflowExecutionInfoMock))
      .thenThrow(new io.temporal.client.WorkflowNotFoundException(WorkflowExecution.newBuilder().setWorkflowId(workflowId).build(), "Workflow not found", null));

    when(workflowClientMock.getWorkflowServiceStubs()).thenReturn(workflowServiceStubsMock);

    try (MockedStatic<WorkflowClientHelper> mockedStatic = mockStatic(WorkflowClientHelper.class)) {
      mockedStatic.when(() -> WorkflowClientHelper.describeWorkflowInstance(
          any(),
          eq(namespace),
          any(),
          any()))
        .thenReturn(workflowExecutionInfoMock);

      WorkflowNotFoundException exception = assertThrows(
        WorkflowNotFoundException.class,
        () -> workflowService.getWorkflowStatus(workflowId)
      );

      assertEquals("workflowId='test-workflow-id', runId='', workflowType='Workflow not found'", exception.getMessage());
    }
  }

  @Test
  void givenGetWorkflowStatusWhenInternalErrorThenThrowWorkflowInternalErrorException() {
    String workflowId = "test-workflow-id";

    when(workflowClientMock.getWorkflowServiceStubs()).thenThrow(new WorkflowServiceException(WorkflowExecution.newBuilder().setWorkflowId(workflowId).build(), "Generic Error", null));

    try (MockedStatic<WorkflowClientHelper> mockedStatic = mockStatic(WorkflowClientHelper.class)) {
      mockedStatic.when(() -> WorkflowClientHelper.describeWorkflowInstance(
          any(),
          eq(namespace),
          any(),
          any()))
        .thenReturn(workflowExecutionInfoMock);

      WorkflowInternalErrorException exception = assertThrows(
        WorkflowInternalErrorException.class,
        () -> workflowService.getWorkflowStatus(workflowId)
      );

      assertEquals("workflowId='test-workflow-id', runId='', workflowType='Generic Error'", exception.getMessage());
    }
  }

  @Test
  void testBuildUntypedWorkflowStub() {
    // Given
    String taskQueue = "test-task-queue";
    String workflowId = "test-workflow-id";
    WorkflowStub expectedStub = Mockito.mock(WorkflowStub.class);
    Class<?> workflowClass = Object.class;

    when(workflowClientMock.newUntypedWorkflowStub(
      eq(workflowClass.getSimpleName()),
      argThat(options -> taskQueue.equals(options.getTaskQueue()) && workflowId.equals(options.getWorkflowId()))
    )).thenReturn(expectedStub);

    // When
    WorkflowStub result = workflowService.buildUntypedWorkflowStub(workflowClass, taskQueue, workflowId);

    // Then
    assertEquals(expectedStub, result);
  }

  @Test
  void testBuildWorkflowDelayed() {
    String taskQueue = "test-task-queue";
    String workflowId = "test-workflow-id";

    Duration duration = Duration.ofDays(1);
    when(workflowClientMock.newWorkflowStub(
      Mockito.eq(CheckDebtPositionExpirationWF.class),
      Mockito.<WorkflowOptions>argThat(options -> taskQueue.equals(options.getTaskQueue()) &&
        workflowId.equals(options.getWorkflowId()) && duration.equals(options.getStartDelay()))
    )).thenReturn(checkDebtPositionExpirationWFMock);

    CheckDebtPositionExpirationWF result = workflowService.buildWorkflowStubDelayed(CheckDebtPositionExpirationWF.class,
      taskQueue,
      workflowId,
      duration);

    Assertions.assertSame(checkDebtPositionExpirationWFMock, result);
  }

  @Test
  void givenNegativeDurationWhenBuildWorkflowDelayedThenDurationZERO() {
    String taskQueue = "test-task-queue";
    String workflowId = "test-workflow-id";

    Duration expectedDuration = Duration.ZERO;
    when(workflowClientMock.newWorkflowStub(
      Mockito.eq(CheckDebtPositionExpirationWF.class),
      Mockito.<WorkflowOptions>argThat(options -> taskQueue.equals(options.getTaskQueue()) &&
        workflowId.equals(options.getWorkflowId()) && expectedDuration.equals(options.getStartDelay()))
    )).thenReturn(checkDebtPositionExpirationWFMock);

    CheckDebtPositionExpirationWF result = workflowService.buildWorkflowStubDelayed(CheckDebtPositionExpirationWF.class,
      taskQueue,
      workflowId,
      Duration.between(LocalDateTime.now(), LocalDateTime.now().minusDays(1)));

    Assertions.assertSame(checkDebtPositionExpirationWFMock, result);
  }

  @Test
  void testBuildWorkflowScheduledWithLocalDate() {
    String taskQueue = "test-task-queue";
    String workflowId = "test-workflow-id";

    LocalDate localDate = LocalDate.now().plusDays(1);


    CheckDebtPositionExpirationWF expectedResult = mock(CheckDebtPositionExpirationWF.class);
    doReturn(expectedResult)
      .when(workflowService)
      .buildWorkflowStubScheduled(
        CheckDebtPositionExpirationWF.class,
        taskQueue,
        workflowId,
        LocalDateTime.of(localDate, LocalTime.MIDNIGHT)
      );

    CheckDebtPositionExpirationWF result = workflowService.buildWorkflowStubScheduled(CheckDebtPositionExpirationWF.class,
      taskQueue, workflowId, localDate);

    Assertions.assertSame(expectedResult, result);

  }

  @Test
  void testBuildWorkflowScheduledWithLocalDateTime() {
    String taskQueue = "test-task-queue";
    String workflowId = "test-workflow-id";

    LocalDateTime nextSchedule = LocalDateTime.now().plusDays(1);
    Duration expectedMaxDuration = Duration.ofDays(1);

    workflowService.buildWorkflowStubScheduled(CheckDebtPositionExpirationWF.class,
      taskQueue,
      workflowId,
      nextSchedule);

    ArgumentCaptor<WorkflowOptions> optionsCaptor = ArgumentCaptor.forClass(WorkflowOptions.class);

    verify(workflowClientMock).newWorkflowStub(
      eq(CheckDebtPositionExpirationWF.class),
      optionsCaptor.capture()
    );

    WorkflowOptions capturedOptions = optionsCaptor.getValue();
    Duration actualStartDelay = capturedOptions.getStartDelay();
    assertNotNull(actualStartDelay);
    Duration diff = expectedMaxDuration.minus(actualStartDelay);
    assertTrue(diff.toSeconds() >= 0);
    assertTrue(diff.toSeconds() < 5);
    assertEquals(taskQueue, capturedOptions.getTaskQueue());
    assertEquals(workflowId, capturedOptions.getWorkflowId());
  }

  @Test
  void testBuildWorkflowScheduledWithOffsetDateTime() {
    String taskQueue = "test-task-queue";
    String workflowId = "test-workflow-id";

    Duration expectedMaxDuration = Duration.ofDays(1);
    OffsetDateTime nextSchedule = OffsetDateTime.now(ZoneOffset.MAX).plus(expectedMaxDuration);

    workflowService.buildWorkflowStubScheduled(CheckDebtPositionExpirationWF.class,
      taskQueue,
      workflowId,
      nextSchedule);

    ArgumentCaptor<WorkflowOptions> optionsCaptor = ArgumentCaptor.forClass(WorkflowOptions.class);

    verify(workflowClientMock).newWorkflowStub(
      eq(CheckDebtPositionExpirationWF.class),
      optionsCaptor.capture()
    );

    WorkflowOptions capturedOptions = optionsCaptor.getValue();
    Duration actualStartDelay = capturedOptions.getStartDelay();
    assertNotNull(actualStartDelay);
    Duration diff = expectedMaxDuration.minus(actualStartDelay);
    assertTrue(diff.toSeconds() >= 0);
    assertTrue(diff.toSeconds() < 5);
    assertEquals(taskQueue, capturedOptions.getTaskQueue());
    assertEquals(workflowId, capturedOptions.getWorkflowId());
  }

  @Test
  void whenCancelWorkflowThenOk() {
    // Given
    String workflowId = "WFID";
    WorkflowStub stubMock = Mockito.mock(WorkflowStub.class);

    when(workflowClientMock.newUntypedWorkflowStub(workflowId))
      .thenReturn(stubMock);

    // When
    workflowService.cancelWorkflow(workflowId);

    // Then
    Mockito.verify(stubMock).cancel();
  }

  @Test
  void givenNotExistentWfWhenCancelWorkflowThenDoNothing() {
    // Given
    String workflowId = "WFID";

    io.temporal.client.WorkflowNotFoundException workflowNotFoundException = new io.temporal.client.WorkflowNotFoundException(mock(WorkflowExecution.class), null, null);
    when(workflowClientMock.newUntypedWorkflowStub(workflowId))
      .thenThrow(workflowNotFoundException);

    // When
    Assertions.assertDoesNotThrow(() -> workflowService.cancelWorkflow(workflowId));
  }

}
