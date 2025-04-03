package it.gov.pagopa.pu.workflow.controller;

import it.gov.pagopa.pu.processexecutions.dto.generated.ExportFile;
import it.gov.pagopa.pu.workflow.controller.generated.ExportFileApi;
import it.gov.pagopa.pu.workflow.dto.generated.WorkflowCreatedDTO;
import it.gov.pagopa.pu.workflow.service.exportfile.ExportFileService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
public class ExportFileControllerImpl implements ExportFileApi {

  private final ExportFileService service;

  public ExportFileControllerImpl(ExportFileService service) {
    this.service = service;
  }

  @Override
  public ResponseEntity<WorkflowCreatedDTO> expireExportFile(Long exportFileId) {
    log.info("Starting expireExportFile for exportFileId: {}", exportFileId);
    WorkflowCreatedDTO createWorkflowResponseDTO = service.expireExportFile(exportFileId);
    return new ResponseEntity<>(createWorkflowResponseDTO, HttpStatus.OK);
  }

  @Override
  public ResponseEntity<WorkflowCreatedDTO> exportFile(Long exportFileId, ExportFile.ExportFileTypeEnum flowFileType) {
    log.info("Creating IngestionFlowFile Workflow for exportFileId {} of type {}", exportFileId, flowFileType);

    // TODO: call workflow String workflowId = ;

    /*WorkflowCreatedDTO response = new WorkflowCreatedDTO(workflowId);
    response.setWorkflowId(workflowId);

    log.info("Ingestion workflow {} created successfully for ingestionFileId {} of type {}", workflowId, exportFileId, flowFileType);

    return ResponseEntity.status(201).body(response);*/

    return ResponseEntity.ok(new WorkflowCreatedDTO());
  }

}
