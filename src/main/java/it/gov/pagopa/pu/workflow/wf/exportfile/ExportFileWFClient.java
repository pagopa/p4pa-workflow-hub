package it.gov.pagopa.pu.workflow.wf.exportfile;

import static it.gov.pagopa.pu.workflow.utilities.Utilities.generateWorkflowId;

import io.temporal.client.WorkflowClient;
import it.gov.pagopa.pu.processexecutions.dto.generated.ExportFile;
import it.gov.pagopa.pu.workflow.service.WorkflowService;
import it.gov.pagopa.pu.workflow.wf.exportfile.wfexportfile.ExportFileWF;
import it.gov.pagopa.pu.workflow.wf.exportfile.wfexportfile.ExportFileWFImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class ExportFileWFClient {

  private final WorkflowService workflowService;

  public ExportFileWFClient(WorkflowService workflowService) {
    this.workflowService = workflowService;
  }

  public String exportFile(Long exportFileId, ExportFile.ExportFileTypeEnum exportFileType) {
    log.info("Starting export file for export file with id: {} and export file type: {}", exportFileId, exportFileType);

    String taskQueue = ExportFileWFImpl.TASK_QUEUE_EXPORT_FILE_WF;
    String workflowId  = generateWorkflowId(exportFileType+"-"+exportFileId, taskQueue);

    ExportFileWF workflow = workflowService.buildWorkflowStub(
      ExportFileWF.class,
      taskQueue,
      workflowId);
    WorkflowClient.start(workflow::exportFile, exportFileId,exportFileType);
    return workflowId;
  }
}
