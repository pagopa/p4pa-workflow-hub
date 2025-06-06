package it.gov.pagopa.pu.workflow.wf.ingestionflow.debtpositiontype.wfingestion;

import io.temporal.workflow.WorkflowInterface;
import io.temporal.workflow.WorkflowMethod;
import it.gov.pagopa.pu.workflow.wf.ingestionflow.BaseIngestionFlowFileWF;

/**
 * Workflow to ingest DebtPositionType file
 * * @see <a href=https://pagopa.atlassian.net/wiki/spaces/SPAC/pages/1287585835/Posizione+Debitoria>Confluence page</a>
 */
@WorkflowInterface
public interface DebtPositionTypeIngestionWF extends BaseIngestionFlowFileWF {
  @Override
  @WorkflowMethod
  void ingest(Long ingestionFlowFileId);
}
