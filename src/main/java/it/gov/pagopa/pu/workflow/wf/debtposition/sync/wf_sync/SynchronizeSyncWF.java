package it.gov.pagopa.pu.workflow.wf.debtposition.sync.wf_sync;

import io.temporal.workflow.WorkflowInterface;
import io.temporal.workflow.WorkflowMethod;
import it.gov.pagopa.pu.debtposition.dto.generated.DebtPositionDTO;
import it.gov.pagopa.pu.workflow.dto.generated.PaymentEventType;

/** @see #synchronizeDPSync(DebtPositionDTO, PaymentEventType)  */
@WorkflowInterface
public interface SynchronizeSyncWF {

  /** Workflow method to synchronize a SYNC DebtPosition */
  @WorkflowMethod
  void synchronizeDPSync(DebtPositionDTO debtPosition, PaymentEventType paymentEventType);
}
