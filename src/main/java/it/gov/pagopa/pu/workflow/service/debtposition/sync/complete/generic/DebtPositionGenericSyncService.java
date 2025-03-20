package it.gov.pagopa.pu.workflow.service.debtposition.sync.complete.generic;

import it.gov.pagopa.payhub.activities.connector.workflowhub.dto.WfExecutionParameters;
import it.gov.pagopa.pu.debtposition.dto.generated.DebtPositionDTO;
import it.gov.pagopa.pu.workflow.dto.generated.PaymentEventType;
import it.gov.pagopa.pu.workflow.wf.debtposition.sync.SynchronizeDebtPositionWfClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class DebtPositionGenericSyncService {

  private final PagoPASyncInteractionModelRetrieverService interactionModelRetrieverService;
  private final SynchronizeDebtPositionWfClient wfClient;

  public DebtPositionGenericSyncService(PagoPASyncInteractionModelRetrieverService interactionModelRetrieverService, SynchronizeDebtPositionWfClient wfClient) {
    this.interactionModelRetrieverService = interactionModelRetrieverService;
    this.wfClient = wfClient;
  }

  public String invokeWorkflow(DebtPositionDTO debtPositionDTO, PaymentEventType paymentEventType, WfExecutionParameters wfExecutionParameters, String accessToken) {
    log.debug("Requested complete change on debtPosition {} (paymentEventType {}) related to Generic WF sync", debtPositionDTO.getDebtPositionId(), paymentEventType);

    if (Boolean.FALSE.equals(debtPositionDTO.getFlagPagoPaPayment())) {
      return wfClient.synchronizeNoPagoPADP(debtPositionDTO, paymentEventType);
    }

    return switch (interactionModelRetrieverService.retrieveInteractionModel(debtPositionDTO.getOrganizationId(), accessToken)) {
      case SYNC ->
        wfClient.synchronizeDPSync(debtPositionDTO, paymentEventType);
      case SYNC_ACA ->
        wfClient.synchronizeDPSyncAca(debtPositionDTO, paymentEventType);
      case SYNC_GPDPRELOAD ->
        wfClient.synchronizeDPSyncGpdPreLoad(debtPositionDTO, paymentEventType);
      case SYNC_ACA_GPDPRELOAD ->
        wfClient.synchronizeDPSyncAcaGpdPreLoad(debtPositionDTO, paymentEventType);
      case ASYNC_GPD ->
        wfExecutionParameters.isMassive() ? null : wfClient.synchronizeDPAsyncGpd(debtPositionDTO, paymentEventType);
    };
  }
}
