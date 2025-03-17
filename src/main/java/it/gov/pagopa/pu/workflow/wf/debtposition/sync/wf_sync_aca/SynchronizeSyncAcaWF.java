package it.gov.pagopa.pu.workflow.wf.debtposition.sync.wf_sync_aca;

import io.temporal.workflow.WorkflowInterface;
import io.temporal.workflow.WorkflowMethod;
import it.gov.pagopa.pu.debtposition.dto.generated.DebtPositionDTO;
import it.gov.pagopa.pu.workflow.dto.generated.PaymentEventType;

/**
 * Workflow to synchronize a SYNC ACA DebtPosition
 * @see <a href=https://pagopa.atlassian.net/wiki/spaces/SPAC/pages/1485308613/Sincronizzazione+Posizione+Debitoria#3.2.1.4.-Sincrona-%2B-ACA>Confluence page</a>
 */
@WorkflowInterface
public interface SynchronizeSyncAcaWF {
  @WorkflowMethod
  void synchronizeDPSyncAca(DebtPositionDTO debtPosition, PaymentEventType paymentEventType);
}
