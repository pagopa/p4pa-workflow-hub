package it.gov.pagopa.pu.workflow.wf.debtposition.sync.wf_sync_gpdpreload;

import io.temporal.spring.boot.WorkflowImpl;
import it.gov.pagopa.payhub.activities.activity.debtposition.synchronize.gpdpreload.SynchronizeInstallmentGpdPreLoadActivity;
import it.gov.pagopa.payhub.activities.dto.debtposition.syncwfconfig.GenericWfExecutionConfig;
import it.gov.pagopa.pu.debtposition.dto.generated.DebtPositionDTO;
import it.gov.pagopa.pu.debtposition.dto.generated.InstallmentDTO;
import it.gov.pagopa.pu.debtposition.dto.generated.SyncCompleteDTO;
import it.gov.pagopa.pu.workflow.dto.PaymentEventRequestDTO;
import it.gov.pagopa.pu.workflow.wf.debtposition.sync.BaseDPSynchronizeWf;
import it.gov.pagopa.pu.workflow.wf.debtposition.sync.config.SynchronizeDebtPositionWfConfig;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@WorkflowImpl(taskQueues = SynchronizeSyncGpdPreLoadWFImpl.TASK_QUEUE_SYNCHRONIZE_DP_SYNC_GPDPRELOAD_WF)
public class SynchronizeSyncGpdPreLoadWFImpl extends BaseDPSynchronizeWf implements SynchronizeSyncGpdPreLoadWF {

  public static final String TASK_QUEUE_SYNCHRONIZE_DP_SYNC_GPDPRELOAD_WF = "DebtPositionSynchronize_SYNC+GPDPRELOAD_WF";

  private SynchronizeInstallmentGpdPreLoadActivity synchronizeInstallmentGpdPreLoadActivity;

  @Override
  protected void buildActivities(SynchronizeDebtPositionWfConfig wfConfig) {
    synchronizeInstallmentGpdPreLoadActivity = wfConfig.buildSynchronizeInstallmentGpdPreLoadActivity();
  }

  @Override
  public void synchronizeDPSyncGpdPreLoad(DebtPositionDTO debtPosition, PaymentEventRequestDTO paymentEventRequest, GenericWfExecutionConfig wfExecutionConfig) {
    synchronizeDebtPosition(debtPosition, paymentEventRequest, wfExecutionConfig);
  }

  @Override
  protected SyncCompleteDTO synchronizeInstallment(DebtPositionDTO debtPosition, InstallmentDTO installment) {
    log.info("Synchronizing Installment with IUD: {} for DebtPosition ID: {} on GPD PreLoad", installment.getIud(), debtPosition.getDebtPositionId());
    synchronizeInstallmentGpdPreLoadActivity.synchronizeInstallmentGpdPreLoad(debtPosition, installment.getIud());
    return buildIupdSyncStatusUpdateDTO(installment);
  }
}
