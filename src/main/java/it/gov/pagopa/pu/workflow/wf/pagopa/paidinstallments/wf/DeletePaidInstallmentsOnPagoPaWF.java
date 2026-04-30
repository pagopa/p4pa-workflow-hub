package it.gov.pagopa.pu.workflow.wf.pagopa.paidinstallments.wf;

import io.temporal.workflow.WorkflowInterface;
import io.temporal.workflow.WorkflowMethod;
import it.gov.pagopa.pu.debtposition.dto.generated.DebtPositionDTO;

/**
 * Workflow to start DeletePaidInstallmentsOnPagoPa process from PagoPA by Broker
 * @see <a href=https://pagopa.atlassian.net/wiki/spaces/SPAC/pages/1830125645/Cancellazione+Installment+pagati+sul+nodo>Confluence page</a>
 */
@WorkflowInterface
public interface DeletePaidInstallmentsOnPagoPaWF {

  @WorkflowMethod
  void deletePaidInstallments(DebtPositionDTO debtPositionDTO, Long receiptId);

}
