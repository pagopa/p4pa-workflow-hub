package it.gov.pagopa.pu.workflow.wf.debtposition.sync.wf_async_gpd;

import io.temporal.spring.boot.WorkflowImpl;
import it.gov.pagopa.payhub.activities.activity.debtposition.synchronize.gpd.SynchronizeInstallmentGpdActivity;
import it.gov.pagopa.payhub.activities.dto.debtposition.syncwfconfig.GenericWfExecutionConfig;
import it.gov.pagopa.pu.debtposition.dto.generated.DebtPositionDTO;
import it.gov.pagopa.pu.debtposition.dto.generated.InstallmentDTO;
import it.gov.pagopa.pu.debtposition.dto.generated.SyncCompleteDTO;
import it.gov.pagopa.pu.workflow.dto.PaymentEventRequestDTO;
import it.gov.pagopa.pu.workflow.wf.debtposition.sync.BaseDPSynchronizeWf;
import it.gov.pagopa.pu.workflow.wf.debtposition.sync.config.SynchronizeDebtPositionWfConfig;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@WorkflowImpl(taskQueues = SynchronizeAsyncGpdWFImpl.TASK_QUEUE_SYNCHRONIZE_DP_ASYNC_GPD_WF)
public class SynchronizeAsyncGpdWFImpl extends BaseDPSynchronizeWf implements SynchronizeAsyncGpdWF {

  public static final String TASK_QUEUE_SYNCHRONIZE_DP_ASYNC_GPD_WF = "DebtPositionSynchronize_ASYNC_GPD_WF";

  private SynchronizeInstallmentGpdActivity synchronizeInstallmentGpdActivity;

  @Override
  protected void buildActivities(SynchronizeDebtPositionWfConfig wfConfig) {
    synchronizeInstallmentGpdActivity = wfConfig.buildSynchronizeInstallmentGpdActivity();
  }

  @Override
  public void synchronizeDPAsyncGpd(DebtPositionDTO debtPosition, PaymentEventRequestDTO paymentEventRequest, GenericWfExecutionConfig wfExecutionConfig) {
    synchronizeDebtPosition(debtPosition, paymentEventRequest, wfExecutionConfig);
  }

  @Override
  protected SyncCompleteDTO synchronizeInstallment(DebtPositionDTO debtPosition, InstallmentDTO installment) {
    log.info("Synchronizing Installment with IUD: {} for DebtPosition ID: {} on GPD", installment.getIud(), debtPosition.getDebtPositionId());
    synchronizeInstallmentGpdActivity.synchronizeInstallmentGpd(debtPosition, installment.getIud());
    return buildIupdSyncStatusUpdateDTO(installment);
  }
}
