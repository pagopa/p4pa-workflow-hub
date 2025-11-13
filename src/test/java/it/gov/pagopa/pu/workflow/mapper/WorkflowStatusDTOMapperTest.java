package it.gov.pagopa.pu.workflow.mapper;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.protobuf.Timestamp;
import io.temporal.api.common.v1.WorkflowExecution;
import io.temporal.api.common.v1.WorkflowType;
import io.temporal.api.enums.v1.EventType;
import io.temporal.api.enums.v1.WorkflowExecutionStatus;
import io.temporal.api.workflow.v1.WorkflowExecutionInfo;
import io.temporal.client.WorkflowClient;
import io.temporal.client.WorkflowFailedException;
import io.temporal.client.WorkflowStub;
import io.temporal.failure.ActivityFailure;
import io.temporal.failure.ApplicationFailure;
import it.gov.pagopa.payhub.activities.util.Utilities;
import it.gov.pagopa.pu.workflow.dto.generated.WorkflowStatusDTO;
import it.gov.pagopa.pu.workflow.utils.TestUtils;
import lombok.SneakyThrows;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WorkflowStatusDTOMapperTest {

  @Mock
  private WorkflowClient workflowClientMock;
  @Mock
  private ObjectMapper objectMapperMock;

  private WorkflowStatusDTOMapper mapper;

  @BeforeEach
  void init(){
    mapper = new WorkflowStatusDTOMapper(workflowClientMock, objectMapperMock);
  }

  @AfterEach
  void verifyNoMoreInteractions(){
    Mockito.verifyNoMoreInteractions(workflowClientMock, objectMapperMock);
  }
  @Test
  void givenProcessingWhenMapThenNoResult(){
    test(WorkflowExecutionStatus.WORKFLOW_EXECUTION_STATUS_RUNNING);
  }

  @Test
  void givenCompletedWhenMapThenFillResult(){
    test(WorkflowExecutionStatus.WORKFLOW_EXECUTION_STATUS_COMPLETED);
  }

  @Test
  void givenFailedWhenMapThenFillResult() {
    test(WorkflowExecutionStatus.WORKFLOW_EXECUTION_STATUS_FAILED);
  }

  @SneakyThrows
  void test(WorkflowExecutionStatus status) {
    String workflowId = "test-workflow-id";
    WorkflowStatusDTO expectedResult = WorkflowStatusDTO.builder()
      .workflowId(workflowId)
      .workflowType("WFTYPE")
      .runId("RUNID")
      .taskQueue("TASKQUEUE")
      .startDateTime(OffsetDateTime.now(Utilities.ZONEID))
      .executionDateTime(OffsetDateTime.now(Utilities.ZONEID).plusMinutes(1))
      .endDateTime(OffsetDateTime.now(Utilities.ZONEID).plusDays(1))
      .duration("PT0S")
      .status(status)
      .build();

    WorkflowExecutionInfo workflowExecutionInfoMock = Mockito.mock(WorkflowExecutionInfo.class, RETURNS_DEEP_STUBS);

    when(workflowExecutionInfoMock.getType()).thenReturn(WorkflowType.newBuilder().setName(expectedResult.getWorkflowType()).build());
    when(workflowExecutionInfoMock.getStatus()).thenReturn(expectedResult.getStatus());
    when(workflowExecutionInfoMock.getExecution().getRunId()).thenReturn(expectedResult.getRunId());
    when(workflowExecutionInfoMock.getTaskQueue()).thenReturn(expectedResult.getTaskQueue());
    when(workflowExecutionInfoMock.getStartTime()).thenReturn(offsetDateTime2ProtobufTimestamp(Objects.requireNonNull(expectedResult.getStartDateTime())));
    when(workflowExecutionInfoMock.getExecutionTime()).thenReturn(offsetDateTime2ProtobufTimestamp(Objects.requireNonNull(expectedResult.getExecutionDateTime())));
    when(workflowExecutionInfoMock.getCloseTime()).thenReturn(offsetDateTime2ProtobufTimestamp(Objects.requireNonNull(expectedResult.getEndDateTime())));
    when(workflowExecutionInfoMock.getExecutionDuration()).thenReturn(com.google.protobuf.Duration.getDefaultInstance());

    if(WorkflowExecutionStatus.WORKFLOW_EXECUTION_STATUS_COMPLETED.equals(status)){
      WorkflowStub mock = Mockito.mock(WorkflowStub.class);
      String result = "OK";
      Mockito.when(mock.getResult(Object.class)).thenReturn(result);

      Mockito.when(workflowClientMock.newUntypedWorkflowStub(workflowId))
          .thenReturn(mock);

      Mockito.when(objectMapperMock.writeValueAsString(result)).thenReturn(result);
      expectedResult.setResult(result);
    } else if (WorkflowExecutionStatus.WORKFLOW_EXECUTION_STATUS_FAILED.equals(status)) {
      WorkflowStub mock = Mockito.mock(WorkflowStub.class);
      String activityType = "ACTIVITYTYPE";
      String activityFailureDescription = "failureDescription";

      Mockito.when(mock.getResult(Object.class))
        .thenAnswer(i -> {
          throw new WorkflowFailedException(Mockito.mock(WorkflowExecution.class), "wf", EventType.EVENT_TYPE_CHILD_WORKFLOW_EXECUTION_FAILED, 1L, null,
            new ActivityFailure("ActivityFailed", 1L, 1L, activityType, "ACITIVITYID", null, null,
              ApplicationFailure.newFailure(activityFailureDescription, activityType)));
        });

      Mockito.when(workflowClientMock.newUntypedWorkflowStub(workflowId))
        .thenReturn(mock);

      expectedResult.setResult("Failed Activity " + activityType + ": " + activityFailureDescription);
    }

    // When
    WorkflowStatusDTO result = mapper.map(workflowId, workflowExecutionInfoMock);

    // Then
    TestUtils.checkNotNullFields(result, "result");
    assertEquals(expectedResult, result);
  }

  private static Timestamp offsetDateTime2ProtobufTimestamp(OffsetDateTime dt) {
    return Timestamp.newBuilder().setSeconds(dt.toEpochSecond()).setNanos(dt.getNano()).build();
  }
}
