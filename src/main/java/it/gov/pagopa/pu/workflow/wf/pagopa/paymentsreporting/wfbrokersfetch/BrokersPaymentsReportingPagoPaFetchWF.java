package it.gov.pagopa.pu.workflow.wf.pagopa.paymentsreporting.wfbrokersfetch;

import io.temporal.workflow.WorkflowInterface;
import io.temporal.workflow.WorkflowMethod;

/**
 * Workflow interface for the Brokered Organizations Retrieve Workflow
 *
 * */
@WorkflowInterface
public interface BrokersPaymentsReportingPagoPaFetchWF {

  /**
   * Workflow method for the Brokered Organizations Retrieve Workflow
   * */
  @WorkflowMethod
  void retrieve();
}
