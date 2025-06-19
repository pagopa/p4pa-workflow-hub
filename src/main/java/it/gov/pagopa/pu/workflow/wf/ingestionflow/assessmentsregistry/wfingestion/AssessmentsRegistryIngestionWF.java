package it.gov.pagopa.pu.workflow.wf.ingestionflow.assessmentsregistry.wfingestion;

import io.temporal.workflow.WorkflowInterface;
import io.temporal.workflow.WorkflowMethod;
import it.gov.pagopa.pu.workflow.wf.ingestionflow.BaseIngestionFlowFileWF;

/**
 * Workflow to ingest AssessmentsRegistry file
 */
@WorkflowInterface
public interface AssessmentsRegistryIngestionWF extends BaseIngestionFlowFileWF {
  @Override
  @WorkflowMethod
  void ingest(Long ingestionFlowFileId);
}
