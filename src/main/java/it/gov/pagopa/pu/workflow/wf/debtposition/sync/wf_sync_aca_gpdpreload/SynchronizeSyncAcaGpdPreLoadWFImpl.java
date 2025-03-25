package it.gov.pagopa.pu.workflow.wf.debtposition.sync.wf_sync_aca_gpdpreload;

import io.temporal.spring.boot.WorkflowImpl;
import it.gov.pagopa.payhub.activities.activity.debtposition.aca.SynchronizeInstallmentAcaActivity;
import it.gov.pagopa.payhub.activities.activity.debtposition.gpdpreload.SynchronizeInstallmentGpdPreLoadActivity;
import it.gov.pagopa.payhub.activities.dto.debtposition.syncwfconfig.GenericWfExecutionConfig;
import it.gov.pagopa.pu.debtposition.dto.generated.DebtPositionDTO;
import it.gov.pagopa.pu.debtposition.dto.generated.InstallmentDTO;
import it.gov.pagopa.pu.debtposition.dto.generated.IupdSyncStatusUpdateDTO;
import it.gov.pagopa.pu.workflow.dto.PaymentEventRequestDTO;
import it.gov.pagopa.pu.workflow.wf.debtposition.sync.BaseDPSynchronizeWf;
import it.gov.pagopa.pu.workflow.wf.debtposition.sync.config.SynchronizeDebtPositionWfConfig;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@WorkflowImpl(taskQueues = SynchronizeSyncAcaGpdPreLoadWFImpl.TASK_QUEUE_SYNCHRONIZE_DP_SYNC_ACA_GPDPRELOAD_WF)
public class SynchronizeSyncAcaGpdPreLoadWFImpl extends BaseDPSynchronizeWf implements SynchronizeSyncAcaGpdPreLoadWF {

  public static final String TASK_QUEUE_SYNCHRONIZE_DP_SYNC_ACA_GPDPRELOAD_WF = "DebtPositionSynchronize_SYNC+ACA+GPDPRELOAD_WF";

  private SynchronizeInstallmentAcaActivity synchronizeInstallmentAcaActivity;
  private SynchronizeInstallmentGpdPreLoadActivity synchronizeInstallmentGpdPreLoadActivity;

  @Override
  protected void buildActivities(SynchronizeDebtPositionWfConfig wfConfig) {
    synchronizeInstallmentAcaActivity = wfConfig.buildSynchronizeInstallmentAcaActivity();
    synchronizeInstallmentGpdPreLoadActivity = wfConfig.buildSynchronizeInstallmentGpdPreLoadActivity();
  }

  @Override
  public void synchronizeDPSyncAcaGpdPreLoad(DebtPositionDTO debtPosition, PaymentEventRequestDTO paymentEventRequest, GenericWfExecutionConfig wfExecutionConfig) {
    synchronizeDebtPosition(debtPosition, paymentEventRequest, wfExecutionConfig);
  }

  @Override
  protected IupdSyncStatusUpdateDTO synchronizeInstallment(DebtPositionDTO debtPosition, InstallmentDTO installment) {
    log.info("Synchronizing Installment with IUD: {} for DebtPosition ID: {} on ACA", installment.getIud(), debtPosition.getDebtPositionId());
    try{
      synchronizeInstallmentAcaActivity.synchronizeInstallmentAca(debtPosition, installment.getIud());
    } finally {
      synchronizeInstallmentGpdPreLoadActivity.synchronizeInstallmentGpdPreLoad(debtPosition, installment.getIud());
    }
    return buildIupdSyncStatusUpdateDTO(installment);
  }
}
