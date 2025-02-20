package it.gov.pagopa.pu.workflow.wf.ingestionflow.debtposition.wfingestion;

import io.temporal.workflow.WorkflowInterface;
import io.temporal.workflow.WorkflowMethod;

/**
 * Workflow interface for the Debt Position Ingestion Flow Workflow
 * */
@WorkflowInterface
public interface DebtPositionIngestionFlowWF {

  /**
   * Workflow method for the Debt Position Ingestion Flow Workflow
   * @param ingestionFlowFileId the id of the ingestion flow file to ingest
   * */
  @WorkflowMethod
  void ingest(Long ingestionFlowFileId);
}
