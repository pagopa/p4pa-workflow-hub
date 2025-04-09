package it.gov.pagopa.pu.workflow.wf.debtposition.custom.fine.wfsynchronizefine;

import io.temporal.spring.boot.WorkflowImpl;
import it.gov.pagopa.payhub.activities.activity.debtposition.custom.fine.DebtPositionSynchronizeFineActivity;
import it.gov.pagopa.payhub.activities.dto.debtposition.HandleFineDebtPositionResult;
import it.gov.pagopa.payhub.activities.dto.debtposition.syncwfconfig.FineWfExecutionConfig;
import it.gov.pagopa.payhub.activities.dto.debtposition.syncwfconfig.GenericWfExecutionConfig;
import it.gov.pagopa.pu.debtposition.dto.generated.DebtPositionDTO;
import it.gov.pagopa.pu.debtposition.dto.generated.DebtPositionStatus;
import it.gov.pagopa.pu.workflow.dto.PaymentEventRequestDTO;
import it.gov.pagopa.pu.workflow.wf.debtposition.custom.activity.CancelReductionExpirationScheduleActivity;
import it.gov.pagopa.pu.workflow.wf.debtposition.custom.activity.InvokeSyncDebtPositionActivity;
import it.gov.pagopa.pu.workflow.wf.debtposition.custom.activity.ScheduleReductionExpirationActivity;
import it.gov.pagopa.pu.workflow.wf.debtposition.custom.fine.config.DebtPositionFineWfConfig;
import it.gov.pagopa.pu.workflow.wf.debtposition.custom.fine.mapper.FineWfExecutionConfigMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import static it.gov.pagopa.pu.workflow.wf.debtposition.custom.fine.DebtPositionFineClientImpl.generateExpireFineReductionWorkflowId;

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
   * In {@link it.gov.pagopa.pu.workflow.config.TemporalWFImplementationCustomizer} we are already setting defaults to all workflows.<BR />
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
  public void synchronizeFine(DebtPositionDTO debtPositionDTO, PaymentEventRequestDTO paymentEventRequest, Boolean massive, FineWfExecutionConfig wfExecutionConfig) {
    HandleFineDebtPositionResult result = debtPositionSynchronizeFineActivity.handleFineDebtPosition(debtPositionDTO, massive, wfExecutionConfig);

    // TODO replace IO placeholders https://pagopa.atlassian.net/browse/P4ADEV-2599
    log.info("Mapped FineWfExecutionConfig: {} to GenericWfExecutionConfig", wfExecutionConfig);
    GenericWfExecutionConfig genericWfExecutionConfig = FineWfExecutionConfigMapper.mapReductionExpired(wfExecutionConfig);

    DebtPositionDTO debtPosition = result.getDebtPositionDTO();

    log.info("Synchronize fine {} with Nodo", debtPosition.getDebtPositionId());
    String workflowId = invokeSyncDebtPositionActivity.synchronizeDPSync(debtPosition, paymentEventRequest, massive, genericWfExecutionConfig);

    if (workflowId != null){
      if (DebtPositionStatus.PAID.equals(debtPosition.getStatus())) {
        log.info("DebtPosition with id {} is PAID, cancelling reduction expiration workflow", debtPosition.getDebtPositionId());
        cancelReductionExpirationScheduleActivity(debtPosition);

      } else if (result.isNotified()) {
        log.info("DebtPosition with id {} is NOTIFIED, cancelling and rescheduling reduction expiration workflow", debtPosition.getDebtPositionId());
        cancelReductionExpirationScheduleActivity(debtPosition);
        scheduleReductionExpirationActivity.expireFineReduction(result.getDebtPositionDTO().getDebtPositionId(), wfExecutionConfig);
      }
    }
  }

  private void cancelReductionExpirationScheduleActivity(DebtPositionDTO debtPosition) {
    String workflowId = generateExpireFineReductionWorkflowId(debtPosition.getDebtPositionId());
    cancelReductionExpirationScheduleActivity.cancelScheduling(workflowId);
  }
}
