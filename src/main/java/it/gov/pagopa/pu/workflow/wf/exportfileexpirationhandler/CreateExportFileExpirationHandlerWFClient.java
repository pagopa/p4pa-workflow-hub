package it.gov.pagopa.pu.workflow.wf.exportfileexpirationhandler;

import io.temporal.client.WorkflowClient;
import it.gov.pagopa.pu.workflow.service.WorkflowService;
import it.gov.pagopa.pu.workflow.wf.exportfileexpirationhandler.wfexportfileexpirationhandler.CreateExportFileExpirationHandlerHandlerWFImpl;
import it.gov.pagopa.pu.workflow.wf.exportfileexpirationhandler.wfexportfileexpirationhandler.CreateExportFileExpirationHandlerWF;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import static it.gov.pagopa.pu.workflow.utilities.Utilities.generateWorkflowId;

@Service
@Slf4j
public class CreateExportFileExpirationHandlerWFClient {

  private final WorkflowService workflowService;

  public CreateExportFileExpirationHandlerWFClient(WorkflowService workflowService) {
    this.workflowService = workflowService;
  }

  public String createExportFileExpirationHandler(Long exportFileId) {
    log.info("Starting createExportFileExpirationHandler for file with exportFileId: {}", exportFileId);

    String taskQueue = CreateExportFileExpirationHandlerHandlerWFImpl.TASK_QUEUE_CREATE_EXPORT_FILE_EXPIRATION_HANDLER_WF;
    String workflowId = generateWorkflowId(exportFileId, taskQueue);

    CreateExportFileExpirationHandlerWF workflow = workflowService.buildWorkflowStub(
      CreateExportFileExpirationHandlerWF.class,
      taskQueue,
      workflowId);
    WorkflowClient.start(workflow::createExportFileExpirationHandler, exportFileId);
    return workflowId;
  }
}
