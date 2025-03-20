package it.gov.pagopa.pu.workflow.wf.debtposition.sync;

import it.gov.pagopa.payhub.activities.dto.debtposition.syncwfconfig.GenericWfExecutionConfig;
import it.gov.pagopa.pu.debtposition.dto.generated.DebtPositionDTO;
import it.gov.pagopa.pu.workflow.dto.generated.PaymentEventType;

public interface SynchronizeDebtPositionWfClient {
  String synchronizeNoPagoPADP(DebtPositionDTO debtPositionDTO, PaymentEventType paymentEventType, GenericWfExecutionConfig wfExecutionConfig);
  String synchronizeDPSync(DebtPositionDTO debtPositionDTO, PaymentEventType paymentEventType, GenericWfExecutionConfig wfExecutionConfig);
  String synchronizeDPSyncAca(DebtPositionDTO debtPositionDTO, PaymentEventType paymentEventType, GenericWfExecutionConfig wfExecutionConfig);
  String synchronizeDPSyncGpdPreLoad(DebtPositionDTO debtPositionDTO, PaymentEventType paymentEventType, GenericWfExecutionConfig wfExecutionConfig);
  String synchronizeDPSyncAcaGpdPreLoad(DebtPositionDTO debtPositionDTO, PaymentEventType paymentEventType, GenericWfExecutionConfig wfExecutionConfig);
  String synchronizeDPAsyncGpd(DebtPositionDTO debtPositionDTO, PaymentEventType paymentEventType, GenericWfExecutionConfig wfExecutionConfig);
}
