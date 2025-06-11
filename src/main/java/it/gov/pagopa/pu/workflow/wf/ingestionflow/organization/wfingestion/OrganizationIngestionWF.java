package it.gov.pagopa.pu.workflow.wf.ingestionflow.organization.wfingestion;

import io.temporal.workflow.WorkflowInterface;
import io.temporal.workflow.WorkflowMethod;
import it.gov.pagopa.pu.workflow.wf.ingestionflow.BaseIngestionFlowFileWF;

/**
 * Workflow to ingest Organization file
 * * @see <a href=https://pagopa.atlassian.net/wiki/spaces/SPAC/pages/1797521599/Import+Enti>Confluence page</a>
 */
@WorkflowInterface
public interface OrganizationIngestionWF extends BaseIngestionFlowFileWF {
  @Override
  @WorkflowMethod
  void ingest(Long ingestionFlowFileId);
}
