package it.gov.pagopa.pu.workflow.mapper;

import io.temporal.client.schedules.ScheduleActionExecution;
import io.temporal.client.schedules.ScheduleActionExecutionStartWorkflow;
import io.temporal.client.schedules.ScheduleActionResult;
import io.temporal.client.schedules.ScheduleInfo;
import it.gov.pagopa.pu.workflow.dto.generated.RecentScheduleExecutionInfoDTO;
import it.gov.pagopa.pu.workflow.dto.generated.ScheduleExecutionInfoDTO;
import it.gov.pagopa.pu.workflow.dto.generated.ScheduleInfoDTO;
import it.gov.pagopa.pu.workflow.enums.ScheduleEnum;
import it.gov.pagopa.pu.workflow.utilities.Utilities;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
public class ScheduleInfoDTOMapper {

  public ScheduleInfoDTO map(ScheduleEnum scheduleId, ScheduleInfo scheduleInfo){
    return ScheduleInfoDTO.builder()
      .scheduleId(scheduleId)
      .numActions(scheduleInfo.getNumActions())
      .numActionsMissedCatchupWindow(scheduleInfo.getNumActionsMissedCatchupWindow())
      .numActionsSkippedOverlap(scheduleInfo.getNumActionsSkippedOverlap())
      .runningActions(scheduleInfo.getRunningActions().stream().map(this::map).filter(Objects::nonNull).toList())
      .recentActions(scheduleInfo.getRecentActions().stream().map(this::map).toList())
      .nextActionTimes(scheduleInfo.getNextActionTimes().stream().map(Utilities::instantToOffsetDateTime).toList())
      .createdAt(Utilities.instantToOffsetDateTime(scheduleInfo.getCreatedAt()))
      .lastUpdatedAt(Utilities.instantToOffsetDateTime(scheduleInfo.getLastUpdatedAt()))
      .build();
  }

  private ScheduleExecutionInfoDTO map(ScheduleActionExecution e) {
    if(e instanceof ScheduleActionExecutionStartWorkflow execution) {
      return ScheduleExecutionInfoDTO.builder()
        .workflowId(execution.getWorkflowId())
        .runId(execution.getFirstExecutionRunId())
        .build();
    } else {
      return null;
    }
  }

  private RecentScheduleExecutionInfoDTO map(ScheduleActionResult a) {
    RecentScheduleExecutionInfoDTO out = RecentScheduleExecutionInfoDTO.builder()
      .scheduledAt(Utilities.instantToOffsetDateTime(a.getScheduledAt()))
      .startedAt(Utilities.instantToOffsetDateTime(a.getStartedAt()))
      .build();
    if(a.getAction() instanceof ScheduleActionExecutionStartWorkflow execution){
      out.setWorkflowId(execution.getWorkflowId());
      out.setRunId(execution.getFirstExecutionRunId());
    }
    return out;
  }
}
