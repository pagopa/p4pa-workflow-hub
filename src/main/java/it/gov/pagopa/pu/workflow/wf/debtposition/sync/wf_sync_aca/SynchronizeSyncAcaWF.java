package it.gov.pagopa.pu.workflow.wf.debtposition.sync.wf_sync_aca;

import io.temporal.workflow.WorkflowInterface;
import io.temporal.workflow.WorkflowMethod;
import it.gov.pagopa.pu.debtposition.dto.generated.DebtPositionDTO;
import it.gov.pagopa.pu.workflow.event.payments.enums.PaymentEventType;

/** @see #synchronizeDPSyncAca(DebtPositionDTO, PaymentEventType) */
@WorkflowInterface
public interface SynchronizeSyncAcaWF {

  /** Workflow method to synchronize a SYNC ACA DebtPosition */
  @WorkflowMethod
  void synchronizeDPSyncAca(DebtPositionDTO debtPosition, PaymentEventType paymentEventType);
}
