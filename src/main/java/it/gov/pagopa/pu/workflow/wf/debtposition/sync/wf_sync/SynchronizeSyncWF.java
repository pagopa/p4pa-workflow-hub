package it.gov.pagopa.pu.workflow.wf.debtposition.sync.wf_sync;

import io.temporal.workflow.WorkflowInterface;
import io.temporal.workflow.WorkflowMethod;
import it.gov.pagopa.payhub.activities.dto.debtposition.syncwfconfig.GenericWfExecutionConfig;
import it.gov.pagopa.pu.debtposition.dto.generated.DebtPositionDTO;
import it.gov.pagopa.pu.workflow.dto.PaymentEventRequestDTO;

/**
 * Workflow to synchronize a SYNC DebtPosition
 * @see <a href=https://pagopa.atlassian.net/wiki/spaces/SPAC/pages/1485308613/Sincronizzazione+Posizione+Debitoria#3.2.1.1.-Sincrona>Confluence page</a>
 */
@WorkflowInterface
public interface SynchronizeSyncWF {
  @WorkflowMethod
  void synchronizeDPSync(DebtPositionDTO debtPosition, PaymentEventRequestDTO paymentEventRequest, GenericWfExecutionConfig wfExecutionConfig);
}
