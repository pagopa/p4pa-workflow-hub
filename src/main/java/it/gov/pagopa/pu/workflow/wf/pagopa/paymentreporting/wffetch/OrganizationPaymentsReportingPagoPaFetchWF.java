package it.gov.pagopa.pu.workflow.wf.pagopa.paymentreporting.wffetch;

import io.temporal.workflow.WorkflowInterface;
import io.temporal.workflow.WorkflowMethod;

/**
 * Workflow interface for the Pago PA Payments Reporting Workflow
 *
 * */
@WorkflowInterface
public interface OrganizationPaymentsReportingPagoPaFetchWF {

  /**
   * Workflow method for the Pago PA Payments Reporting Workflow
   * @param organizationId the ID of the organization
   * */
  @WorkflowMethod
  void retrieve(Long organizationId);
}
