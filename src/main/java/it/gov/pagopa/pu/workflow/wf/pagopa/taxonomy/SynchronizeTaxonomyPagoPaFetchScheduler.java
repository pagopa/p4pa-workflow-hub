package it.gov.pagopa.pu.workflow.wf.pagopa.taxonomy;

import io.temporal.client.schedules.ScheduleDescription;
import io.temporal.client.schedules.ScheduleException;
import io.temporal.client.schedules.ScheduleHandle;
import it.gov.pagopa.pu.workflow.service.WorkflowScheduleService;
import it.gov.pagopa.pu.workflow.wf.pagopa.taxonomy.wftaxonomyfetch.SynchronizeTaxonomyPagoPaFetchWF;
import it.gov.pagopa.pu.workflow.wf.pagopa.taxonomy.wftaxonomyfetch.SynchronizeTaxonomyPagoPaFetchWFImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class SynchronizeTaxonomyPagoPaFetchScheduler {
  public static final String SYNCHRONIZE_TAXONOMY_PAGOPA_FETCH_SCHEDULEID = "SynchronizeTaxonomyPagoPaFetchSchedule";

  public SynchronizeTaxonomyPagoPaFetchScheduler(
    WorkflowScheduleService workflowScheduleService,
    @Value("${schedule.synchronize-taxonomy-pagopa-fetch.cron-expression}") String cronExpression
  ) {
    init(workflowScheduleService, cronExpression);
  }

  private void init(WorkflowScheduleService workflowScheduleService, String cronExpression) {
    String taskQueue = SynchronizeTaxonomyPagoPaFetchWFImpl.TASK_QUEUE_SYNCHRONIZE_TAXONOMY_PAGOPA_FETCH;
    log.info("Scheduling {}", taskQueue);

    ScheduleHandle handle = workflowScheduleService.getSchedule(SYNCHRONIZE_TAXONOMY_PAGOPA_FETCH_SCHEDULEID);
    log.debug("ScheduleHandle {}", handle);

    try {
      ScheduleDescription describe = handle.describe();
      log.info("Found an existing schedule {}", describe);
    } catch (ScheduleException e) {
      log.info("Creating a new schedule");

      handle = workflowScheduleService.buildSchedule(
        SynchronizeTaxonomyPagoPaFetchWF.class,
        taskQueue,
        taskQueue,
        SYNCHRONIZE_TAXONOMY_PAGOPA_FETCH_SCHEDULEID,
        cronExpression);
      log.info("Created schedule {}", handle.describe());
    }
  }
}
