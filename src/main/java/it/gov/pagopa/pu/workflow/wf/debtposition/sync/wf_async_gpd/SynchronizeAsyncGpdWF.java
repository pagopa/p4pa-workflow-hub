package it.gov.pagopa.pu.workflow.wf.debtposition.sync.wf_async_gpd;

import io.temporal.workflow.WorkflowInterface;
import io.temporal.workflow.WorkflowMethod;
import it.gov.pagopa.pu.debtposition.dto.generated.DebtPositionDTO;
import it.gov.pagopa.pu.workflow.event.payments.enums.PaymentEventType;

/** @see #synchronizeDPAsyncGpd(DebtPositionDTO, PaymentEventType)  */
@WorkflowInterface
public interface SynchronizeAsyncGpdWF {

  /** Workflow method to synchronize a GPD DebtPosition */
  @WorkflowMethod
  void synchronizeDPAsyncGpd(DebtPositionDTO debtPosition, PaymentEventType paymentEventType);
}
