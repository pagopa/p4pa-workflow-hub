package it.gov.pagopa.pu.workflow.service;

import io.temporal.api.common.v1.WorkflowExecution;
import io.temporal.api.enums.v1.WorkflowExecutionStatus;
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
import it.gov.pagopa.pu.workflow.wf.ingestionflow.paymentsreporting.wfingestion.PaymentsReportingIngestionWF;
import it.gov.pagopa.pu.workflow.wf.ingestionflow.paymentsreporting.wfingestion.PaymentsReportingIngestionWFImpl;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WorkflowServiceTest {

  @Mock
  private WorkflowClient workflowClientMock;
  @Mock
  private PaymentsReportingIngestionWF wfMock;
  @Mock
  private WorkflowExecutionInfo workflowExecutionInfoMock;
  @Mock
  private WorkflowServiceStubs workflowServiceStubsMock;

  private WorkflowService workflowService;

  @BeforeEach
  void init(){
    workflowService = new WorkflowServiceImpl(workflowClientMock);
  }

  @AfterEach
  void verifyNoMoreInteractions(){
    Mockito.verifyNoMoreInteractions(workflowClientMock, wfMock);
  }

  @Test
  void whenIngestThenOk(){
    // Given
    long ingestionFlowFileId = 1L;
    String workflowId = String.valueOf(ingestionFlowFileId);

    when(workflowClientMock.newWorkflowStub(
        Mockito.eq(PaymentsReportingIngestionWF.class),
        Mockito.<WorkflowOptions>argThat(options ->
          PaymentsReportingIngestionWFImpl.TASK_QUEUE_PAYMENTS_REPORTING_INGESTION_WF.equals(options.getTaskQueue()) &&
            workflowId.equals(options.getWorkflowId())
        )))
      .thenReturn(wfMock);

    // When
    PaymentsReportingIngestionWF result = workflowService.buildWorkflowStub(PaymentsReportingIngestionWF.class, PaymentsReportingIngestionWFImpl.TASK_QUEUE_PAYMENTS_REPORTING_INGESTION_WF, workflowId);

    // Then
    Assertions.assertSame(wfMock, result);
  }

  @Test
  void givenGetWorkflowStatusThenSuccess() {
    // Given
    String workflowId = "test-workflow-id";
    String expectedStatus = WorkflowExecutionStatus.WORKFLOW_EXECUTION_STATUS_RUNNING.name();

    when(workflowExecutionInfoMock.getStatus()).thenReturn(WorkflowExecutionStatus.WORKFLOW_EXECUTION_STATUS_RUNNING);

    when(workflowClientMock.getWorkflowServiceStubs()).thenReturn(workflowServiceStubsMock);

    try (MockedStatic<WorkflowClientHelper> mockedStatic = mockStatic(WorkflowClientHelper.class)) {
      mockedStatic.when(() -> WorkflowClientHelper.describeWorkflowInstance(
          any(),
          eq("default"),
          any(),
          any()))
        .thenReturn(workflowExecutionInfoMock);

      // When
      WorkflowStatusDTO result = workflowService.getWorkflowStatus(workflowId);

      // Then
      assertEquals(workflowId, result.getWorkflowId());
      assertEquals(expectedStatus, result.getStatus());
    }
  }


  @Test
  void givenGetWorkflowStatusWhenWorkflowNotFoundThenThrowWorkflowNotFoundException() {
    String workflowId = "test-workflow-id";

    when(workflowExecutionInfoMock.getStatus()).thenThrow(new io.temporal.client.WorkflowNotFoundException(WorkflowExecution.newBuilder().setWorkflowId(workflowId).build(), "Workflow not found", null));

    when(workflowClientMock.getWorkflowServiceStubs()).thenReturn(workflowServiceStubsMock);

    try (MockedStatic<WorkflowClientHelper> mockedStatic = mockStatic(WorkflowClientHelper.class)) {
      mockedStatic.when(() -> WorkflowClientHelper.describeWorkflowInstance(
          any(),
          eq("default"),
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
  void givenGetWorkflowStatusWhenInternalErrorThenThrowWorkflowInternalErrorException(){
    String workflowId = "test-workflow-id";

    when(workflowClientMock.getWorkflowServiceStubs()).thenThrow(new WorkflowServiceException(WorkflowExecution.newBuilder().setWorkflowId(workflowId).build(), "Generic Error", null));

    try (MockedStatic<WorkflowClientHelper> mockedStatic = mockStatic(WorkflowClientHelper.class)) {
      mockedStatic.when(() -> WorkflowClientHelper.describeWorkflowInstance(
          any(),
          eq("default"),
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

    when(workflowClientMock.newUntypedWorkflowStub(
      eq(workflowId),
      argThat(options -> taskQueue.equals(options.getTaskQueue()) && workflowId.equals(options.getWorkflowId()))
    )).thenReturn(expectedStub);

    // When
    WorkflowStub result = workflowService.buildUntypedWorkflowStub(taskQueue, workflowId);

    // Then
    assertEquals(expectedStub, result);
  }


}
