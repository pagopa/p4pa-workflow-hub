package it.gov.pagopa.pu.workflow.wf.ingestionflow.assessmentsregistry;

import it.gov.pagopa.pu.workflow.dto.generated.WorkflowCreatedDTO;
import it.gov.pagopa.pu.workflow.service.temporal.WorkflowClientService;
import it.gov.pagopa.pu.workflow.service.temporal.WorkflowService;
import it.gov.pagopa.pu.workflow.utilities.TaskQueueConstants;
import it.gov.pagopa.pu.workflow.wf.ingestionflow.assessmentsregistry.wfingestion.AssessmentsRegistryIngestionWF;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import static it.gov.pagopa.pu.workflow.utilities.Utilities.generateWorkflowId;

@Slf4j
@Service
@RequiredArgsConstructor
public class AssessmentsRegistryIngestionWFClient {

  private final WorkflowService workflowService;
  private final WorkflowClientService workflowClientService;

  public WorkflowCreatedDTO ingest(Long ingestionFlowFileId) {
    log.info("Starting debt assessments registry ingestion flow file having id {}", ingestionFlowFileId);
    String taskQueue = TaskQueueConstants.TASK_QUEUE_IMPORT_MEDIUM_PRIORITY;
    String workflowId = generateWorkflowId(ingestionFlowFileId, AssessmentsRegistryIngestionWF.class);

    AssessmentsRegistryIngestionWF workflow = workflowService.buildWorkflowStub(
      AssessmentsRegistryIngestionWF.class,
      taskQueue,
      workflowId);
    return workflowClientService.start(workflow::ingest, ingestionFlowFileId);
  }
}
