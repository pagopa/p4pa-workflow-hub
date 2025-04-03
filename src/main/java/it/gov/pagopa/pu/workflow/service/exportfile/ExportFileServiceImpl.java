package it.gov.pagopa.pu.workflow.service.exportfile;

import it.gov.pagopa.pu.workflow.dto.generated.WorkflowCreatedDTO;
import it.gov.pagopa.pu.workflow.wf.exportfile.expiration.ExportFileExpirationHandlerWFClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class ExportFileServiceImpl implements ExportFileService {

  private final ExportFileExpirationHandlerWFClient exportFileExpirationHandlerWFClient;

  public ExportFileServiceImpl(ExportFileExpirationHandlerWFClient exportFileExpirationHandlerWFClient) {
    this.exportFileExpirationHandlerWFClient = exportFileExpirationHandlerWFClient;
  }

  @Override
  public WorkflowCreatedDTO exportFileExpirationHandler(Long exportFileId) {
    log.info("Starting exportFileExpirationHandler for exportFileId: {}", exportFileId);
    String workflowId = exportFileExpirationHandlerWFClient.exportFileExpirationHandler(exportFileId);
    return buildWorkflowCreatedDTO(workflowId);
  }

  private WorkflowCreatedDTO buildWorkflowCreatedDTO(String workflowId) {
    return WorkflowCreatedDTO.builder()
      .workflowId(workflowId)
      .build();
  }
}
