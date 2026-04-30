package it.gov.pagopa.pu.workflow.wf.ingestionflow.debtposition.wfingestion;

import io.temporal.workflow.WorkflowInterface;
import io.temporal.workflow.WorkflowMethod;
import it.gov.pagopa.pu.workflow.wf.ingestionflow.BaseIngestionFlowFileWF;

/**
 * Workflow to ingest Debt Positions
 * @see <a href=https://pagopa.atlassian.net/wiki/spaces/SPAC/pages/1327890499/Posizione+Debitoria+Massiva>Confluence page</a>
 * */
@WorkflowInterface
public interface DebtPositionIngestionFlowWF extends BaseIngestionFlowFileWF {
  @WorkflowMethod
  @Override
  void ingest(Long ingestionFlowFileId);
}
