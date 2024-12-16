package it.gov.pagopa.pu.workflow;

import io.temporal.workflow.WorkflowInterface;
import io.temporal.workflow.WorkflowMethod;

/**
 * Workflow interface for the Payments Reporting Ingestion Workflow
 * */

@WorkflowInterface
public interface PaymentsReportingIngestionWF {

  /**
   * Workflow method for the Payments Reporting Ingestion Workflow
   * @param fileId
   * */
  @WorkflowMethod
  public void ingest(Long fileId);
}
