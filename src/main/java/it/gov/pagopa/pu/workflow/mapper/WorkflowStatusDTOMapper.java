package it.gov.pagopa.pu.workflow.mapper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.temporal.api.enums.v1.WorkflowExecutionStatus;
import io.temporal.api.workflow.v1.WorkflowExecutionInfo;
import io.temporal.client.WorkflowClient;
import io.temporal.client.WorkflowFailedException;
import io.temporal.failure.ActivityFailure;
import it.gov.pagopa.pu.workflow.dto.generated.WorkflowStatusDTO;
import it.gov.pagopa.pu.workflow.utilities.Utilities;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class WorkflowStatusDTOMapper {

  private final WorkflowClient workflowClient;
  private final ObjectMapper objectMapper;

  public WorkflowStatusDTOMapper(WorkflowClient workflowClient, ObjectMapper objectMapper) {
    this.workflowClient = workflowClient;
    this.objectMapper = objectMapper;
  }

  public WorkflowStatusDTO map(String workflowId, WorkflowExecutionInfo info){
    WorkflowStatusDTO out = WorkflowStatusDTO.builder()
      .workflowId(workflowId)
      .workflowType(info.getType().getName())
      .runId(info.getExecution().getRunId())
      .taskQueue(info.getTaskQueue())
      .status(info.getStatus())
      .startDateTime(Utilities.protobufTimestamp2OffsetDateTime(info.getStartTime()))
      .executionDateTime(Utilities.protobufTimestamp2OffsetDateTime(info.getExecutionTime()))
      .endDateTime(Utilities.protobufTimestamp2OffsetDateTime(info.getCloseTime()))
      .duration(Utilities.protobufDuration2Duration(info.getExecutionDuration()).toString())
      .build();

    if(WorkflowExecutionStatus.WORKFLOW_EXECUTION_STATUS_COMPLETED.equals(info.getStatus())){
      extractWfCompletedResult(workflowId, out);
    } else if(WorkflowExecutionStatus.WORKFLOW_EXECUTION_STATUS_FAILED.equals(info.getStatus())) {
      extractWfFailedResult(workflowId, out);
    }
    return out;
  }

  private void extractWfCompletedResult(String workflowId, WorkflowStatusDTO out) {
    try {
      Object result = workflowClient.newUntypedWorkflowStub(workflowId).getResult(Object.class);
      if(result!=null){
        out.setResult(objectMapper.writeValueAsString(result));
      }
    } catch (JsonProcessingException e) {
      log.error("Cannot serialize WF result of workflowId {}", workflowId, e);
    }
  }

  private void extractWfFailedResult(String workflowId, WorkflowStatusDTO out) {
    try{
      workflowClient.newUntypedWorkflowStub(workflowId).getResult(Object.class);
    } catch(WorkflowFailedException e){
      if(e.getCause() instanceof ActivityFailure activityFailure) {
        out.setResult("Failed Activity " + activityFailure.getActivityType() + ": " + Utilities.getWorkflowExceptionMessage(e.getCause()));
      } else {
        out.setResult(e.getMessage());
      }
    }
  }
}
