package it.gov.pagopa.pu.workflow.controller;

import it.gov.pagopa.pu.workflow.controller.generated.DebtPositionApi;
import it.gov.pagopa.pu.debtposition.dto.generated.DebtPositionDTO;
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
  public ResponseEntity<WorkflowCreatedDTO> handleDpSync(DebtPositionDTO debtPositionDTO) {
    log.info("Starting workflow to handling debt position sync with debtPositionId: {}", debtPositionDTO.getDebtPositionId());
    WorkflowCreatedDTO createDpSyncResponseDTO = service.handleDPSync(debtPositionDTO);
    return new ResponseEntity<>(createDpSyncResponseDTO, HttpStatus.OK);
  }

  @Override
  public ResponseEntity<WorkflowCreatedDTO> alignDpSyncAca(DebtPositionDTO debtPositionDTO) {
    log.info("Starting workflow for align debt position sync on ACA with debtPositionId: {}", debtPositionDTO.getDebtPositionId());
    WorkflowCreatedDTO createDpSyncResponseDTO = service.alignDpSyncAca(debtPositionDTO);
    return new ResponseEntity<>(createDpSyncResponseDTO, HttpStatus.OK);
  }

  @Override
  public ResponseEntity<WorkflowCreatedDTO> checkDpExpiration(Long debtPositionId){
    log.info("Starting workflow for handle expiration of debt position with debtPositionId: {}", debtPositionId);
    WorkflowCreatedDTO createWorkflowResponseDTO = service.checkDpExpiration(debtPositionId);
    return new ResponseEntity<>(createWorkflowResponseDTO, HttpStatus.OK);
  }
}
