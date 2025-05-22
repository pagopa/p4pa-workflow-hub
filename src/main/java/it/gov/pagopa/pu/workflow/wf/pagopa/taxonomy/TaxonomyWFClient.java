package it.gov.pagopa.pu.workflow.wf.pagopa.taxonomy;

import it.gov.pagopa.pu.workflow.dto.generated.WorkflowCreatedDTO;
import it.gov.pagopa.pu.workflow.service.temporal.WorkflowClientService;
import it.gov.pagopa.pu.workflow.service.temporal.WorkflowService;
import it.gov.pagopa.pu.workflow.wf.pagopa.taxonomy.wftaxonomyfetch.SynchronizeTaxonomyPagoPaFetchWF;
import it.gov.pagopa.pu.workflow.wf.pagopa.taxonomy.wftaxonomyfetch.SynchronizeTaxonomyPagoPaFetchWFImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import static it.gov.pagopa.pu.workflow.utilities.Utilities.generateWorkflowId;

@Slf4j
@Service
public class TaxonomyWFClient {

  private final WorkflowService workflowService;
  private final WorkflowClientService workflowClientService;
  private static final String ON_DEMAND = "ON-DEMAND";

  public TaxonomyWFClient(WorkflowService workflowService, WorkflowClientService workflowClientService) {
    this.workflowService = workflowService;
    this.workflowClientService = workflowClientService;
  }

  public WorkflowCreatedDTO synchronizeTaxonomy() {
    log.info("Starting synchronizeTaxonomy {}", ON_DEMAND);
    String taskQueue = SynchronizeTaxonomyPagoPaFetchWFImpl.TASK_QUEUE_SYNCHRONIZE_TAXONOMY_PAGOPA_FETCH;
    String workflowId = generateWorkflowId(ON_DEMAND, SynchronizeTaxonomyPagoPaFetchWF.class);

    SynchronizeTaxonomyPagoPaFetchWF workflow = workflowService.buildWorkflowStub(
      SynchronizeTaxonomyPagoPaFetchWF.class,
      taskQueue,
      workflowId);

    return workflowClientService.start(workflow::synchronize);
  }
}
