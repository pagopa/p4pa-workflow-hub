package it.gov.pagopa.pu.workflow.wf.paymentsreporting.pagopa.wffetch;

import io.temporal.workflow.WorkflowInterface;
import io.temporal.workflow.WorkflowMethod;

/**
 * Workflow interface for the Pago PA Payments Reporting Workflow per Organization
 *
 * */
@WorkflowInterface
public interface OrganizationPaymentsReportingPagoPaFetchWF {

  /**
   * Workflow method for the Pago PA Payments Reporting Workflow per Organization
   * @param organizationId the ID of the organization
   * */
  @WorkflowMethod
  void retrieve(Long organizationId);
}
