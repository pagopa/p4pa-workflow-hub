package it.gov.pagopa.pu.workflow;

import io.temporal.workflow.WorkflowInterface;
import io.temporal.workflow.WorkflowMethod;

@WorkflowInterface
public interface PaymentsReportingIngestionWF {
  @WorkflowMethod
  public void ingest(Long fileId);
}
