package it.gov.pagopa.pu.workflow.service;

import io.temporal.client.schedules.ScheduleHandle;

/**
 * It allows to configure a recurrent Workflow.<BR />
 * For triggering a Workflow Execution at a specific one-time future point rather than on a recurring schedule, the Start Delay option should be used instead of a Schedule.
 * */
public interface WorkflowScheduleService {

  ScheduleHandle buildSchedule(Class<?> workflowInterface, String taskQueue, String workflowId, String scheduleId, String cronExpression);
  ScheduleHandle getSchedule(String scheduleId);
}
