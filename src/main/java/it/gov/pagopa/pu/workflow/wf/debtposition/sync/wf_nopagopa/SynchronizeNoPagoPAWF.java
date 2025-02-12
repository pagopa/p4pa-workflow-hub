package it.gov.pagopa.pu.workflow.wf.debtposition.sync.wf_nopagopa;

import io.temporal.workflow.WorkflowInterface;
import io.temporal.workflow.WorkflowMethod;
import it.gov.pagopa.pu.debtposition.dto.generated.DebtPositionDTO;
import it.gov.pagopa.pu.workflow.dto.generated.PaymentEventType;

/** @see #synchronizeDPNoPagoPA(DebtPositionDTO, PaymentEventType) */
@WorkflowInterface
public interface SynchronizeNoPagoPAWF {

  /** Workflow method to synchronize a not PagoPA DebtPosition */
  @WorkflowMethod
  void synchronizeDPNoPagoPA(DebtPositionDTO debtPosition, PaymentEventType paymentEventType);
}
