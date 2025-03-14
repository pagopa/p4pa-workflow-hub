package it.gov.pagopa.pu.workflow.wf.ingestionflow.receipt.pagopa.wfingestion;

import io.temporal.workflow.WorkflowInterface;
import io.temporal.workflow.WorkflowMethod;

/**
 * Workflow to ingest RT originated from PagoPA
 * @see <a href=https://pagopa.atlassian.net/wiki/spaces/SPAC/pages/1473806561/Ricevute+RT>Confluence page</a>
 * */
@WorkflowInterface
public interface ReceiptPagopaIngestionWF {
  @WorkflowMethod
  void ingest(Long ingestionFlowFileId);
}
