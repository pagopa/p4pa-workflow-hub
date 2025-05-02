package it.gov.pagopa.pu.workflow.wf.exportfile.expiration;

import it.gov.pagopa.pu.workflow.dto.generated.WorkflowCreatedDTO;
import it.gov.pagopa.pu.workflow.service.WorkflowClientService;
import it.gov.pagopa.pu.workflow.service.WorkflowService;
import it.gov.pagopa.pu.workflow.wf.exportfile.expiration.wfexpiration.ExportFileExpirationHandlerWF;
import it.gov.pagopa.pu.workflow.wf.exportfile.expiration.wfexpiration.ExportFileExpirationHandlerWFImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

import static it.gov.pagopa.pu.workflow.utilities.Utilities.generateWorkflowId;

@Service
@Slf4j
public class ExportFileExpirationHandlerWFClient {

  private final WorkflowService workflowService;
  private final WorkflowClientService workflowClientService;

  public ExportFileExpirationHandlerWFClient(WorkflowService workflowService, WorkflowClientService workflowClientService) {
    this.workflowService = workflowService;
    this.workflowClientService = workflowClientService;
  }

  public WorkflowCreatedDTO exportFileExpirationHandler(Long exportFileId) {
    log.info("Starting exportFileExpirationHandler for file with exportFileId: {}", exportFileId);

    String taskQueue = ExportFileExpirationHandlerWFImpl.TASK_QUEUE_EXPORT_FILE_EXPIRATION_HANDLER_WF;
    String workflowId = generateWorkflowId(exportFileId, ExportFileExpirationHandlerWF.class);

    ExportFileExpirationHandlerWF workflow = workflowService.buildWorkflowStub(
      ExportFileExpirationHandlerWF.class,
      taskQueue,
      workflowId);
    return workflowClientService.start(workflow::exportFileExpirationHandler, exportFileId);
  }

  public void scheduleExportFileExpiration(Long exportFileId, LocalDate expirationDate) {
    log.info("Scheduling export file expiration WF: {}, on {}", exportFileId, expirationDate);

    String taskQueue = ExportFileExpirationHandlerWFImpl.TASK_QUEUE_EXPORT_FILE_EXPIRATION_HANDLER_WF;
    String workflowId = generateWorkflowId(exportFileId, ExportFileExpirationHandlerWF.class);

    ExportFileExpirationHandlerWF workflow = workflowService.buildWorkflowStubScheduled(
      ExportFileExpirationHandlerWF.class,
      taskQueue,
      workflowId,
      expirationDate
    );
    workflowClientService.start(workflow::exportFileExpirationHandler, exportFileId);
  }
}
