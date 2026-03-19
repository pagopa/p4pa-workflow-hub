package it.gov.pagopa.pu.workflow.wf.ingestionflow.notice;

import it.gov.pagopa.pu.workflow.service.temporal.WorkflowClientService;
import it.gov.pagopa.pu.workflow.service.temporal.WorkflowService;
import it.gov.pagopa.pu.workflow.utilities.TaskQueueConstants;
import it.gov.pagopa.pu.workflow.wf.ingestionflow.notice.deletemassivenoticesfile.DeleteMassiveNoticesFileWF;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import static it.gov.pagopa.pu.workflow.utilities.Utilities.generateWorkflowId;

@Service
@Slf4j
public class DeleteMassiveNoticesFileWFClient {
  private final WorkflowService workflowService;
  private final WorkflowClientService workflowClientService;

  public DeleteMassiveNoticesFileWFClient(WorkflowService workflowService, WorkflowClientService workflowClientService) {
    this.workflowService = workflowService;
    this.workflowClientService = workflowClientService;
  }

  public void delete(Long ingestionFlowFileId) {
    log.info("Starting on-demand delete massive notices file workflow for ingestionFlowFileId {}", ingestionFlowFileId);

    String taskQueue = TaskQueueConstants.TASK_QUEUE_IMPORT_MEDIUM_PRIORITY;
    String workflowId = generateWorkflowId(ingestionFlowFileId, DeleteMassiveNoticesFileWF.class);

    DeleteMassiveNoticesFileWF workflow = workflowService.buildWorkflowStubToStartNew(
      DeleteMassiveNoticesFileWF.class,
      taskQueue,
      workflowId
    );

    workflowClientService.start(workflow::deleteMassiveNoticesFile, ingestionFlowFileId);
  }
}
