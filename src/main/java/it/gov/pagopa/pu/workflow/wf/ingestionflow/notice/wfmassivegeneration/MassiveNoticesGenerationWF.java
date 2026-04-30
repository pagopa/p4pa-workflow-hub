package it.gov.pagopa.pu.workflow.wf.ingestionflow.notice.wfmassivegeneration;

import io.temporal.workflow.WorkflowInterface;
import io.temporal.workflow.WorkflowMethod;

/**
 * Workflow to generate massive notices file related to an IngestionFlowFile
 * @see <a href=https://pagopa.atlassian.net/wiki/spaces/SPAC/pages/2763620525/Generazione+massiva+avvisi>Confluence page</a>
 */
@WorkflowInterface
public interface MassiveNoticesGenerationWF {
  @WorkflowMethod
  void generate(Long ingestionFlowFileId);
}
