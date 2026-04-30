package it.gov.pagopa.pu.workflow.wf.debtposition.sync.wf_finalize_massive;

import io.temporal.workflow.WorkflowInterface;
import io.temporal.workflow.WorkflowMethod;
import it.gov.pagopa.payhub.activities.dto.debtposition.syncwfconfig.GenericWfExecutionConfig;
import it.gov.pagopa.pu.debtposition.dto.generated.DebtPositionDTO;
import it.gov.pagopa.pu.debtposition.dto.generated.SyncStatusUpdateRequestDTO;
import it.gov.pagopa.pu.workflow.dto.PaymentEventRequestDTO;

/**
 * Workflow to finalize a massive DebtPosition synchronized through specialized API to support massive operations
 * @see <a href=https://pagopa.atlassian.net/wiki/spaces/SPAC/pages/1485308613/Sincronizzazione+Posizione+Debitoria#3.2.2.-Workflow-se-scenario-massivo-e-tecnologia-abilitata-al-caricamento-massivo>Confluence page</a>
 */
@WorkflowInterface
public interface FinalizeMassiveSyncWF {
  @WorkflowMethod
  SyncStatusUpdateRequestDTO finalizeMassiveSync(DebtPositionDTO debtPosition, PaymentEventRequestDTO paymentEventRequest, GenericWfExecutionConfig wfExecutionConfig);
}
