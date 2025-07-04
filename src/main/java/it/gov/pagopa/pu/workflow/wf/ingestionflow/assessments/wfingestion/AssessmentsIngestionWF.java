package it.gov.pagopa.pu.workflow.wf.ingestionflow.assessments.wfingestion;

import io.temporal.workflow.WorkflowInterface;
import io.temporal.workflow.WorkflowMethod;
import it.gov.pagopa.pu.workflow.wf.ingestionflow.BaseIngestionFlowFileWF;

/**
 * Workflow to ingest Assessments file
 * * @see <a href=https://pagopa.atlassian.net/wiki/spaces/SPAC/pages/1847689218/Import+Accertamenti>Confluence page</a>
 */
@WorkflowInterface
public interface AssessmentsIngestionWF extends BaseIngestionFlowFileWF {
  @Override
  @WorkflowMethod
  void ingest(Long ingestionFlowFileId);
}
