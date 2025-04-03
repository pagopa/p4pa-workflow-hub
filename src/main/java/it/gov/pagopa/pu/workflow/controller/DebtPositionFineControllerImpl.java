package it.gov.pagopa.pu.workflow.controller;

import it.gov.pagopa.pu.workflow.controller.generated.DebtPositionFineApi;
import it.gov.pagopa.pu.workflow.dto.generated.WorkflowCreatedDTO;
import it.gov.pagopa.pu.workflow.service.debtposition.custom.fine.DebtPositionFineService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
public class DebtPositionFineControllerImpl implements DebtPositionFineApi {

  private final DebtPositionFineService debtPositionFineService;

  public DebtPositionFineControllerImpl(DebtPositionFineService debtPositionFineService) {
    this.debtPositionFineService = debtPositionFineService;
  }

  @Override
  public ResponseEntity<WorkflowCreatedDTO> expireFineReduction(Long debtPositionId) {
    log.info("Starting workflow to handle fine reduction expiration: {}", debtPositionId);
    return ResponseEntity.ok(debtPositionFineService.expireFineReduction(debtPositionId));
  }
}
