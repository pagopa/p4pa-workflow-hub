package it.gov.pagopa.pu.workflow.wf.debtposition.ionotification.wf_ionotification;

import io.temporal.workflow.WorkflowInterface;
import io.temporal.workflow.WorkflowMethod;
import it.gov.pagopa.payhub.activities.dto.debtposition.syncwfconfig.GenericWfExecutionConfig;
import it.gov.pagopa.pu.debtposition.dto.generated.DebtPositionDTO;
import it.gov.pagopa.pu.debtposition.dto.generated.SyncCompleteDTO;

import java.util.Map;

/**
 * Workflow to handle IO notification of a new debt position synchronized
 * @see <a href=https://pagopa.atlassian.net/wiki/spaces/SPAC/pages/1485308613/Sincronizzazione+Posizione+Debitoria#3.2.-Pagamenti-pagoPA>Confluence page</a>
 * */

@WorkflowInterface
public interface IoNotificationWF {

  @WorkflowMethod
  void sendIoNotification(DebtPositionDTO debtPositionDTO, Map<String, SyncCompleteDTO> iudSyncCompleteDTOMap, GenericWfExecutionConfig.IONotificationBaseOpsMessages ioMessages);
}
