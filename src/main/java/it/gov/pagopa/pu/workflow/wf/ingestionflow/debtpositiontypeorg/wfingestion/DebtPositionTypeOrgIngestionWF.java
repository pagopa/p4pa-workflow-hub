package it.gov.pagopa.pu.workflow.wf.ingestionflow.debtpositiontypeorg.wfingestion;

import io.temporal.workflow.WorkflowInterface;
import io.temporal.workflow.WorkflowMethod;
import it.gov.pagopa.pu.workflow.wf.ingestionflow.BaseIngestionFlowFileWF;

/**
 * Workflow to ingest DebtPositionTypeOrg file
 * @see <a href=https://pagopa.atlassian.net/wiki/spaces/SPAC/pages/1820852308/Import+Tipologia+Posizione+Debitoria+Ente>Confluence page</a>
 */
@WorkflowInterface
public interface DebtPositionTypeOrgIngestionWF extends BaseIngestionFlowFileWF {
  @Override
  @WorkflowMethod
  void ingest(Long ingestionFlowFileId);
}
