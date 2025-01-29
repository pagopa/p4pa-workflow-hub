package it.gov.pagopa.pu.workflow.controller.debtposition;

import it.gov.pagopa.pu.workflow.controller.generated.DebtPositionApi;
import it.gov.pagopa.pu.workflow.dto.generated.DebtPositionRequestDTO;
import it.gov.pagopa.pu.workflow.dto.generated.WorkflowCreatedDTO;
import it.gov.pagopa.pu.workflow.service.debtposition.DebtPositionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
public class DebtPositionControllerImpl implements DebtPositionApi {

  private final DebtPositionService service;

  public DebtPositionControllerImpl(DebtPositionService service) {
    this.service = service;
  }

  @Override
  public ResponseEntity<WorkflowCreatedDTO> createDpSync(DebtPositionRequestDTO debtPositionRequestDTO) {
    log.info("Starting workflow for creation debt position sync with debtPositionId: {}", debtPositionRequestDTO.getDebtPositionId());
    WorkflowCreatedDTO createDpSyncResponseDTO = service.createDPSync(debtPositionRequestDTO);
    return new ResponseEntity<>(createDpSyncResponseDTO, HttpStatus.OK);
  }

  @Override
  public ResponseEntity<WorkflowCreatedDTO> alignDpSyncAca(DebtPositionRequestDTO debtPositionRequestDTO) {
    log.info("Starting workflow for align debt position sync on ACA with debtPositionId: {}", debtPositionRequestDTO.getDebtPositionId());
    WorkflowCreatedDTO createDpSyncResponseDTO = service.alignDpSyncAca(debtPositionRequestDTO);
    return new ResponseEntity<>(createDpSyncResponseDTO, HttpStatus.OK);
  }
}
