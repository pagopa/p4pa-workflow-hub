package it.gov.pagopa.pu.workflow.wf.ingestionflow.receipt.pagopa.wfingestion;

import io.temporal.workflow.WorkflowInterface;
import io.temporal.workflow.WorkflowMethod;

/**
 * Workflow interface for the Receipt Pagopa Ingestion
 * */

@WorkflowInterface
public interface ReceiptPagopaIngestionWF {

  /**
   * Workflow method for the Receipt Pagopa Ingestion
   * @param ingestionFlowFileId the id of the ingestion flow file to ingest
   * */
  @WorkflowMethod
  void ingest(Long ingestionFlowFileId);
}
