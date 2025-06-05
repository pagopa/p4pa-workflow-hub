package it.gov.pagopa.pu.workflow.service;

import io.temporal.api.enums.v1.WorkflowExecutionStatus;
import it.gov.pagopa.pu.workflow.dto.generated.WorkflowStatusDTO;
import it.gov.pagopa.pu.workflow.exception.custom.TooManyAttemptsException;
import it.gov.pagopa.pu.workflow.service.temporal.WorkflowService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.stubbing.Answer;

import static io.temporal.api.enums.v1.WorkflowExecutionStatus.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
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
        Mockito.when(workflowServiceMock.getWorkflowStatus(WORKFLOW_ID))
                .thenReturn(new WorkflowStatusDTO().status(WORKFLOW_EXECUTION_STATUS_COMPLETED));

        // When
        WorkflowExecutionStatus result = service.waitTerminationStatus(WORKFLOW_ID, 3, 100);

        // Then
        assertEquals(WORKFLOW_EXECUTION_STATUS_COMPLETED, result);
    }

    @Test
    void givenWaitTerminationStatusWhenStatusFailedThenTerminate() {
        // Given
        Mockito.when(workflowServiceMock.getWorkflowStatus(WORKFLOW_ID))
                .thenReturn(new WorkflowStatusDTO().status(WORKFLOW_EXECUTION_STATUS_FAILED));

        // When
        WorkflowExecutionStatus result = service.waitTerminationStatus(WORKFLOW_ID, 3, 100);

        // Then
        assertEquals(WORKFLOW_EXECUTION_STATUS_FAILED, result);
    }

    @Test
    void givenWaitTerminationStatusWhenStatusNotTerminalThenRetryAndComplete() {
        // Given
        Mockito.when(workflowServiceMock.getWorkflowStatus(WORKFLOW_ID))
                .thenAnswer(new Answer<WorkflowStatusDTO>() {
                    private int count = 0;
                    @Override
                    public WorkflowStatusDTO answer(InvocationOnMock invocation) {
                        count++;
                        if (count < 3) {
                            return new WorkflowStatusDTO().status(WORKFLOW_EXECUTION_STATUS_RUNNING);
                        }
                        return new WorkflowStatusDTO().status(WORKFLOW_EXECUTION_STATUS_COMPLETED);
                    }
                });

        // When
        WorkflowExecutionStatus result = service.waitTerminationStatus(WORKFLOW_ID, 3, 100);

        // Then
        assertEquals(WORKFLOW_EXECUTION_STATUS_COMPLETED, result);
        Mockito.verify(workflowServiceMock, times(3)).getWorkflowStatus(WORKFLOW_ID);
    }


    @Test
    void givenWaitTerminationStatusWhenThreadInterruptedThenRestoreThreadAndTerminate() {
        // Given
        Mockito.when(workflowServiceMock.getWorkflowStatus(WORKFLOW_ID))
                .thenAnswer(invocation -> {
                    Thread.currentThread().interrupt();
                    return new WorkflowStatusDTO().status(WORKFLOW_EXECUTION_STATUS_RUNNING);
                })
                .thenReturn(new WorkflowStatusDTO().status(WORKFLOW_EXECUTION_STATUS_COMPLETED));

        // When
        WorkflowExecutionStatus result = service.waitTerminationStatus(WORKFLOW_ID, 3, 100);

        // Then
        assertEquals(WORKFLOW_EXECUTION_STATUS_COMPLETED, result);
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

        assertEquals("Maximum number of retries reached for workflow " + WORKFLOW_ID, exception.getMessage());
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

        assertEquals("Maximum number of retries reached for workflow " + WORKFLOW_ID, exception.getMessage());
    }
}
