package it.gov.pagopa.pu.workflow.controller;

import it.gov.pagopa.payhub.activities.connector.workflowhub.dto.WfExecutionParameters;
import it.gov.pagopa.pu.workflow.controller.generated.DebtPositionApi;
import it.gov.pagopa.pu.workflow.dto.generated.PaymentEventType;
import it.gov.pagopa.pu.workflow.dto.generated.SyncDebtPositionRequestDTO;
import it.gov.pagopa.pu.workflow.dto.generated.WorkflowCreatedDTO;
import it.gov.pagopa.pu.workflow.service.debtposition.DebtPositionService;
import it.gov.pagopa.pu.workflow.utilities.SecurityUtils;
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
  public ResponseEntity<WorkflowCreatedDTO> syncDebtPosition(SyncDebtPositionRequestDTO syncDebtPositionRequest, Boolean massive, Boolean partialChange, PaymentEventType paymentEventType) {
    WfExecutionParameters wfExecutionParameters = new WfExecutionParameters(
      Boolean.TRUE.equals(massive),
      Boolean.TRUE.equals(partialChange),
      syncDebtPositionRequest.getExecutionConfig());

    log.info("Starting workflow to synchronize DebtPosition: {} (massive: {}, partial: {})",
      syncDebtPositionRequest.getDebtPosition().getDebtPositionId(),
      wfExecutionParameters.isMassive(),
      wfExecutionParameters.isPartialChange());

    return ResponseEntity.ok(
      service.syncDebtPosition(
        syncDebtPositionRequest.getDebtPosition(), paymentEventType,
        wfExecutionParameters,
        SecurityUtils.getAccessToken()));
  }

  @Override
  public ResponseEntity<WorkflowCreatedDTO> checkDpExpiration(Long debtPositionId) {
    log.info("Starting workflow for handle expiration of debt position with debtPositionId: {}", debtPositionId);
    WorkflowCreatedDTO createWorkflowResponseDTO = service.checkDpExpiration(debtPositionId);
    return new ResponseEntity<>(createWorkflowResponseDTO, HttpStatus.OK);
  }
}
