package it.gov.pagopa.pu.workflow.mapper;

import io.temporal.api.workflow.v1.WorkflowExecutionInfo;
import it.gov.pagopa.pu.workflow.dto.generated.WorkflowStatusDTO;
import it.gov.pagopa.pu.workflow.utilities.Utilities;
import org.springframework.stereotype.Service;

@Service
public class WorkflowStatusDTOMapper {

  public WorkflowStatusDTO map(String workflowId, WorkflowExecutionInfo info){
    return WorkflowStatusDTO.builder()
      .workflowId(workflowId)
      .workflowType(info.getType().getName())
      .runId(info.getExecution().getRunId())
      .taskQueue(info.getTaskQueue())
      .status(info.getStatus().name())
      .startDateTime(Utilities.protobufTimestamp2OffsetDateTime(info.getStartTime()))
      .executionDateTime(Utilities.protobufTimestamp2OffsetDateTime(info.getExecutionTime()))
      .endDateTime(Utilities.protobufTimestamp2OffsetDateTime(info.getCloseTime()))
      .duration(Utilities.protobufDuration2Duration(info.getExecutionDuration()).toString())
      .build();
  }
}
