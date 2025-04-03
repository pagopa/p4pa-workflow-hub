package it.gov.pagopa.pu.workflow.service.debtposition.sync.complete.generic;

import it.gov.pagopa.payhub.activities.dto.debtposition.syncwfconfig.GenericWfExecutionConfig;
import it.gov.pagopa.pu.debtposition.dto.generated.DebtPositionDTO;
import it.gov.pagopa.pu.workflow.dto.PaymentEventRequestDTO;
import it.gov.pagopa.pu.workflow.wf.debtposition.sync.SynchronizeDebtPositionWfClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

@Slf4j
@Lazy
@Service
public class DebtPositionGenericSyncService {

  private final PagoPASyncInteractionModelRetrieverService interactionModelRetrieverService;
  private final SynchronizeDebtPositionWfClient wfClient;

  public DebtPositionGenericSyncService(PagoPASyncInteractionModelRetrieverService interactionModelRetrieverService, SynchronizeDebtPositionWfClient wfClient) {
    this.interactionModelRetrieverService = interactionModelRetrieverService;
    this.wfClient = wfClient;
  }

  public String invokeWorkflow(DebtPositionDTO debtPositionDTO, PaymentEventRequestDTO paymentEventRequest, boolean massive, GenericWfExecutionConfig wfExecutionConfig, String accessToken) {
    log.debug("Requested complete change on debtPosition {} (paymentEventType {}) related to Generic WF sync",
      debtPositionDTO.getDebtPositionId(),
      paymentEventRequest!=null? paymentEventRequest.getPaymentEventType() : null);

    if (Boolean.FALSE.equals(debtPositionDTO.getFlagPagoPaPayment())) {
      return wfClient.synchronizeNoPagoPADP(debtPositionDTO, paymentEventRequest, wfExecutionConfig);
    }

    return switch (interactionModelRetrieverService.retrieveInteractionModel(debtPositionDTO.getOrganizationId(), accessToken)) {
      case SYNC ->
        wfClient.synchronizeDPSync(debtPositionDTO, paymentEventRequest, wfExecutionConfig);
      case SYNC_ACA ->
        wfClient.synchronizeDPSyncAca(debtPositionDTO, paymentEventRequest, wfExecutionConfig);
      case SYNC_GPDPRELOAD ->
        wfClient.synchronizeDPSyncGpdPreLoad(debtPositionDTO, paymentEventRequest, wfExecutionConfig);
      case SYNC_ACA_GPDPRELOAD ->
        wfClient.synchronizeDPSyncAcaGpdPreLoad(debtPositionDTO, paymentEventRequest, wfExecutionConfig);
      case ASYNC_GPD ->
        massive ? null : wfClient.synchronizeDPAsyncGpd(debtPositionDTO, paymentEventRequest, wfExecutionConfig);
    };
  }
}
