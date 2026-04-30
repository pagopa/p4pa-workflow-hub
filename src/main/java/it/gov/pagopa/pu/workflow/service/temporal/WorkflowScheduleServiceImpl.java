package it.gov.pagopa.pu.workflow.service.temporal;

import io.temporal.client.WorkflowOptions;
import io.temporal.client.schedules.*;
import it.gov.pagopa.payhub.activities.util.Utilities;
import it.gov.pagopa.pu.workflow.dto.generated.RecentScheduleExecutionInfoDTO;
import it.gov.pagopa.pu.workflow.dto.generated.ScheduleInfoDTO;
import it.gov.pagopa.pu.workflow.dto.generated.WorkflowStatusDTO;
import it.gov.pagopa.pu.workflow.enums.ScheduleEnum;
import it.gov.pagopa.pu.workflow.mapper.ScheduleInfoDTOMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.time.OffsetDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

@Service
@Slf4j
public class WorkflowScheduleServiceImpl implements WorkflowScheduleService {

  public static final String ON_DEMAND_SCHEDULE_SUFFIX = "ON-DEMAND";

  private final ScheduleClient scheduleClient;
  private final ScheduleInfoDTOMapper scheduleInfoDTOMapper;
  private final WorkflowService workflowService;

  public WorkflowScheduleServiceImpl(ScheduleClient scheduleClient, ScheduleInfoDTOMapper scheduleInfoDTOMapper, WorkflowService workflowService) {
    this.scheduleClient = scheduleClient;
    this.scheduleInfoDTOMapper = scheduleInfoDTOMapper;
    this.workflowService = workflowService;
  }

  @Override
  public ScheduleHandle schedule(ScheduleEnum scheduleId, Class<?> workflowInterface, String taskQueue, String cronExpression) {
    log.info("Scheduling {} on taskQueue {}", scheduleId, taskQueue);

    ScheduleHandle handle = getSchedule(scheduleId);
    log.debug("ScheduleHandle {}", handle);

    try {
      ScheduleDescription describe = handle.describe();
      log.info("Found an existing schedule {}", describe);
      checkExistingSchedule(
        workflowInterface,
        taskQueue,
        scheduleId,
        cronExpression,
        describe,
        handle
      );
    } catch (ScheduleException e) {
      log.info("Creating a new schedule");

      handle = scheduleInner(
        workflowInterface,
        taskQueue,
        scheduleId,
        cronExpression);
      log.info("Created schedule {}", handle.describe());
    }

    return handle;
  }

  private void checkExistingSchedule(Class<?> workflowInterface, String taskQueue, ScheduleEnum scheduleId, String cronExpression, ScheduleDescription describe, ScheduleHandle handle) {
    List<String> existingCronExpressions = describe.getSchedule().getSpec().getCronExpressions();
    if (CollectionUtils.isEmpty(existingCronExpressions) || !existingCronExpressions.getFirst().equals(cronExpression)) {
      log.info("Schedule {} already exists but with a different cron expression {}. Updating it with the new one {}.", scheduleId, existingCronExpressions, cronExpression);
      handle.delete();
      scheduleInner(workflowInterface, taskQueue, scheduleId, cronExpression);
      log.info("Existing schedule updated {}", describe);
    }
  }

  private ScheduleHandle scheduleInner(Class<?> workflowInterface, String taskQueue, ScheduleEnum scheduleId, String cronExpression) {
    WorkflowOptions workflowOptions = WorkflowOptions.newBuilder()
      .setWorkflowId(scheduleId.getValue())
      .setTaskQueue(taskQueue)
      .build();

    ScheduleSpec scheduleSpec = ScheduleSpec.newBuilder()
      .setCronExpressions(List.of(cronExpression))
      .setTimeZoneName(Utilities.ZONEID.getId())
      .build();

    Schedule schedule = Schedule.newBuilder()
      .setAction(ScheduleActionStartWorkflow.newBuilder()
        .setWorkflowType(workflowInterface)
        .setOptions(workflowOptions)
        .build())
      .setSpec(scheduleSpec)
      .build();

    return scheduleClient.createSchedule(
      scheduleId.getValue(), schedule, ScheduleOptions.newBuilder().build());
  }

  @Override
  public ScheduleHandle getSchedule(ScheduleEnum scheduleId) {
    return scheduleClient.getHandle(scheduleId.getValue());
  }


  @Override
  public ScheduleInfoDTO getScheduleInfo(ScheduleEnum scheduleId) {
    ScheduleDescription scheduleDescription = getSchedule(scheduleId).describe();
    ScheduleInfoDTO scheduleInfoDTO =
      scheduleInfoDTOMapper.map(scheduleId, scheduleDescription.getInfo());

    WorkflowStatusDTO workflowStatusDTO = null;
    if(scheduleDescription.getSchedule().getAction() instanceof ScheduleActionStartWorkflow action) {
      workflowStatusDTO = getOnDemandScheduleExecution(String.format("%s-%s", action.getWorkflowType(), ON_DEMAND_SCHEDULE_SUFFIX));
    }

    scheduleInfoDTO.setLastManualExecution(workflowStatusDTO);

    Optional<OffsetDateTime> maxRecentActionOpt = scheduleInfoDTO.getRecentActions().stream()
      .map(RecentScheduleExecutionInfoDTO::getStartedAt)
      .filter(Objects::nonNull)
      .max(Comparator.naturalOrder());
    OffsetDateTime maxRecentAction = maxRecentActionOpt.orElse(null);

    OffsetDateTime manualExecutionDateTime = workflowStatusDTO != null
      ? workflowStatusDTO.getExecutionDateTime()
      : null;

    OffsetDateTime lastExecution = Stream.of(maxRecentAction, manualExecutionDateTime)
      .filter(Objects::nonNull)
      .max(Comparator.naturalOrder())
      .orElse(null);

    scheduleInfoDTO.setLastExecution(lastExecution);
    return scheduleInfoDTO;
  }

  private WorkflowStatusDTO getOnDemandScheduleExecution(String workflowId) {
    try {
      return workflowService.getWorkflowStatus(workflowId);
    } catch (Exception ex) {
      return null;
    }
  }
}
