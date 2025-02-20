package it.gov.pagopa.pu.workflow.wf.pagopa.paymentsreporting;

import io.temporal.client.schedules.*;
import it.gov.pagopa.pu.workflow.service.WorkflowScheduleService;
import it.gov.pagopa.pu.workflow.wf.pagopa.paymentsreporting.wfbrokersfetch.BrokersPaymentsReportingPagoPaFetchWF;
import it.gov.pagopa.pu.workflow.wf.pagopa.paymentsreporting.wfbrokersfetch.BrokersPaymentsReportingPagoPaFetchWFImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Slf4j
@Component
public class PaymentsReportingPagoPaScheduler {

  public PaymentsReportingPagoPaScheduler(WorkflowScheduleService workflowScheduleService,
                                          @Value("${schedule.payments-reporting-pagopa.frequency-hour}") Long frequencyHour) {
    init(workflowScheduleService, Duration.ofHours(frequencyHour), "payments-reporting-pagopa");
  }

  private void init(WorkflowScheduleService workflowScheduleService, Duration scheduleDuration, String scheduleId) {
    log.info("Scheduling BrokersPaymentsReportingPagoPaFetchWF");
    ScheduleHandle handle = workflowScheduleService.getSchedule(scheduleId);
    log.debug("ScheduleHandle {}", handle);
    try {
      ScheduleDescription describe = handle.describe();
      log.info("Found an existing schedule {}", describe);
    } catch (ScheduleException e) {
      log.info("Creating a new schedule");

      handle = workflowScheduleService.buildSchedule(
        BrokersPaymentsReportingPagoPaFetchWF.class,
        BrokersPaymentsReportingPagoPaFetchWFImpl.TASK_QUEUE_BROKERS_PAYMENTS_REPORTING_PAGOPA_FETCH,
        BrokersPaymentsReportingPagoPaFetchWFImpl.TASK_QUEUE_BROKERS_PAYMENTS_REPORTING_PAGOPA_FETCH,
        scheduleId,
        scheduleDuration);
      log.info("Created schedule {}", handle.describe());
    }
  }
}
