package it.gov.pagopa.pu.workflow.service.exportfile;

import it.gov.pagopa.pu.processexecutions.dto.generated.ExportFile.ExportFileTypeEnum;
import it.gov.pagopa.pu.workflow.dto.generated.WorkflowCreatedDTO;
import it.gov.pagopa.pu.workflow.wf.exportfile.ExportFileWFClient;
import it.gov.pagopa.pu.workflow.wf.exportfile.expiration.ExportFileExpirationHandlerWFClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class ExportFileServiceImpl implements ExportFileService {

  private final ExportFileWFClient exportFileWFClient;
  private final ExportFileExpirationHandlerWFClient exportFileExpirationHandlerWFClient;

  public ExportFileServiceImpl(ExportFileWFClient exportFileWFClient,
    ExportFileExpirationHandlerWFClient exportFileExpirationHandlerWFClient) {
    this.exportFileWFClient = exportFileWFClient;
    this.exportFileExpirationHandlerWFClient = exportFileExpirationHandlerWFClient;
  }

  @Override
  public WorkflowCreatedDTO expireExportFile(Long exportFileId) {
    log.debug("Starting expireExportFile for exportFileId: {}", exportFileId);
    String workflowId = exportFileExpirationHandlerWFClient.exportFileExpirationHandler(exportFileId);
    return buildWorkflowCreatedDTO(workflowId);
  }

  @Override
  public WorkflowCreatedDTO create(Long exportFileId,
    ExportFileTypeEnum exportFileType) {
    log.debug("Starting createExportFile for exportFileId: {} and type: {}", exportFileId, exportFileType);
    String workflowId = exportFileWFClient.exportFile(exportFileId, exportFileType);
    return buildWorkflowCreatedDTO(workflowId);
  }

  private WorkflowCreatedDTO buildWorkflowCreatedDTO(String workflowId) {
    return WorkflowCreatedDTO.builder()
      .workflowId(workflowId)
      .build();
  }
}
