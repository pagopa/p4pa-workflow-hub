package it.gov.pagopa.pu.workflow.wf.debtposition.sync.wf_nopagopa;

import io.temporal.workflow.WorkflowInterface;
import io.temporal.workflow.WorkflowMethod;
import it.gov.pagopa.payhub.activities.dto.debtposition.syncwfconfig.GenericWfExecutionConfig;
import it.gov.pagopa.pu.debtposition.dto.generated.DebtPositionDTO;
import it.gov.pagopa.pu.debtposition.dto.generated.SyncStatusUpdateRequestDTO;
import it.gov.pagopa.pu.workflow.dto.PaymentEventRequestDTO;

/**
 * Workflow to synchronize a No pagoPA handled DebtPosition
 * @see <a href=https://pagopa.atlassian.net/wiki/spaces/SPAC/pages/1485308613/Sincronizzazione+Posizione+Debitoria#3.1.-Pagamento-non-pagoPA>Confluence page</a>
 */
@WorkflowInterface
public interface SynchronizeNoPagoPAWF {
  @WorkflowMethod
  SyncStatusUpdateRequestDTO synchronizeDPNoPagoPA(DebtPositionDTO debtPosition, PaymentEventRequestDTO paymentEventRequest, GenericWfExecutionConfig wfExecutionConfig);
}
