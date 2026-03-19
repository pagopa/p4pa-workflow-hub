package it.gov.pagopa.pu.workflow.wf.ingestionflow.notice.deletemassivenoticesfile;

import io.temporal.workflow.WorkflowInterface;
import io.temporal.workflow.WorkflowMethod;

/**
 * Link to Confluence page for DeleteMassiveNoticesFileWF
 * * @see <a href=https://pagopa.atlassian.net/wiki/spaces/SPAC/pages/2780987489/Cancellazione+massiva+degli+avvisi>Confluence page</a>
 */
@WorkflowInterface
public interface DeleteMassiveNoticesFileWF {
  @WorkflowMethod
  void deleteMassiveNoticesFile(Long ingestionFlowFileId);
}
