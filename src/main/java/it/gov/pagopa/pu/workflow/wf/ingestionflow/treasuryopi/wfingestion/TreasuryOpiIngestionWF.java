package it.gov.pagopa.pu.workflow.wf.ingestionflow.treasuryopi.wfingestion;

import io.temporal.workflow.WorkflowInterface;
import io.temporal.workflow.WorkflowMethod;

/**
 * Workflow interface for the Treasury OPI Ingestion
 * */

@WorkflowInterface
public interface TreasuryOpiIngestionWF {

  /**
   * Workflow method for the Treasury OPI Ingestion
   * @param ingestionFlowId the id of the ingestion flow to ingest
   * */
  @WorkflowMethod
  void ingest(Long ingestionFlowId);
}
