package it.gov.pagopa.pu.workflow.wf.ingestionflow.paymentnotification.wfingestion;

import io.temporal.workflow.WorkflowInterface;
import io.temporal.workflow.WorkflowMethod;

/**
 * Workflow to ingest PaymentNotification file
 * @see <a href=https://pagopa.atlassian.net/wiki/spaces/SPAC/pages/1339031693/Classificazione+incassi#3.2.-Pagamento-Notificati>Confluence page</a>
 */
@WorkflowInterface
public interface PaymentNotificationIngestionWF {
  @WorkflowMethod
  void ingest(Long ingestionFlowFileId);
}
