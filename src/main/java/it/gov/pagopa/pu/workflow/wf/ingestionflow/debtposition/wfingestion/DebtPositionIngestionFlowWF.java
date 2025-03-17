package it.gov.pagopa.pu.workflow.wf.ingestionflow.debtposition.wfingestion;

import io.temporal.workflow.WorkflowInterface;
import io.temporal.workflow.WorkflowMethod;

/**
 * Workflow to ingest Debt Positions
 * @see <a href=https://pagopa.atlassian.net/wiki/spaces/SPAC/pages/1327890499/Posizione+Debitoria+Massiva>Confluence page</a>
 * */
@WorkflowInterface
public interface DebtPositionIngestionFlowWF {
  @WorkflowMethod
  void ingest(Long ingestionFlowFileId);
}
