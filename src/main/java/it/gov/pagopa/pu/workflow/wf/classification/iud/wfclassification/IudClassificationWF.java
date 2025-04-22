package it.gov.pagopa.pu.workflow.wf.classification.iud.wfclassification;

import io.temporal.workflow.SignalMethod;
import io.temporal.workflow.WorkflowInterface;
import io.temporal.workflow.WorkflowMethod;
import it.gov.pagopa.pu.workflow.wf.classification.iud.dto.IudClassificationNotifyPaymentNotificationSignalDTO;
import it.gov.pagopa.pu.workflow.wf.classification.iud.dto.IudClassificationNotifyReceiptSignalDTO;

/**
 * Workflow to handle IUF receipt classification
 * @see <a href=https://pagopa.atlassian.net/wiki/spaces/SPAC/pages/1339031693/Classificazione+incassi#3.5.1.-Classificazione-IUD>Confluence page</a>
 */
@WorkflowInterface
public interface IudClassificationWF {
  String SIGNAL_METHOD_NAME_NOTIFY_RECEIPT ="notifyReceipt";
  String SIGNAL_METHOD_NAME_NOTIFY_PAYMENT_NOTIFICATION ="notifyPaymentNotification";

  @WorkflowMethod
  void classify();

  @SignalMethod
  void notifyReceipt(IudClassificationNotifyReceiptSignalDTO signalDTO);

  @SignalMethod
  void notifyPaymentNotification(IudClassificationNotifyPaymentNotificationSignalDTO signalDTO);

}
