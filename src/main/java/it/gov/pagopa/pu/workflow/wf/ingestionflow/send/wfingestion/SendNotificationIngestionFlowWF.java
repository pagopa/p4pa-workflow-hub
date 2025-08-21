package it.gov.pagopa.pu.workflow.wf.ingestionflow.send.wfingestion;

import io.temporal.workflow.WorkflowInterface;
import io.temporal.workflow.WorkflowMethod;
import it.gov.pagopa.pu.workflow.wf.ingestionflow.BaseIngestionFlowFileWF;

/**
 * Workflow to ingest Send Notification file
 * * @see <a href=https://pagopa.atlassian.net/wiki/spaces/SPAC/pages/1839497225/Import+Notifica+SEND>Confluence page</a>
 */
@WorkflowInterface
public interface SendNotificationIngestionFlowWF extends BaseIngestionFlowFileWF {
  @Override
  @WorkflowMethod
  void ingest(Long ingestionFlowFileId);
}
