package it.gov.pagopa.pu.workflow.wf.pagopa.send.payments.wf;

import io.temporal.workflow.WorkflowInterface;
import io.temporal.workflow.WorkflowMethod;
import it.gov.pagopa.pu.debtpositions.dto.generated.DebtPositionDTO;

/**
 * Workflow interface for handling SEND notification payments.
 * <p>
 * This workflow coordinates activities to notify a payment, retrieve the related
 * receipt, and publish a SEND timeline event when the notification is paid.
 * </p>
 * @see <a href=https://pagopa.atlassian.net/wiki/spaces/SPAC/pages/3401515261/Gestione+pagamento+notifica+SEND>Confluence page</a>
 * */
@WorkflowInterface
public interface SendNotificationPaymentHandlerWF {

  /**
   * Workflow method to notify a payment and publish a SEND timeline event if the notification is paid.
   *
   * @param debtPosition the debt position associated with the payment
   * @param receiptId the unique identifier of the payment receipt
   */
  @WorkflowMethod
  void notifyPayment(DebtPositionDTO debtPosition, Long receiptId);
}
