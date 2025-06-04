package it.gov.pagopa.pu.workflow.service.temporal;

import io.temporal.client.schedules.ScheduleHandle;
import it.gov.pagopa.pu.workflow.dto.generated.ScheduleInfoDTO;
import it.gov.pagopa.pu.workflow.enums.ScheduleEnum;

/**
 * It allows to configure a recurrent Workflow.<BR />
 * For triggering a Workflow Execution at a specific one-time future point rather than on a recurring schedule, the Start Delay option should be used instead of a Schedule.
 * */
public interface WorkflowScheduleService {

  ScheduleHandle schedule(ScheduleEnum scheduleId, Class<?> workflowInterface, String taskQueue, String cronExpression);
  ScheduleHandle getSchedule(ScheduleEnum scheduleId);
  ScheduleInfoDTO getScheduleInfo(ScheduleEnum scheduleId);
}
