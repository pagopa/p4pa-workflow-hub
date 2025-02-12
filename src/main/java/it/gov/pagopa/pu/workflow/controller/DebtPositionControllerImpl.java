package it.gov.pagopa.pu.workflow.controller;

import it.gov.pagopa.pu.debtposition.dto.generated.DebtPositionDTO;
import it.gov.pagopa.pu.workflow.controller.generated.DebtPositionApi;
import it.gov.pagopa.pu.workflow.dto.generated.WorkflowCreatedDTO;
import it.gov.pagopa.pu.workflow.event.payments.enums.PaymentEventType;
import it.gov.pagopa.pu.workflow.service.debtposition.DebtPositionService;
import it.gov.pagopa.pu.workflow.utilities.SecurityUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@RestController
@Slf4j
public class DebtPositionControllerImpl implements DebtPositionApi {

  private final DebtPositionService service;

  public DebtPositionControllerImpl(DebtPositionService service) {
    this.service = service;
  }

  @Override
  public ResponseEntity<WorkflowCreatedDTO> syncDebtPosition(DebtPositionDTO debtPositionDTO, Boolean massive, PaymentEventType paymentEventType) {
    log.info("Starting workflow to synchronize DebtPosition: {} (massive context: {})", debtPositionDTO.getDebtPositionId(), massive);
    return ResponseEntity.ok(
      service.syncDebtPosition(
        debtPositionDTO, paymentEventType,
        Optional.ofNullable(massive).orElse(false),
        SecurityUtils.getAccessToken()));
  }

  @Override
  public ResponseEntity<WorkflowCreatedDTO> checkDpExpiration(Long debtPositionId) {
    log.info("Starting workflow for handle expiration of debt position with debtPositionId: {}", debtPositionId);
    WorkflowCreatedDTO createWorkflowResponseDTO = service.checkDpExpiration(debtPositionId);
    return new ResponseEntity<>(createWorkflowResponseDTO, HttpStatus.OK);
  }
}
