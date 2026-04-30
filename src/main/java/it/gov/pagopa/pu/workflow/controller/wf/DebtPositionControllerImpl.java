package it.gov.pagopa.pu.workflow.controller.wf;

import it.gov.pagopa.payhub.activities.connector.workflowhub.dto.WfExecutionParameters;
import it.gov.pagopa.pu.workflow.controller.generated.DebtPositionApi;
import it.gov.pagopa.pu.workflow.dto.PaymentEventRequestDTO;
import it.gov.pagopa.pu.workflow.dto.generated.MassiveDebtPositionIbanUpdateRequestDTO;
import it.gov.pagopa.pu.workflow.dto.generated.PaymentEventType;
import it.gov.pagopa.pu.workflow.dto.generated.SyncDebtPositionRequestDTO;
import it.gov.pagopa.pu.workflow.dto.generated.WorkflowCreatedDTO;
import it.gov.pagopa.pu.workflow.service.wf.debtposition.DebtPositionService;
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
  public ResponseEntity<WorkflowCreatedDTO> syncDebtPosition(SyncDebtPositionRequestDTO syncDebtPositionRequest, Boolean massive, Boolean partialChange, PaymentEventType paymentEventType, String paymentEventDescription) {
    WfExecutionParameters wfExecutionParameters = new WfExecutionParameters(
      Boolean.TRUE.equals(massive),
      Boolean.TRUE.equals(partialChange),
      syncDebtPositionRequest.getExecutionConfig());

    log.info("Starting workflow to synchronize DebtPosition: {} (massive: {}, partial: {}) and evenType: {}",
      syncDebtPositionRequest.getDebtPosition().getDebtPositionId(),
      wfExecutionParameters.isMassive(),
      wfExecutionParameters.isPartialChange(),
      paymentEventType);

    return ResponseEntity.ok(
      service.syncDebtPosition(
        syncDebtPositionRequest.getDebtPosition(),
        paymentEventType != null? new PaymentEventRequestDTO(paymentEventType, paymentEventDescription) : null,
        wfExecutionParameters,
        SecurityUtils.getAccessToken()));
  }

  @Override
  public ResponseEntity<WorkflowCreatedDTO> checkDpExpiration(Long debtPositionId) {
    log.info("Starting workflow for handle expiration of debt position with debtPositionId: {}", debtPositionId);
    WorkflowCreatedDTO wfExec = service.checkDpExpiration(debtPositionId);
    return new ResponseEntity<>(wfExec, HttpStatus.OK);
  }

  @Override
  public ResponseEntity<WorkflowCreatedDTO> massiveDpIbanUpdate(Long orgId, MassiveDebtPositionIbanUpdateRequestDTO massiveDebtPositionIbanUpdateRequestDTO) {
    log.info("Starting workflow for handle massive debt position iban updade having orgId: {}", orgId);
    return ResponseEntity.ok(service.massiveIbanUpdate(orgId, massiveDebtPositionIbanUpdateRequestDTO));
  }
}
