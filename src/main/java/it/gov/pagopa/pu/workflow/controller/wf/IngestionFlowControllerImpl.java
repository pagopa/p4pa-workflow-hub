package it.gov.pagopa.pu.workflow.controller.wf;

import it.gov.pagopa.pu.processexecutions.dto.generated.IngestionFlowFile;
import it.gov.pagopa.pu.workflow.controller.generated.IngestionFlowApi;
import it.gov.pagopa.pu.workflow.dto.generated.WorkflowCreatedDTO;
import it.gov.pagopa.pu.workflow.service.wf.ingestionflowfile.IngestionFlowFileStarterService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
public class IngestionFlowControllerImpl implements IngestionFlowApi {

  private final IngestionFlowFileStarterService service;

  public IngestionFlowControllerImpl(IngestionFlowFileStarterService service) {
    this.service = service;
  }

  @Override
  public ResponseEntity<WorkflowCreatedDTO> ingestFlowFile(Long ingestionFlowFileId, IngestionFlowFile.IngestionFlowFileTypeEnum flowFileType) {
    log.info("Creating IngestionFlowFile Workflow for ingestionFlowFileId {} of type {}", ingestionFlowFileId, flowFileType);

    WorkflowCreatedDTO wfExec = service.ingest(ingestionFlowFileId, flowFileType);
    return ResponseEntity.status(201).body(wfExec);
  }

}
