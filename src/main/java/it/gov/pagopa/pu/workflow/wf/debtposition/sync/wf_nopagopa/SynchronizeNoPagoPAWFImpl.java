package it.gov.pagopa.pu.workflow.wf.debtposition.sync.wf_nopagopa;

import io.temporal.spring.boot.WorkflowImpl;
import it.gov.pagopa.payhub.activities.dto.debtposition.syncwfconfig.GenericWfExecutionConfig;
import it.gov.pagopa.pu.debtposition.dto.generated.DebtPositionDTO;
import it.gov.pagopa.pu.debtposition.dto.generated.InstallmentDTO;
import it.gov.pagopa.pu.debtposition.dto.generated.SyncCompleteDTO;
import it.gov.pagopa.pu.debtposition.dto.generated.SyncStatusUpdateRequestDTO;
import it.gov.pagopa.pu.workflow.dto.PaymentEventRequestDTO;
import it.gov.pagopa.pu.workflow.utilities.TaskQueueConstants;
import it.gov.pagopa.pu.workflow.wf.debtposition.sync.BaseDPSynchronizeWf;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

@Slf4j
@WorkflowImpl(taskQueues = TaskQueueConstants.TASK_QUEUE_DP_RESERVED_SYNC)
public class SynchronizeNoPagoPAWFImpl extends BaseDPSynchronizeWf implements SynchronizeNoPagoPAWF {

  @Override
  public SyncStatusUpdateRequestDTO synchronizeDPNoPagoPA(DebtPositionDTO debtPosition, PaymentEventRequestDTO paymentEventRequest, GenericWfExecutionConfig wfExecutionConfig) {
    return synchronizeDebtPosition(debtPosition, paymentEventRequest, wfExecutionConfig);
  }

  @Override
  protected SyncCompleteDTO synchronizeInstallment(DebtPositionDTO debtPosition, InstallmentDTO installment) {
    // SYNC DebtPosition should not invoke any PagoPA API
    return buildIupdSyncStatusUpdateDTO(installment);
  }

  @Override
  protected void startIONotificationWF(DebtPositionDTO requestedDebtPosition, Map<String, SyncCompleteDTO> iupdSyncStatusUpdateDTOMap, GenericWfExecutionConfig.IONotificationBaseOpsMessages ioMessages) {
    // Do Nothing
  }
}
