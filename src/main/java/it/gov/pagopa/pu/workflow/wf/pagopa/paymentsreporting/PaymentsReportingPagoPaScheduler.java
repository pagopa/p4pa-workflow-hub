package it.gov.pagopa.pu.workflow.wf.pagopa.paymentsreporting;

import io.temporal.client.schedules.*;
import it.gov.pagopa.pu.workflow.service.WorkflowScheduleService;
import it.gov.pagopa.pu.workflow.wf.pagopa.paymentsreporting.wfbrokersfetch.OrganizationsBrokeredRetrieveWF;
import it.gov.pagopa.pu.workflow.wf.pagopa.paymentsreporting.wfbrokersfetch.OrganizationsBrokeredRetrieveWFImpl;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Slf4j
@Component
public class PaymentsReportingPagoPaScheduler {
  private final WorkflowScheduleService workflowScheduleService;
  private final Long scheduleDuration;
  private final String scheduleId;

  public PaymentsReportingPagoPaScheduler(WorkflowScheduleService workflowScheduleService,
                                          @Value("${schedule.payments-reporting-pagopa.name}") String scheduleId,
                                          @Value("${schedule.payments-reporting-pagopa.frequency-hour}") Long frequencyHour) {
    this.workflowScheduleService = workflowScheduleService;
    this.scheduleId = scheduleId;
    this.scheduleDuration = frequencyHour;
  }

  @PostConstruct
  public void init() {
    log.info("Scheduling OrganizationsBrokeredRetrieveWF");
    ScheduleHandle handle = workflowScheduleService.getSchedule(scheduleId);
    log.debug("ScheduleHandle {}", handle);
    try {
      ScheduleDescription describe = handle.describe();
      log.info("Found an existing schedule {}", describe);
    } catch (ScheduleException e) {
      log.info("Creating a new schedule");

      handle = workflowScheduleService.buildSchedule(
        OrganizationsBrokeredRetrieveWF.class,
        OrganizationsBrokeredRetrieveWFImpl.TASK_QUEUE_ORGANIZATIONS_BROKERED_RETRIEVE,
        OrganizationsBrokeredRetrieveWFImpl.TASK_QUEUE_ORGANIZATIONS_BROKERED_RETRIEVE,
        scheduleId,
        Duration.ofHours(scheduleDuration));
      log.info("Created schedule {}", handle.describe());
    }
  }
}
