package it.gov.pagopa.pu.workflow.service;

import io.temporal.client.schedules.ScheduleHandle;

import java.time.Duration;

public interface ScheduleService {

  ScheduleHandle buildSchedule(Class<?> workflowInterface, String taskQueue, String workflowId, String scheduleId, Duration startEvery);
  ScheduleHandle getSchedule(String scheduleId);
}
