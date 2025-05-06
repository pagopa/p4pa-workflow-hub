package it.gov.pagopa.pu.workflow.service.debtposition.sync.complete;

import it.gov.pagopa.payhub.activities.connector.workflowhub.dto.WfExecutionParameters;
import it.gov.pagopa.payhub.activities.dto.debtposition.syncwfconfig.GenericWfExecutionConfig;
import it.gov.pagopa.pu.debtposition.dto.generated.DebtPositionDTO;
import it.gov.pagopa.pu.workflow.dto.PaymentEventRequestDTO;
import it.gov.pagopa.pu.workflow.dto.generated.WorkflowCreatedDTO;
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

  public WorkflowCreatedDTO invokeWorkflow(DebtPositionDTO debtPositionDTO, PaymentEventRequestDTO paymentEventRequest, WfExecutionParameters wfExecutionParameters, String accessToken) {
    log.debug("Requested complete change on debtPosition {} (paymentEventType {})",
      debtPositionDTO.getDebtPositionId(),
      paymentEventRequest!=null? paymentEventRequest.getPaymentEventType() : null);

    if(wfExecutionParameters.getWfExecutionConfig() == null || wfExecutionParameters.getWfExecutionConfig() instanceof GenericWfExecutionConfig) {
      return genericWfSyncService.invokeWorkflow(debtPositionDTO, paymentEventRequest, wfExecutionParameters.isMassive(), (GenericWfExecutionConfig)wfExecutionParameters.getWfExecutionConfig(), accessToken);
    } else {
      return customWfSyncService.invokeWorkflow(debtPositionDTO, paymentEventRequest, wfExecutionParameters);
    }
  }
}
