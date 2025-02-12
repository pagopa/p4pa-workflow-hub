package it.gov.pagopa.pu.workflow.wf.debtposition.sync.wf_sync_gpdpreload;

import io.temporal.workflow.WorkflowInterface;
import io.temporal.workflow.WorkflowMethod;
import it.gov.pagopa.pu.debtposition.dto.generated.DebtPositionDTO;
import it.gov.pagopa.pu.workflow.dto.generated.PaymentEventType;

/** @see #synchronizeDPSyncGpdPreLoad(DebtPositionDTO, PaymentEventType) */
@WorkflowInterface
public interface SynchronizeSyncGpdPreLoadWF {

  /** Workflow method to synchronize a SYNC GPD PreLoad DebtPosition */
  @WorkflowMethod
  void synchronizeDPSyncGpdPreLoad(DebtPositionDTO debtPosition, PaymentEventType paymentEventType);
}
