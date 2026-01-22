package it.gov.pagopa.pu.workflow.service.temporal;

import it.gov.pagopa.pu.workflow.dto.generated.WorkflowStatusDTO;
import it.gov.pagopa.pu.workflow.exception.custom.TooManyAttemptsException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.stubbing.Answer;

import static io.temporal.api.enums.v1.WorkflowExecutionStatus.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
class WorkflowCompletionServiceTest {

  @Mock
  private WorkflowService workflowServiceMock;

  private WorkflowCompletionService service;

  private static final String WORKFLOW_ID = "workflow-123";

  @BeforeEach
  void setUp() {
    service = new WorkflowCompletionService(workflowServiceMock);
  }

  @Test
  void givenWaitTerminationStatusThenSuccess() {
    // Given
    WorkflowStatusDTO expectedResult = new WorkflowStatusDTO().status(WORKFLOW_EXECUTION_STATUS_COMPLETED);
    Mockito.when(workflowServiceMock.getWorkflowStatus(WORKFLOW_ID))
      .thenReturn(expectedResult);

    // When
    WorkflowStatusDTO result = service.waitTerminationStatus(WORKFLOW_ID, 3, 100);

    // Then
    assertSame(expectedResult, result);
  }

  @Test
  void givenWaitTerminationStatusWhenStatusFailedThenTerminate() {
    // Given
    WorkflowStatusDTO expectedResult = new WorkflowStatusDTO().status(WORKFLOW_EXECUTION_STATUS_FAILED);
    Mockito.when(workflowServiceMock.getWorkflowStatus(WORKFLOW_ID))
      .thenReturn(expectedResult);

    // When
    WorkflowStatusDTO result = service.waitTerminationStatus(WORKFLOW_ID, 3, 100);

    // Then
    assertSame(expectedResult, result);
  }

  @Test
  void givenWaitTerminationStatusWhenStatusNotTerminalThenRetryAndComplete() {
    // Given
    WorkflowStatusDTO expectedResult = new WorkflowStatusDTO().status(WORKFLOW_EXECUTION_STATUS_COMPLETED);
    Mockito.when(workflowServiceMock.getWorkflowStatus(WORKFLOW_ID))
      .thenAnswer(new Answer<WorkflowStatusDTO>() {
        private int count = 0;

        @Override
        public WorkflowStatusDTO answer(InvocationOnMock invocation) {
          count++;
          if (count < 3) {
            return new WorkflowStatusDTO().status(WORKFLOW_EXECUTION_STATUS_RUNNING);
          }
          return expectedResult;
        }
      });

    // When
    WorkflowStatusDTO result = service.waitTerminationStatus(WORKFLOW_ID, 3, 100);

    // Then
    assertSame(expectedResult, result);
    Mockito.verify(workflowServiceMock, times(3)).getWorkflowStatus(WORKFLOW_ID);
  }


  @Test
  void givenWaitTerminationStatusWhenThreadInterruptedThenRestoreThreadAndTerminate() {
    // Given
    WorkflowStatusDTO expectedResult = new WorkflowStatusDTO().status(WORKFLOW_EXECUTION_STATUS_COMPLETED);
    Mockito.when(workflowServiceMock.getWorkflowStatus(WORKFLOW_ID))
      .thenAnswer(invocation -> {
        Thread.currentThread().interrupt();
        return new WorkflowStatusDTO().status(WORKFLOW_EXECUTION_STATUS_RUNNING);
      })
      .thenReturn(expectedResult);

    // When
    WorkflowStatusDTO result = service.waitTerminationStatus(WORKFLOW_ID, 3, 100);

    // Then
    assertSame(expectedResult, result);
  }

  @Test
  void givenWaitTerminationStatusWhenUnknownStatusThenThrowsTooManyAttemptsException() {
    // Given
    Mockito.when(workflowServiceMock.getWorkflowStatus(WORKFLOW_ID))
      .thenReturn(new WorkflowStatusDTO().status(WORKFLOW_EXECUTION_STATUS_RUNNING));

    // When & Then
    TooManyAttemptsException exception = assertThrows(TooManyAttemptsException.class, () ->
      service.waitTerminationStatus(WORKFLOW_ID, 1, 100)
    );

    assertEquals("[TOO_MANY_ATTEMPTS] Maximum number of retries reached for workflow " + WORKFLOW_ID, exception.getMessage());
  }

  @Test
  void givenWaitTerminationStatusWhenStatusNullThenRetry() {
    // Given
    Mockito.when(workflowServiceMock.getWorkflowStatus(WORKFLOW_ID))
      .thenReturn(new WorkflowStatusDTO().status(null));

    // When & Then
    TooManyAttemptsException exception = assertThrows(TooManyAttemptsException.class, () ->
      service.waitTerminationStatus(WORKFLOW_ID, 1, 100)
    );

    assertEquals("[TOO_MANY_ATTEMPTS] Maximum number of retries reached for workflow " + WORKFLOW_ID, exception.getMessage());
  }
}
