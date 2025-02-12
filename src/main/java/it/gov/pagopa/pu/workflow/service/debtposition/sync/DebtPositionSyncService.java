package it.gov.pagopa.pu.workflow.service.debtposition.sync;

import it.gov.pagopa.pu.debtposition.dto.generated.DebtPositionDTO;
import it.gov.pagopa.pu.workflow.dto.generated.PaymentEventType;
import it.gov.pagopa.pu.workflow.wf.debtposition.sync.SynchronizeDebtPositionWfClient;
import org.springframework.stereotype.Service;

@Service
public class DebtPositionSyncService {

  private final PagoPASyncInteractionModelRetrieverService interactionModelRetrieverService;
  private final SynchronizeDebtPositionWfClient wfClient;

  public DebtPositionSyncService(PagoPASyncInteractionModelRetrieverService interactionModelRetrieverService, SynchronizeDebtPositionWfClient wfClient) {
    this.interactionModelRetrieverService = interactionModelRetrieverService;
    this.wfClient = wfClient;
  }

  public String invokeWorkflow(DebtPositionDTO debtPositionDTO, PaymentEventType paymentEventType, boolean massive, String accessToken) {
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
        massive ? null : wfClient.synchronizeDPAsyncGpd(debtPositionDTO, paymentEventType);
    };
  }
}
