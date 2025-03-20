package it.gov.pagopa.pu.workflow.wf.debtposition.sync.wf_finalize_massive;

import io.temporal.spring.boot.WorkflowImpl;
import it.gov.pagopa.payhub.activities.dto.debtposition.syncwfconfig.GenericWfExecutionConfig;
import it.gov.pagopa.pu.debtposition.dto.generated.DebtPositionDTO;
import it.gov.pagopa.pu.debtposition.dto.generated.InstallmentDTO;
import it.gov.pagopa.pu.debtposition.dto.generated.IupdSyncStatusUpdateDTO;
import it.gov.pagopa.pu.workflow.dto.generated.PaymentEventType;
import it.gov.pagopa.pu.workflow.wf.debtposition.sync.BaseDPSynchronizeWf;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@WorkflowImpl(taskQueues = FinalizeMassiveSyncWFImpl.TASK_QUEUE_FINALIZE_MASSIVE_DP_WF)
public class FinalizeMassiveSyncWFImpl extends BaseDPSynchronizeWf implements FinalizeMassiveSyncWF {

  public static final String TASK_QUEUE_FINALIZE_MASSIVE_DP_WF = "DebtPositionSynchronize_finalizeMassive_WF";

  @Override
  public void finalizeMassiveSync(DebtPositionDTO debtPosition, PaymentEventType paymentEventType, GenericWfExecutionConfig wfExecutionConfig) {
    synchronizeDebtPosition(debtPosition, paymentEventType, wfExecutionConfig);
  }

  @Override
  protected IupdSyncStatusUpdateDTO synchronizeInstallment(DebtPositionDTO debtPosition, InstallmentDTO installment) {
    // SYNC DebtPosition should not invoke any PagoPA API
    return buildIupdSyncStatusUpdateDTO(installment);
  }
}
