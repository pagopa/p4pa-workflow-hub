package it.gov.pagopa.pu.workflow.wf.ingestionflow.orgsilservice.wfingestion;

import io.temporal.workflow.WorkflowInterface;
import io.temporal.workflow.WorkflowMethod;
import it.gov.pagopa.pu.workflow.wf.ingestionflow.BaseIngestionFlowFileWF;

/**
 * Workflow to ingest Organization file
 * * @see <a href=https://pagopa.atlassian.net/wiki/spaces/SPAC/pages/1861026265/Import+Servizi+esposti+dai+SIL>Confluence page</a>
 */
@WorkflowInterface
public interface OrgSilServiceIngestionWF extends BaseIngestionFlowFileWF {
  @Override
  @WorkflowMethod
  void ingest(Long ingestionFlowFileId);
}
