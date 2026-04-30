package it.gov.pagopa.pu.workflow.wf.pagopa.taxonomy;

import it.gov.pagopa.pu.workflow.dto.generated.WorkflowCreatedDTO;
import it.gov.pagopa.pu.workflow.service.temporal.WorkflowClientService;
import it.gov.pagopa.pu.workflow.service.temporal.WorkflowScheduleServiceImpl;
import it.gov.pagopa.pu.workflow.service.temporal.WorkflowService;
import it.gov.pagopa.pu.workflow.utilities.TaskQueueConstants;
import it.gov.pagopa.pu.workflow.wf.pagopa.taxonomy.wftaxonomyfetch.SynchronizeTaxonomyPagoPaFetchWF;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import static it.gov.pagopa.pu.workflow.utilities.Utilities.generateWorkflowId;

@Slf4j
@Service
public class TaxonomyWFClient {

  private final WorkflowService workflowService;
  private final WorkflowClientService workflowClientService;

  public TaxonomyWFClient(WorkflowService workflowService, WorkflowClientService workflowClientService) {
    this.workflowService = workflowService;
    this.workflowClientService = workflowClientService;
  }

  public WorkflowCreatedDTO synchronizeTaxonomy() {
    log.info("Starting on-demand execution of synchronizeTaxonomy");
    String taskQueue = TaskQueueConstants.TASK_QUEUE_LOW_PRIORITY;
    String workflowId = generateWorkflowId(WorkflowScheduleServiceImpl.ON_DEMAND_SCHEDULE_SUFFIX, SynchronizeTaxonomyPagoPaFetchWF.class);

    SynchronizeTaxonomyPagoPaFetchWF workflow = workflowService.buildWorkflowStubToStartNew(
      SynchronizeTaxonomyPagoPaFetchWF.class,
      taskQueue,
      workflowId);

    return workflowClientService.start(workflow::synchronize);
  }
}
