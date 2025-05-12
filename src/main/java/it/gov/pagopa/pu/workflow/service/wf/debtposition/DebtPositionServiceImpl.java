package it.gov.pagopa.pu.workflow.service.wf.debtposition;

import it.gov.pagopa.payhub.activities.connector.workflowhub.dto.WfExecutionParameters;
import it.gov.pagopa.pu.debtposition.dto.generated.DebtPositionDTO;
import it.gov.pagopa.pu.workflow.dto.PaymentEventRequestDTO;
import it.gov.pagopa.pu.workflow.dto.generated.WorkflowCreatedDTO;
import it.gov.pagopa.pu.workflow.service.wf.debtposition.sync.DebtPositionSyncService;
import it.gov.pagopa.pu.workflow.wf.debtposition.expirationdp.CheckDebtPositionExpirationWfClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class DebtPositionServiceImpl implements DebtPositionService {

  private final DebtPositionSyncService debtPositionSyncService;
  private final CheckDebtPositionExpirationWfClient checkDebtPositionExpirationWfClient;

  public DebtPositionServiceImpl(DebtPositionSyncService debtPositionSyncService, CheckDebtPositionExpirationWfClient checkDebtPositionExpirationWfClient) {
    this.debtPositionSyncService = debtPositionSyncService;
    this.checkDebtPositionExpirationWfClient = checkDebtPositionExpirationWfClient;
  }

  @Override
  public WorkflowCreatedDTO syncDebtPosition(DebtPositionDTO debtPositionDTO, PaymentEventRequestDTO paymentEventRequest, WfExecutionParameters wfExecutionParameters, String accessToken) {
    log.debug("Starting workflow to sync DebtPosition: {} (massive: {}, partial: {})", debtPositionDTO.getDebtPositionId(), wfExecutionParameters.isMassive(), wfExecutionParameters.isPartialChange());
    return debtPositionSyncService.invokeWorkflow(debtPositionDTO, paymentEventRequest, wfExecutionParameters, accessToken);
  }

  @Override
  public WorkflowCreatedDTO checkDpExpiration(Long debtPositionId) {
    log.debug("Starting workflow for checking expiration of debt position with debtPositionId: {}", debtPositionId);
    return checkDebtPositionExpirationWfClient.checkDpExpiration(debtPositionId);
  }

}
