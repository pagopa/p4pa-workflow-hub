package it.gov.pagopa.pu.workflow.wf.debtposition.sync.wf_sync_aca_gpdpreload;

import io.temporal.workflow.WorkflowInterface;
import io.temporal.workflow.WorkflowMethod;
import it.gov.pagopa.pu.debtposition.dto.generated.DebtPositionDTO;
import it.gov.pagopa.pu.workflow.dto.generated.PaymentEventType;

/** @see #synchronizeDPSyncAcaGpdPreLoad(DebtPositionDTO, PaymentEventType) */
@WorkflowInterface
public interface SynchronizeSyncAcaGpdPreLoadWF {

  /** Workflow method to synchronize a SYNC ACA+GPD PreLoad DebtPosition */
  @WorkflowMethod
  void synchronizeDPSyncAcaGpdPreLoad(DebtPositionDTO debtPosition, PaymentEventType paymentEventType);
}
