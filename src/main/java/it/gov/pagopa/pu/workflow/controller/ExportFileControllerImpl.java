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
    log.info("Creating Export File Workflow for exportFileId {} of type {}", exportFileId, flowFileType);
    WorkflowCreatedDTO response = service.create(exportFileId, flowFileType);
    log.info("Export File workflow {} created successfully for exportFileId {} of type {}", response.getWorkflowId(), exportFileId, flowFileType);
    return ResponseEntity.status(201).body(response);
  }

}
