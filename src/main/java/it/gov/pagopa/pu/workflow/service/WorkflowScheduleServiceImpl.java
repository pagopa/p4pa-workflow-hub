package it.gov.pagopa.pu.workflow.service;

import io.temporal.client.WorkflowOptions;
import io.temporal.client.schedules.*;
import it.gov.pagopa.payhub.activities.util.Utilities;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class WorkflowScheduleServiceImpl implements WorkflowScheduleService {

  private final ScheduleClient scheduleClient;

  public WorkflowScheduleServiceImpl(ScheduleClient scheduleClient) {
    this.scheduleClient = scheduleClient;
  }

  @Override
  public ScheduleHandle buildSchedule(Class<?> workflowInterface, String taskQueue, String workflowId, String scheduleId, String cronExpression) {
    WorkflowOptions workflowOptions = WorkflowOptions.newBuilder()
      .setWorkflowId(workflowId)
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
      scheduleId, schedule, ScheduleOptions.newBuilder().build());
  }

  @Override
  public ScheduleHandle getSchedule(String scheduleId) {
    return scheduleClient.getHandle(scheduleId);
  }
}
