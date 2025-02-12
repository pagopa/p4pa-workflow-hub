package it.gov.pagopa.pu.workflow.wf.debtposition.sync.wf_sync_gpdpreload;

import io.temporal.spring.boot.WorkflowImpl;
import it.gov.pagopa.payhub.activities.activity.debtposition.gpdpreload.SynchronizeInstallmentGpdPreLoadActivity;
import it.gov.pagopa.pu.debtposition.dto.generated.DebtPositionDTO;
import it.gov.pagopa.pu.debtposition.dto.generated.InstallmentDTO;
import it.gov.pagopa.pu.debtposition.dto.generated.IupdSyncStatusUpdateDTO;
import it.gov.pagopa.pu.workflow.dto.generated.PaymentEventType;
import it.gov.pagopa.pu.workflow.wf.debtposition.sync.BaseDPSynchronizeWf;
import it.gov.pagopa.pu.workflow.wf.debtposition.sync.config.SynchronizeDebtPositionWfConfig;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@WorkflowImpl(taskQueues = SynchronizeSyncGpdPreLoadWFImpl.TASK_QUEUE_SYNCHRONIZE_DP_SYNC_GPDPRELOAD_WF)
public class SynchronizeSyncGpdPreLoadWFImpl extends BaseDPSynchronizeWf implements SynchronizeSyncGpdPreLoadWF {

  public static final String TASK_QUEUE_SYNCHRONIZE_DP_SYNC_GPDPRELOAD_WF = "SynchronizeDP_SYNC+GPDPRELOAD_WF";

  private SynchronizeInstallmentGpdPreLoadActivity synchronizeInstallmentGpdPreLoadActivity;

  @Override
  protected void buildActivities(SynchronizeDebtPositionWfConfig wfConfig) {
    synchronizeInstallmentGpdPreLoadActivity = wfConfig.buildSynchronizeInstallmentGpdPreLoadActivity();
  }

  @Override
  public void synchronizeDPSyncGpdPreLoad(DebtPositionDTO debtPosition, PaymentEventType paymentEventType) {
    synchronizeDebtPosition(debtPosition, paymentEventType);
  }

  @Override
  protected IupdSyncStatusUpdateDTO synchronizeInstallment(DebtPositionDTO debtPosition, InstallmentDTO installment) {
    log.info("Synchronizing Installment with IUD: {} for DebtPosition ID: {} on GPD PreLoad", installment.getIud(), debtPosition.getDebtPositionId());
    synchronizeInstallmentGpdPreLoadActivity.synchronizeInstallmentGpdPreLoad(debtPosition, installment.getIud());
    return buildIupdSyncStatusUpdateDTO(installment);
  }
}
