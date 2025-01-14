package it.gov.pagopa.pu.workflow.wf.ingestionflow.treasury.opi.wfingestion;

import io.temporal.workflow.WorkflowInterface;
import io.temporal.workflow.WorkflowMethod;

/**
 * Workflow interface for the Treasury OPI Ingestion
 * */

@WorkflowInterface
public interface TreasuryOpiIngestionWF {

  /**
   * Workflow method for the Treasury OPI Ingestion
   * @param ingestionFlowFileId the id of the ingestion flow file to ingest
   * */
  @WorkflowMethod
  void ingest(Long ingestionFlowFileId);
}
