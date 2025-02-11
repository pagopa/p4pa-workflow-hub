package it.gov.pagopa.pu.workflow.wf.debtposition.sync;

import it.gov.pagopa.pu.debtposition.dto.generated.DebtPositionDTO;
import it.gov.pagopa.pu.workflow.event.payments.enums.PaymentEventType;

public interface SynchronizeDebtPositionWfClient {
  String synchronizeNoPagoPADPSync(DebtPositionDTO debtPositionDTO, PaymentEventType paymentEventType);
  String synchronizeDPSync(DebtPositionDTO debtPositionDTO, PaymentEventType paymentEventType);
  String synchronizeDPSyncAca(DebtPositionDTO debtPositionDTO, PaymentEventType paymentEventType);
  String synchronizeDPSyncGpdPreload(DebtPositionDTO debtPositionDTO, PaymentEventType paymentEventType);
  String synchronizeDPSyncAcaGpdPreload(DebtPositionDTO debtPositionDTO, PaymentEventType paymentEventType);
  String synchronizeDPAsyncGpd(DebtPositionDTO debtPositionDTO, PaymentEventType paymentEventType);
}
