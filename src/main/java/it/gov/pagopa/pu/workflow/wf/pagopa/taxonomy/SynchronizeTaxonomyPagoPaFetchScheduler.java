package it.gov.pagopa.pu.workflow.wf.pagopa.taxonomy;

import io.temporal.client.schedules.ScheduleHandle;
import it.gov.pagopa.pu.workflow.service.temporal.WorkflowScheduleService;
import it.gov.pagopa.pu.workflow.wf.pagopa.taxonomy.wftaxonomyfetch.SynchronizeTaxonomyPagoPaFetchWF;
import it.gov.pagopa.pu.workflow.wf.pagopa.taxonomy.wftaxonomyfetch.SynchronizeTaxonomyPagoPaFetchWFImpl;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@Getter
public class SynchronizeTaxonomyPagoPaFetchScheduler {
  public static final String SYNCHRONIZE_TAXONOMY_PAGOPA_FETCH_SCHEDULEID = "SynchronizeTaxonomyPagoPaFetchSchedule";

  private final ScheduleHandle schedule;

  public SynchronizeTaxonomyPagoPaFetchScheduler(
    WorkflowScheduleService workflowScheduleService,
    @Value("${schedule.synchronize-taxonomy-pagopa-fetch.cron-expression}") String cronExpression
  ) {
    this.schedule = workflowScheduleService.schedule(SYNCHRONIZE_TAXONOMY_PAGOPA_FETCH_SCHEDULEID, SynchronizeTaxonomyPagoPaFetchWF.class, SynchronizeTaxonomyPagoPaFetchWFImpl.TASK_QUEUE_SYNCHRONIZE_TAXONOMY_PAGOPA_FETCH, cronExpression);
  }

}
