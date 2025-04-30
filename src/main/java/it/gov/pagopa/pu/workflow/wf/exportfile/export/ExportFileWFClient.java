package it.gov.pagopa.pu.workflow.wf.exportfile.export;

import static it.gov.pagopa.pu.workflow.utilities.Utilities.generateWorkflowId;

import io.temporal.client.WorkflowClient;
import it.gov.pagopa.pu.processexecutions.dto.generated.ExportFile;
import it.gov.pagopa.pu.workflow.dto.generated.WorkflowCreatedDTO;
import it.gov.pagopa.pu.workflow.mapper.WorkflowCreatedMapper;
import it.gov.pagopa.pu.workflow.service.WorkflowService;
import it.gov.pagopa.pu.workflow.wf.exportfile.export.wfexportfile.ExportFileWF;
import it.gov.pagopa.pu.workflow.wf.exportfile.export.wfexportfile.ExportFileWFImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class ExportFileWFClient {

  private final WorkflowService workflowService;

  public ExportFileWFClient(WorkflowService workflowService) {
    this.workflowService = workflowService;
  }

  public WorkflowCreatedDTO exportFile(Long exportFileId, ExportFile.ExportFileTypeEnum exportFileType) {
    log.info("Starting export file for export file with id: {} and export file type: {}", exportFileId, exportFileType);

    String taskQueue = ExportFileWFImpl.TASK_QUEUE_EXPORT_FILE_WF;
    String workflowId  = generateWorkflowId(exportFileType+"-"+exportFileId, ExportFileWF.class);

    ExportFileWF workflow = workflowService.buildWorkflowStub(
      ExportFileWF.class,
      taskQueue,
      workflowId);
    WorkflowCreatedDTO wfExec = WorkflowCreatedMapper.map(WorkflowClient.start(workflow::exportFile, exportFileId, exportFileType));
    log.info("Started workflow: {}", wfExec);
    return wfExec;
  }
}
