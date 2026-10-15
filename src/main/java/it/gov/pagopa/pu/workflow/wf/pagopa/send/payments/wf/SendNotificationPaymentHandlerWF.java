package it.gov.pagopa.pu.workflow.wf.pagopa.send.payments.wf;

import io.temporal.workflow.WorkflowInterface;
import io.temporal.workflow.WorkflowMethod;
import it.gov.pagopa.pu.debtpositions.dto.generated.DebtPositionDTO;

@WorkflowInterface
public interface SendNotificationPaymentHandlerWF {

  @WorkflowMethod
  void notifyPayment(DebtPositionDTO debtPosition, Long receiptId);
}
