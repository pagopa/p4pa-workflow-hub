package it.gov.pagopa.pu.workflow.wf.debtposition.sync.wf_sync_aca;

import io.temporal.workflow.WorkflowInterface;
import io.temporal.workflow.WorkflowMethod;
import it.gov.pagopa.payhub.activities.dto.debtposition.syncwfconfig.GenericWfExecutionConfig;
import it.gov.pagopa.pu.debtposition.dto.generated.DebtPositionDTO;
import it.gov.pagopa.pu.debtposition.dto.generated.SyncStatusUpdateRequestDTO;
import it.gov.pagopa.pu.workflow.dto.PaymentEventRequestDTO;

/**
 * Workflow to synchronize a SYNC ACA DebtPosition
 * @see <a href=https://pagopa.atlassian.net/wiki/spaces/SPAC/pages/1485308613/Sincronizzazione+Posizione+Debitoria#3.2.1.4.-Sincrona-%2B-ACA>Confluence page</a>
 */
@WorkflowInterface
public interface SynchronizeSyncAcaWF {
  @WorkflowMethod
  SyncStatusUpdateRequestDTO synchronizeDPSyncAca(DebtPositionDTO debtPosition, PaymentEventRequestDTO paymentEventRequest, GenericWfExecutionConfig wfExecutionConfig);
}
