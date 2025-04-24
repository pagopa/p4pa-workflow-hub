package it.gov.pagopa.pu.workflow.wf.debtposition.sync.wf_finalize_massive;

import io.temporal.spring.boot.WorkflowImpl;
import it.gov.pagopa.payhub.activities.dto.debtposition.syncwfconfig.GenericWfExecutionConfig;
import it.gov.pagopa.pu.debtposition.dto.generated.DebtPositionDTO;
import it.gov.pagopa.pu.debtposition.dto.generated.InstallmentDTO;
import it.gov.pagopa.pu.debtposition.dto.generated.IupdSyncStatusUpdateDTO;
import it.gov.pagopa.pu.workflow.dto.PaymentEventRequestDTO;
import it.gov.pagopa.pu.workflow.wf.debtposition.sync.BaseDPSynchronizeWf;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@WorkflowImpl(taskQueues = FinalizeMassiveSyncWFImpl.TASK_QUEUE_FINALIZE_MASSIVE_DP_WF)
public class FinalizeMassiveSyncWFImpl extends BaseDPSynchronizeWf implements FinalizeMassiveSyncWF {

  public static final String TASK_QUEUE_FINALIZE_MASSIVE_DP_WF = "DebtPositionSynchronize_finalizeMassive_WF";

  @Override
  public void finalizeMassiveSync(DebtPositionDTO debtPosition, PaymentEventRequestDTO paymentEventRequest, GenericWfExecutionConfig wfExecutionConfig) {
    synchronizeDebtPosition(debtPosition, paymentEventRequest, wfExecutionConfig);
  }

  @Override
  protected IupdSyncStatusUpdateDTO synchronizeInstallment(DebtPositionDTO debtPosition, InstallmentDTO installment) {
    // Massive DebtPosition ingestion should not invoke any PagoPA API as finalization WF: the sync operation has been executed massively using specialized massive API
    return buildIupdSyncStatusUpdateDTO(installment);
  }
}
