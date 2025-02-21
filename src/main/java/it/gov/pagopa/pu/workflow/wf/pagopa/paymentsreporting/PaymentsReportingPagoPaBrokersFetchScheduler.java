package it.gov.pagopa.pu.workflow.wf.pagopa.paymentsreporting;

import io.temporal.client.schedules.*;
import it.gov.pagopa.pu.workflow.service.WorkflowScheduleService;
import it.gov.pagopa.pu.workflow.wf.pagopa.paymentsreporting.wfbrokersfetch.PaymentsReportingPagoPaBrokersFetchWF;
import it.gov.pagopa.pu.workflow.wf.pagopa.paymentsreporting.wfbrokersfetch.PaymentsReportingPagoPaBrokersFetchWFImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class PaymentsReportingPagoPaBrokersFetchScheduler {
  public static final String PAYMENTS_REPORTING_PAGOPA_FETCH_SCHEDULEID = "PaymentsReportingPagoPaBrokersFetchSchedule";

  public PaymentsReportingPagoPaBrokersFetchScheduler(
    WorkflowScheduleService workflowScheduleService,
    @Value("${schedule.payments-reporting-pagopa-brokers-fetch.cron-expression}") String cronExpression
  ) {
    init(workflowScheduleService, cronExpression);
  }

  private void init(WorkflowScheduleService workflowScheduleService, String cronExpression) {
    String taskQueue = PaymentsReportingPagoPaBrokersFetchWFImpl.TASK_QUEUE_BROKERS_PAYMENTS_REPORTING_PAGOPA_FETCH;
    log.info("Scheduling {}", taskQueue);

    ScheduleHandle handle = workflowScheduleService.getSchedule(PAYMENTS_REPORTING_PAGOPA_FETCH_SCHEDULEID);
    log.debug("ScheduleHandle {}", handle);

    try {
      ScheduleDescription describe = handle.describe();
      log.info("Found an existing schedule {}", describe);
    } catch (ScheduleException e) {
      log.info("Creating a new schedule");

      handle = workflowScheduleService.buildSchedule(
        PaymentsReportingPagoPaBrokersFetchWF.class,
        taskQueue,
        taskQueue,
        PAYMENTS_REPORTING_PAGOPA_FETCH_SCHEDULEID,
        cronExpression);
      log.info("Created schedule {}", handle.describe());
    }
  }
}
