package it.gov.pagopa.pu.workflow.wf.debtposition.sync.wf_sync;

import io.temporal.spring.boot.WorkflowImpl;
import it.gov.pagopa.pu.debtposition.dto.generated.DebtPositionDTO;
import it.gov.pagopa.pu.debtposition.dto.generated.InstallmentDTO;
import it.gov.pagopa.pu.debtposition.dto.generated.IupdSyncStatusUpdateDTO;
import it.gov.pagopa.pu.workflow.event.payments.enums.PaymentEventType;
import it.gov.pagopa.pu.workflow.wf.debtposition.sync.BaseDPSynchronizeWf;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@WorkflowImpl(taskQueues = SynchronizeSyncWFImpl.TASK_QUEUE_SYNCHRONIZE_DP_SYNC_WF)
public class SynchronizeSyncWFImpl extends BaseDPSynchronizeWf implements SynchronizeSyncWF {

  public static final String TASK_QUEUE_SYNCHRONIZE_DP_SYNC_WF = "SynchronizeDP_SYNC_WF";

  @Override
  public void synchronizeDPSync(DebtPositionDTO debtPosition, PaymentEventType paymentEventType) {
    synchronizeDebtPosition(debtPosition, paymentEventType);
  }

  @Override
  protected IupdSyncStatusUpdateDTO synchronizeInstallment(DebtPositionDTO debtPosition, InstallmentDTO installment) {
    // SYNC DebtPosition should not invoke any PagoPA API
    return buildIupdSyncStatusUpdateDTO(installment);
  }
}
