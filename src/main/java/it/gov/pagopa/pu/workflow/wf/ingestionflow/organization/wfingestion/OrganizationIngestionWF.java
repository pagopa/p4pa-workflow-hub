package it.gov.pagopa.pu.workflow.wf.ingestionflow.organization.wfingestion;

import io.temporal.workflow.WorkflowInterface;
import io.temporal.workflow.WorkflowMethod;

/**
 * Workflow to ingest Organization file
 */
@WorkflowInterface
public interface OrganizationIngestionWF {
  @WorkflowMethod
  void ingest(Long ingestionFlowFileId);
}
