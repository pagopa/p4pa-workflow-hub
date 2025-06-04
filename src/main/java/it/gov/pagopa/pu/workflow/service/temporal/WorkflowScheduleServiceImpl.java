package it.gov.pagopa.pu.workflow.service.temporal;

import io.temporal.client.WorkflowOptions;
import io.temporal.client.schedules.*;
import it.gov.pagopa.payhub.activities.util.Utilities;
import it.gov.pagopa.pu.workflow.dto.generated.ScheduleInfoDTO;
import it.gov.pagopa.pu.workflow.enums.ScheduleEnum;
import it.gov.pagopa.pu.workflow.mapper.ScheduleInfoDTOMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class WorkflowScheduleServiceImpl implements WorkflowScheduleService {

  private final ScheduleClient scheduleClient;
  private final ScheduleInfoDTOMapper scheduleInfoDTOMapper;

  public WorkflowScheduleServiceImpl(ScheduleClient scheduleClient, ScheduleInfoDTOMapper scheduleInfoDTOMapper) {
    this.scheduleClient = scheduleClient;
    this.scheduleInfoDTOMapper = scheduleInfoDTOMapper;
  }

  @Override
  public ScheduleHandle schedule(ScheduleEnum scheduleId, Class<?> workflowInterface, String taskQueue, String cronExpression) {
    log.info("Scheduling {}", taskQueue);

    ScheduleHandle handle = getSchedule(scheduleId);
    log.debug("ScheduleHandle {}", handle);

    try {
      ScheduleDescription describe = handle.describe();
      log.info("Found an existing schedule {}", describe);
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
    return scheduleInfoDTOMapper.map(scheduleId, getSchedule(scheduleId).describe().getInfo());
  }
}
