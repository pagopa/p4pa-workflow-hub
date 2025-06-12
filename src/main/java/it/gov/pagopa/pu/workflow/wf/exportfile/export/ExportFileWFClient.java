package it.gov.pagopa.pu.workflow.wf.exportfile.export;

import it.gov.pagopa.pu.processexecutions.dto.generated.ExportFile;
import it.gov.pagopa.pu.workflow.dto.generated.WorkflowCreatedDTO;
import it.gov.pagopa.pu.workflow.service.temporal.WorkflowClientService;
import it.gov.pagopa.pu.workflow.service.temporal.WorkflowService;
import it.gov.pagopa.pu.workflow.utilities.TaskQueueConstants;
import it.gov.pagopa.pu.workflow.wf.exportfile.export.wfexportfile.ExportFileWF;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import static it.gov.pagopa.pu.workflow.utilities.Utilities.generateWorkflowId;

@Slf4j
@Service
public class ExportFileWFClient {

  private final WorkflowService workflowService;
  private final WorkflowClientService workflowClientService;

  public ExportFileWFClient(WorkflowService workflowService, WorkflowClientService workflowClientService) {
    this.workflowService = workflowService;
    this.workflowClientService = workflowClientService;
  }

  public WorkflowCreatedDTO exportFile(Long exportFileId, ExportFile.ExportFileTypeEnum exportFileType) {
    log.info("Starting export file for export file with id: {} and export file type: {}", exportFileId, exportFileType);

    String taskQueue = TaskQueueConstants.TASK_QUEUE_EXPORT_MEDIUM_PRIORITY;
    String workflowId  = generateWorkflowId(exportFileType+"-"+exportFileId, ExportFileWF.class);

    ExportFileWF workflow = workflowService.buildWorkflowStub(
      ExportFileWF.class,
      taskQueue,
      workflowId);
    return workflowClientService.start(workflow::exportFile, exportFileId, exportFileType);
  }
}
