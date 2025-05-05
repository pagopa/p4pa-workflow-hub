package it.gov.pagopa.pu.workflow.wf.debtposition.sync.wf_sync_aca;

import io.temporal.spring.boot.WorkflowImpl;
import it.gov.pagopa.payhub.activities.activity.debtposition.aca.SynchronizeInstallmentAcaActivity;
import it.gov.pagopa.payhub.activities.dto.debtposition.syncwfconfig.GenericWfExecutionConfig;
import it.gov.pagopa.pu.debtposition.dto.generated.DebtPositionDTO;
import it.gov.pagopa.pu.debtposition.dto.generated.InstallmentDTO;
import it.gov.pagopa.pu.debtposition.dto.generated.SyncCompleteDTO;
import it.gov.pagopa.pu.workflow.dto.PaymentEventRequestDTO;
import it.gov.pagopa.pu.workflow.wf.debtposition.sync.BaseDPSynchronizeWf;
import it.gov.pagopa.pu.workflow.wf.debtposition.sync.config.SynchronizeDebtPositionWfConfig;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@WorkflowImpl(taskQueues = SynchronizeSyncAcaWFImpl.TASK_QUEUE_SYNCHRONIZE_DP_SYNC_ACA_WF)
public class SynchronizeSyncAcaWFImpl extends BaseDPSynchronizeWf implements SynchronizeSyncAcaWF {

  public static final String TASK_QUEUE_SYNCHRONIZE_DP_SYNC_ACA_WF = "DebtPositionSynchronize_SYNC+ACA_WF";

  private SynchronizeInstallmentAcaActivity synchronizeInstallmentAcaActivity;

  @Override
  protected void buildActivities(SynchronizeDebtPositionWfConfig wfConfig) {
    synchronizeInstallmentAcaActivity = wfConfig.buildSynchronizeInstallmentAcaActivity();
  }

  @Override
  public void synchronizeDPSyncAca(DebtPositionDTO debtPosition, PaymentEventRequestDTO paymentEventRequest, GenericWfExecutionConfig wfExecutionConfig) {
    synchronizeDebtPosition(debtPosition, paymentEventRequest, wfExecutionConfig);
  }

  @Override
  protected SyncCompleteDTO synchronizeInstallment(DebtPositionDTO debtPosition, InstallmentDTO installment) {
    log.info("Synchronizing Installment with IUD: {} for DebtPosition ID: {} on ACA", installment.getIud(), debtPosition.getDebtPositionId());
    synchronizeInstallmentAcaActivity.synchronizeInstallmentAca(debtPosition, installment.getIud());
    return buildIupdSyncStatusUpdateDTO(installment);
  }
}
