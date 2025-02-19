package it.gov.pagopa.pu.workflow.wf.paymentsreporting;

import io.temporal.client.schedules.*;
import it.gov.pagopa.pu.workflow.service.ScheduleService;
import it.gov.pagopa.pu.workflow.wf.paymentsreporting.broker.wfretrieve.OrganizationsBrokeredRetrieveWF;
import it.gov.pagopa.pu.workflow.wf.paymentsreporting.broker.wfretrieve.OrganizationsBrokeredRetrieveWFImpl;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Duration;

import static it.gov.pagopa.pu.workflow.utilities.Utilities.generateWorkflowId;

@Slf4j
@Component
public class PaymentsReportingPagoPaScheduler {
  private final ScheduleService scheduleService;
  private final Long scheduleDuration;
  private final String scheduleId;

  public PaymentsReportingPagoPaScheduler(ScheduleService scheduleService,
      @Value("${schedule.payments-reporting-pagopa.schedule-id:payments-reporting-pagopa-schedule}") String scheduleId,
      @Value("${schedule.payments-reporting-pagopa.schedule-duration:6}") Long scheduleDuration) {
    this.scheduleService = scheduleService;
    this.scheduleId = scheduleId;
    this.scheduleDuration = scheduleDuration;
  }

  @PostConstruct
  public void init() {
    log.info("Scheduling OrganizationsBrokeredRetrieveWF");
    ScheduleHandle handle = scheduleService.getSchedule(scheduleId);
    log.debug("ScheduleHandle {}", handle);
    try {
      ScheduleDescription describe = handle.describe();
      log.info("Found an existing schedule {}", describe);
    } catch (ScheduleException e) {
      log.info("Creating a new schedule");
      String workflowId = generateWorkflowId(0L, OrganizationsBrokeredRetrieveWFImpl.TASK_QUEUE_ORGANIZATIONS_BROKERED_RETRIEVE);

      handle = scheduleService.buildSchedule(
        OrganizationsBrokeredRetrieveWF.class,
        OrganizationsBrokeredRetrieveWFImpl.TASK_QUEUE_ORGANIZATIONS_BROKERED_RETRIEVE,
        workflowId,
        scheduleId,
        Duration.ofHours(scheduleDuration));
      log.info("Created schedule {}", handle.describe());
    }
  }
}
