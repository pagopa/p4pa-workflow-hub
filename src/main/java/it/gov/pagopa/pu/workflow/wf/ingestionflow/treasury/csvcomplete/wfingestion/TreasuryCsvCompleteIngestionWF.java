package it.gov.pagopa.pu.workflow.wf.ingestionflow.treasury.csvcomplete.wfingestion;

import io.temporal.workflow.WorkflowInterface;
import io.temporal.workflow.WorkflowMethod;
import it.gov.pagopa.pu.workflow.wf.ingestionflow.BaseIngestionFlowFileWF;

/**
 * Workflow to ingest treasury csv complete file
 * * @see <a href=https://pagopa.atlassian.net/wiki/spaces/SPAC/pages/1373896780/ASIS+-+Tesoreria>Confluence page</a>
 */
@WorkflowInterface
public interface TreasuryCsvCompleteIngestionWF extends BaseIngestionFlowFileWF {
  @Override
  @WorkflowMethod
  void ingest(Long ingestionFlowFileId);
}
