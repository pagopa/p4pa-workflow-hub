package it.gov.pagopa.pu.workflow.wf.debtposition.sync;

import it.gov.pagopa.pu.debtposition.dto.generated.DebtPositionDTO;
import it.gov.pagopa.pu.workflow.dto.generated.PaymentEventType;

public interface SynchronizeDebtPositionWfClient {
  String synchronizeNoPagoPADP(DebtPositionDTO debtPositionDTO, PaymentEventType paymentEventType);
  String synchronizeDPSync(DebtPositionDTO debtPositionDTO, PaymentEventType paymentEventType);
  String synchronizeDPSyncAca(DebtPositionDTO debtPositionDTO, PaymentEventType paymentEventType);
  String synchronizeDPSyncGpdPreLoad(DebtPositionDTO debtPositionDTO, PaymentEventType paymentEventType);
  String synchronizeDPSyncAcaGpdPreLoad(DebtPositionDTO debtPositionDTO, PaymentEventType paymentEventType);
  String synchronizeDPAsyncGpd(DebtPositionDTO debtPositionDTO, PaymentEventType paymentEventType);
}
