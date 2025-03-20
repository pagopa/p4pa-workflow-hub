package it.gov.pagopa.pu.workflow.service.debtposition.sync.complete;

import it.gov.pagopa.payhub.activities.connector.workflowhub.dto.WfExecutionParameters;
import it.gov.pagopa.payhub.activities.dto.debtposition.GenericWfExecutionConfig;
import it.gov.pagopa.pu.debtposition.dto.generated.DebtPositionDTO;
import it.gov.pagopa.pu.workflow.dto.generated.PaymentEventType;
import it.gov.pagopa.pu.workflow.service.debtposition.sync.complete.custom.DebtPositionCustomWfSyncService;
import it.gov.pagopa.pu.workflow.service.debtposition.sync.complete.generic.DebtPositionGenericSyncService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class DebtPositionCompleteChangeSyncService {

  private final DebtPositionGenericSyncService genericWfSyncService;
  private final DebtPositionCustomWfSyncService customWfSyncService;

  public DebtPositionCompleteChangeSyncService(DebtPositionGenericSyncService genericWfSyncService, DebtPositionCustomWfSyncService customWfSyncService) {
    this.genericWfSyncService = genericWfSyncService;
    this.customWfSyncService = customWfSyncService;
  }

  public String invokeWorkflow(DebtPositionDTO debtPositionDTO, PaymentEventType paymentEventType, WfExecutionParameters wfExecutionParameters, String accessToken) {
    log.debug("Requested complete change on debtPosition {} (paymentEventType {})", debtPositionDTO.getDebtPositionId(), paymentEventType);

    if(wfExecutionParameters.getWfExecutionConfig() == null || wfExecutionParameters.getWfExecutionConfig() instanceof GenericWfExecutionConfig) {
      return genericWfSyncService.invokeWorkflow(debtPositionDTO, paymentEventType, wfExecutionParameters, accessToken);
    } else {
      return customWfSyncService.invokeWorkflow(debtPositionDTO, paymentEventType, wfExecutionParameters);
    }
  }
}
