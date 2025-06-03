package it.gov.pagopa.pu.workflow.wf.ingestionflow.receipt.wfingestion;

import io.temporal.workflow.WorkflowInterface;
import io.temporal.workflow.WorkflowMethod;

/**
 * Workflow to ingest RT
 * @see <a href=https://pagopa.atlassian.net/wiki/spaces/SPAC/pages/1558675561/Import+Pagati>Confluence page</a>
 * */
@WorkflowInterface
public interface ReceiptIngestionWF {
  @WorkflowMethod
  void ingest(Long ingestionFlowFileId);
}
