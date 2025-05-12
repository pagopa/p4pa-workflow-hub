package it.gov.pagopa.pu.workflow.wf.pagopa.paymentsreporting;

import io.temporal.client.schedules.ScheduleHandle;
import it.gov.pagopa.pu.workflow.service.temporal.WorkflowScheduleService;
import it.gov.pagopa.pu.workflow.wf.pagopa.paymentsreporting.wfbrokersfetch.PaymentsReportingPagoPaBrokersFetchWF;
import it.gov.pagopa.pu.workflow.wf.pagopa.paymentsreporting.wfbrokersfetch.PaymentsReportingPagoPaBrokersFetchWFImpl;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@Getter
public class PaymentsReportingPagoPaBrokersFetchScheduler {
  public static final String PAYMENTS_REPORTING_PAGOPA_FETCH_SCHEDULEID = "PaymentsReportingPagoPaBrokersFetchSchedule";

  private final ScheduleHandle schedule;

  PaymentsReportingPagoPaBrokersFetchScheduler(
    WorkflowScheduleService workflowScheduleService,
    @Value("${schedule.payments-reporting-pagopa-brokers-fetch.cron-expression}") String cronExpression
  ) {
    schedule = workflowScheduleService.schedule(
      PAYMENTS_REPORTING_PAGOPA_FETCH_SCHEDULEID,
      PaymentsReportingPagoPaBrokersFetchWF.class,
      PaymentsReportingPagoPaBrokersFetchWFImpl.TASK_QUEUE_BROKERS_PAYMENTS_REPORTING_PAGOPA_FETCH,
      cronExpression);
  }

}
