package it.gov.pagopa.pu.workflow.wf.debtposition.custom.fine.wfsynchronizefine;

import io.temporal.spring.boot.WorkflowImpl;
import it.gov.pagopa.payhub.activities.activity.debtposition.custom.fine.DebtPositionSynchronizeFineActivity;
import it.gov.pagopa.payhub.activities.dto.debtposition.HandleFineDebtPositionResult;
import it.gov.pagopa.payhub.activities.dto.debtposition.syncwfconfig.FineWfExecutionConfig;
import it.gov.pagopa.payhub.activities.dto.debtposition.syncwfconfig.GenericWfExecutionConfig;
import it.gov.pagopa.pu.debtposition.dto.generated.DebtPositionDTO;
import it.gov.pagopa.pu.debtposition.dto.generated.DebtPositionStatus;
import it.gov.pagopa.pu.workflow.config.temporal.TemporalWFImplementationCustomizer;
import it.gov.pagopa.pu.workflow.dto.PaymentEventRequestDTO;
import it.gov.pagopa.pu.workflow.wf.debtposition.custom.activity.InvokeSyncDebtPositionActivity;
import it.gov.pagopa.pu.workflow.wf.debtposition.custom.fine.activity.CancelReductionExpirationScheduleActivity;
import it.gov.pagopa.pu.workflow.wf.debtposition.custom.fine.activity.ScheduleReductionExpirationActivity;
import it.gov.pagopa.pu.workflow.wf.debtposition.custom.fine.config.DebtPositionFineWfConfig;
import it.gov.pagopa.pu.workflow.wf.debtposition.custom.fine.mapper.FineWfExecutionConfigMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

@Slf4j
@WorkflowImpl(taskQueues = SynchronizeFineWFImpl.TASK_QUEUE_SYNC_FINE)
public class SynchronizeFineWFImpl implements SynchronizeFineWF, ApplicationContextAware {

  public static final String TASK_QUEUE_SYNC_FINE = "DebtPositionSynchronize_fine_WF";

  private DebtPositionSynchronizeFineActivity debtPositionSynchronizeFineActivity;
  private InvokeSyncDebtPositionActivity invokeSyncDebtPositionActivity;
  private CancelReductionExpirationScheduleActivity cancelReductionExpirationScheduleActivity;
  private ScheduleReductionExpirationActivity scheduleReductionExpirationActivity;

  /**
   * Temporal workflow will not allow to use injection in order to avoid <a href="https://docs.temporal.io/workflows#non-deterministic-change">non-deterministic changes</a> due to dynamic reconfiguration.<BR />
   * Anyway it allows to override ActivityOptions, but actually it's not supporting the override based on the particular workflow.<BR />
   * In {@link TemporalWFImplementationCustomizer} we are already setting defaults to all workflows.<BR />
   * Use this as an example to override based on the particular workflow.
   */
  @Override
  public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
    DebtPositionFineWfConfig wfConfig = applicationContext.getBean(DebtPositionFineWfConfig.class);

    debtPositionSynchronizeFineActivity = wfConfig.buildDebtPositionSynchronizeFineActivityStub();
    invokeSyncDebtPositionActivity = wfConfig.buildInvokeSyncDebtPositionActivityStub();
    cancelReductionExpirationScheduleActivity = wfConfig.buildCancelReductionExpirationScheduleActivityStub();
    scheduleReductionExpirationActivity = wfConfig.buildScheduleReductionExpirationActivityStub();
  }

  @Override
  public void synchronizeFineDP(DebtPositionDTO debtPositionDTO, PaymentEventRequestDTO paymentEventRequest, Boolean massive, FineWfExecutionConfig wfExecutionConfig) {
    HandleFineDebtPositionResult result = debtPositionSynchronizeFineActivity.handleFineDebtPosition(debtPositionDTO, massive, wfExecutionConfig);

    DebtPositionDTO debtPosition = result.getDebtPositionDTO();

    log.info("Mapping FineWfExecutionConfig to GenericWfExecutionConfig");
    GenericWfExecutionConfig genericWfExecutionConfig;
    if(result.isNotified()) {
      genericWfExecutionConfig = FineWfExecutionConfigMapper.mapNotifiedInstallment(wfExecutionConfig, debtPosition);
    } else {
      genericWfExecutionConfig = new GenericWfExecutionConfig();
    }

    log.info("Synchronize fine {} with Nodo", debtPosition.getDebtPositionId());
    invokeSyncDebtPositionActivity.synchronizeDPSync(debtPosition, paymentEventRequest, massive, genericWfExecutionConfig);

    if (DebtPositionStatus.PAID.equals(debtPosition.getStatus()) || DebtPositionStatus.PARTIALLY_PAID.equals(debtPosition.getStatus())) {
      log.info("DebtPosition with id {} is [{}], cancelling reduction expiration workflow", debtPosition.getDebtPositionId(), debtPosition.getStatus());
      cancelReductionExpirationScheduleActivity(debtPosition);

    } else if (result.isNotified()) {
      log.info("DebtPosition with id {} is NOTIFIED, cancelling and rescheduling reduction expiration workflow", debtPosition.getDebtPositionId());
      cancelReductionExpirationScheduleActivity(debtPosition);
      scheduleReductionExpirationActivity.scheduleExpireFineReduction(debtPosition.getDebtPositionId(), wfExecutionConfig, result.getReductionEndDate());
    }

  }

  private void cancelReductionExpirationScheduleActivity(DebtPositionDTO debtPosition) {
    cancelReductionExpirationScheduleActivity.cancelReductionPeriodExpirationScheduling(debtPosition.getDebtPositionId());
  }
}
