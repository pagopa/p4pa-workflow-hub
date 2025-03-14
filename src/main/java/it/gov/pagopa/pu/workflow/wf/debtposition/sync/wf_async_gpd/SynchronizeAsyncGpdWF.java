package it.gov.pagopa.pu.workflow.wf.debtposition.sync.wf_async_gpd;

import io.temporal.workflow.WorkflowInterface;
import io.temporal.workflow.WorkflowMethod;
import it.gov.pagopa.pu.debtposition.dto.generated.DebtPositionDTO;
import it.gov.pagopa.pu.workflow.dto.generated.PaymentEventType;

/**
 * Workflow to synchronize a GPD DebtPosition
 * @see <a href=https://pagopa.atlassian.net/wiki/spaces/SPAC/pages/1485308613/Sincronizzazione+Posizione+Debitoria#3.2.1.2.-GPD>Confluence page</a>
 */
@WorkflowInterface
public interface SynchronizeAsyncGpdWF {
  @WorkflowMethod
  void synchronizeDPAsyncGpd(DebtPositionDTO debtPosition, PaymentEventType paymentEventType);
}
