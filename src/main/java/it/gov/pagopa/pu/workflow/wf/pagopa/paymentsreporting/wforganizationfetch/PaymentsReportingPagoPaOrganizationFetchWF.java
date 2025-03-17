package it.gov.pagopa.pu.workflow.wf.pagopa.paymentsreporting.wforganizationfetch;

import io.temporal.workflow.WorkflowInterface;
import io.temporal.workflow.WorkflowMethod;

/**
 * Workflow to fetch PaymentsReporting files from PagoPA for a given organizationId
 * @see <a href=https://pagopa.atlassian.net/wiki/spaces/SPAC/pages/1339031693/Classificazione+incassi#3.3.-Rendicontazione>Confluence page</a>
 */
@WorkflowInterface
public interface PaymentsReportingPagoPaOrganizationFetchWF {

  @WorkflowMethod
  void retrieve(Long organizationId);
}
