package it.gov.pagopa.pu.workflow.mapper;

import com.google.protobuf.Timestamp;
import io.temporal.api.common.v1.WorkflowType;
import io.temporal.api.enums.v1.WorkflowExecutionStatus;
import io.temporal.api.workflow.v1.WorkflowExecutionInfo;
import it.gov.pagopa.payhub.activities.util.Utilities;
import it.gov.pagopa.pu.workflow.dto.generated.WorkflowStatusDTO;
import it.gov.pagopa.pu.workflow.utils.TestUtils;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.time.OffsetDateTime;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.when;

class WorkflowStatusDTOMapperTest {

  private final WorkflowStatusDTOMapper mapper = new WorkflowStatusDTOMapper();

  @Test
  void test() {
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
      .status(WorkflowExecutionStatus.WORKFLOW_EXECUTION_STATUS_RUNNING)
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

    // When
    WorkflowStatusDTO result = mapper.map(workflowId, workflowExecutionInfoMock);

    // Then
    TestUtils.checkNotNullFields(result);
    assertEquals(expectedResult, result);
  }

  private static Timestamp offsetDateTime2ProtobufTimestamp(OffsetDateTime dt) {
    return Timestamp.newBuilder().setSeconds(dt.toEpochSecond()).setNanos(dt.getNano()).build();
  }
}
