package it.gov.pagopa.pu.workflow.wf.ingestionflow.orgsilservice;

import it.gov.pagopa.pu.workflow.dto.generated.WorkflowCreatedDTO;
import it.gov.pagopa.pu.workflow.service.temporal.WorkflowClientService;
import it.gov.pagopa.pu.workflow.service.temporal.WorkflowService;
import it.gov.pagopa.pu.workflow.utilities.TaskQueueConstants;
import it.gov.pagopa.pu.workflow.wf.ingestionflow.orgsilservice.wfingestion.OrgSilServiceIngestionWF;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import static it.gov.pagopa.pu.workflow.utilities.Utilities.generateWorkflowId;

@Slf4j
@Service
public class OrgSilServiceIngestionWFClient {

  private final WorkflowService workflowService;
  private final WorkflowClientService workflowClientService;


  public OrgSilServiceIngestionWFClient(
    WorkflowService workflowService,
    WorkflowClientService workflowClientService
  ) {
    this.workflowService = workflowService;
    this.workflowClientService = workflowClientService;
  }

  public WorkflowCreatedDTO ingest(Long ingestionFlowFileId) {
    log.info("Starting org sil service ingestion flow file having id {}", ingestionFlowFileId);
    String taskQueue = TaskQueueConstants.TASK_QUEUE_IMPORT_MEDIUM_PRIORITY;
    String workflowId = generateWorkflowId(ingestionFlowFileId, OrgSilServiceIngestionWF.class);

    OrgSilServiceIngestionWF workflow = workflowService.buildWorkflowStub(
      OrgSilServiceIngestionWF.class,
      taskQueue,
      workflowId);
    return workflowClientService.start(workflow::ingest, ingestionFlowFileId);
  }

}
