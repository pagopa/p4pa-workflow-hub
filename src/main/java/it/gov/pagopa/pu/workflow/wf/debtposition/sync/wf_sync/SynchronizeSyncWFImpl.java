package it.gov.pagopa.pu.workflow.wf.debtposition.sync.wf_sync;

import io.temporal.spring.boot.WorkflowImpl;
import it.gov.pagopa.payhub.activities.dto.debtposition.syncwfconfig.GenericWfExecutionConfig;
import it.gov.pagopa.pu.debtposition.dto.generated.DebtPositionDTO;
import it.gov.pagopa.pu.debtposition.dto.generated.InstallmentDTO;
import it.gov.pagopa.pu.debtposition.dto.generated.SyncCompleteDTO;
import it.gov.pagopa.pu.workflow.dto.PaymentEventRequestDTO;
import it.gov.pagopa.pu.workflow.wf.debtposition.sync.BaseDPSynchronizeWf;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@WorkflowImpl(taskQueues = SynchronizeSyncWFImpl.TASK_QUEUE_SYNCHRONIZE_DP_SYNC_WF)
public class SynchronizeSyncWFImpl extends BaseDPSynchronizeWf implements SynchronizeSyncWF {

  public static final String TASK_QUEUE_SYNCHRONIZE_DP_SYNC_WF = "DebtPositionSynchronize_SYNC_WF";

  @Override
  public void synchronizeDPSync(DebtPositionDTO debtPosition, PaymentEventRequestDTO paymentEventRequest, GenericWfExecutionConfig wfExecutionConfig) {
    synchronizeDebtPosition(debtPosition, paymentEventRequest, wfExecutionConfig);
  }

  @Override
  protected SyncCompleteDTO synchronizeInstallment(DebtPositionDTO debtPosition, InstallmentDTO installment) {
    // SYNC DebtPosition should not invoke any PagoPA API
    return buildIupdSyncStatusUpdateDTO(installment);
  }
}
