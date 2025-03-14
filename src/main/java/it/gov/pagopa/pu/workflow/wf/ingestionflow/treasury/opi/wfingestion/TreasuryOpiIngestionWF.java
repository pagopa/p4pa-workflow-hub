package it.gov.pagopa.pu.workflow.wf.ingestionflow.treasury.opi.wfingestion;

import io.temporal.workflow.WorkflowInterface;
import io.temporal.workflow.WorkflowMethod;

/**
 * Workflow to ingest Treasury OPI file
 * @see <a href=https://pagopa.atlassian.net/wiki/spaces/SPAC/pages/1339031693/Classificazione+incassi#3.4.1.-Tesoreria-IUFs>Confluence page</a>
 */
@WorkflowInterface
public interface TreasuryOpiIngestionWF {
  @WorkflowMethod
  void ingest(Long ingestionFlowFileId);
}
