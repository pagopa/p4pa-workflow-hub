package it.gov.pagopa.pu.workflow.wf.paymentsreporting;

import io.temporal.client.WorkflowOptions;
import io.temporal.client.schedules.*;
import io.temporal.serviceclient.WorkflowServiceStubs;
import it.gov.pagopa.pu.workflow.wf.paymentsreporting.broker.wfretrieve.OrganizationsBrokeredRetrieveWF;
import it.gov.pagopa.pu.workflow.wf.paymentsreporting.broker.wfretrieve.OrganizationsBrokeredRetrieveWFImpl;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Collections;

import static it.gov.pagopa.pu.workflow.utilities.Utilities.generateWorkflowId;

@Slf4j
@Component
public class PaymentsReportingPagoPaScheduler {
  private final ScheduleClient scheduleClient;
  private final Duration scheduleDuration;
  private final String scheduleId;

  public PaymentsReportingPagoPaScheduler(
                                          @Value("${schedule.payments-reporting-pagopa.schedule-id:payments-reporting-pagopa-schedule}") String scheduleId,
                                          @Value("${schedule.payments-reporting-pagopa.schedule-duration:6}") Long scheduleDuration) {
    this.scheduleId = scheduleId;
    this.scheduleDuration = Duration.ofHours(scheduleDuration);
    WorkflowServiceStubs localServiceStubs = WorkflowServiceStubs.newLocalServiceStubs();
    this.scheduleClient = ScheduleClient.newInstance(localServiceStubs);
  }

  @PostConstruct
  public void init() {
    log.info("Scheduling OrganizationsBrokeredRetrieveWF");
    ScheduleHandle handle = scheduleClient.getHandle(scheduleId);
    try {
      ScheduleDescription describe = handle.describe();
      log.info("Found an existing schedule {}", describe);
    } catch (ScheduleException e) {
      log.info("Creating a new schedule");
      String workflowId = generateWorkflowId(0L, OrganizationsBrokeredRetrieveWFImpl.TASK_QUEUE_ORGANIZATIONS_BROKERED_RETRIEVE);
      Schedule schedule = buildSchedule(
        OrganizationsBrokeredRetrieveWF.class,
        OrganizationsBrokeredRetrieveWFImpl.TASK_QUEUE_ORGANIZATIONS_BROKERED_RETRIEVE,
        workflowId,
        scheduleDuration
      );
      log.debug("buildSchedule {}", schedule);

      handle = scheduleClient.createSchedule(
        scheduleId, schedule, ScheduleOptions.newBuilder().build());
      log.info("Created schedule {}", handle.describe());
    }
  }

  protected Schedule buildSchedule(Class<?> workflowInterface, String taskQueue, String workflowId, Duration startEvery) {
    WorkflowOptions workflowOptions = WorkflowOptions.newBuilder()
      .setWorkflowId(workflowId)
      .setTaskQueue(taskQueue)
      .build();

    ScheduleSpec scheduleSpec = ScheduleSpec.newBuilder()
      .setIntervals(
        Collections.singletonList(new ScheduleIntervalSpec(startEvery)))
      .build();

    return Schedule.newBuilder()
      .setAction(ScheduleActionStartWorkflow.newBuilder()
        .setWorkflowType(workflowInterface)
        .setOptions(workflowOptions)
        .build())
      .setSpec(scheduleSpec)
      .build();
  }
}
