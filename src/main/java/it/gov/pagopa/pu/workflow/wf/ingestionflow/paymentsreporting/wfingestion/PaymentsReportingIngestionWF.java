package it.gov.pagopa.pu.workflow.wf.ingestionflow.paymentsreporting.wfingestion;

import io.temporal.workflow.WorkflowInterface;
import io.temporal.workflow.WorkflowMethod;

/**
 * Workflow interface for the Payments Reporting Ingestion Workflow
 * */

@WorkflowInterface
public interface PaymentsReportingIngestionWF {

  /**
   * Workflow method for the Payments Reporting Ingestion Workflow
   * @param ingestionFlowFileId the id of the ingestion flow file to ingest
   * */
  @WorkflowMethod
  void ingest(Long ingestionFlowFileId);
}
