package it.gov.pagopa.pu.workflow.wf.exportfile.expiration;

import io.temporal.client.WorkflowClient;
import it.gov.pagopa.pu.workflow.service.WorkflowService;
import it.gov.pagopa.pu.workflow.wf.exportfile.expiration.wfexpiration.ExportFileExpirationHandlerWFImpl;
import it.gov.pagopa.pu.workflow.wf.exportfile.expiration.wfexpiration.ExportFileExpirationHandlerWF;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import static it.gov.pagopa.pu.workflow.utilities.Utilities.generateWorkflowId;

@Service
@Slf4j
public class ExportFileExpirationHandlerWFClient {

  private final WorkflowService workflowService;

  public ExportFileExpirationHandlerWFClient(WorkflowService workflowService) {
    this.workflowService = workflowService;
  }

  public String exportFileExpirationHandler(Long exportFileId) {
    log.info("Starting exportFileExpirationHandler for file with exportFileId: {}", exportFileId);

    String taskQueue = ExportFileExpirationHandlerWFImpl.TASK_QUEUE_EXPORT_FILE_EXPIRATION_HANDLER_WF;
    String workflowId = generateWorkflowId(exportFileId, taskQueue);

    ExportFileExpirationHandlerWF workflow = workflowService.buildWorkflowStub(
      ExportFileExpirationHandlerWF.class,
      taskQueue,
      workflowId);
    WorkflowClient.start(workflow::exportFileExpirationHandler, exportFileId);
    return workflowId;
  }
}
