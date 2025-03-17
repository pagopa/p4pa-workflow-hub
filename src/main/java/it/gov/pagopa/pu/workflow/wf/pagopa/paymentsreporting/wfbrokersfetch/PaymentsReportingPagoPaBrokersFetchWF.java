package it.gov.pagopa.pu.workflow.wf.pagopa.paymentsreporting.wfbrokersfetch;

import io.temporal.workflow.WorkflowInterface;
import io.temporal.workflow.WorkflowMethod;

/**
 * Workflow to start PaymentsReporting files retrieve from PagoPA by Broker
 * @see <a href=https://pagopa.atlassian.net/wiki/spaces/SPAC/pages/1339031693/Classificazione+incassi#3.3.-Rendicontazione>Confluence page</a>
 */
@WorkflowInterface
public interface PaymentsReportingPagoPaBrokersFetchWF {

  @WorkflowMethod
  void retrieve();
}
