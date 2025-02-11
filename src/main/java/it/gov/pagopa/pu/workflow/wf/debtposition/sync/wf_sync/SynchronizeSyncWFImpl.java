package it.gov.pagopa.pu.workflow.wf.debtposition.sync.wf_sync;

import io.temporal.spring.boot.WorkflowImpl;
import it.gov.pagopa.payhub.activities.activity.debtposition.ionotification.SendDebtPositionIONotificationActivity;
import it.gov.pagopa.pu.debtposition.dto.generated.DebtPositionDTO;
import it.gov.pagopa.pu.workflow.event.payments.enums.PaymentEventType;
import it.gov.pagopa.pu.workflow.wf.debtposition.sync.activity.PublishPaymentEventActivity;
import it.gov.pagopa.pu.workflow.wf.debtposition.sync.config.SynchronizeDebtPositionWfConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import static it.gov.pagopa.pu.workflow.wf.debtposition.sync.wf_sync.SynchronizeSyncWFImpl.TASK_QUEUE_SYNCHRONIZE_DP_SYNC_WF;

@Slf4j
@WorkflowImpl(taskQueues = TASK_QUEUE_SYNCHRONIZE_DP_SYNC_WF)
public class SynchronizeSyncWFImpl implements SynchronizeSyncWF, ApplicationContextAware {

  public static final String TASK_QUEUE_SYNCHRONIZE_DP_SYNC_WF = "SynchronizeDP_SYNC_WF";

  private PublishPaymentEventActivity publishPaymentEventActivity;
  private SendDebtPositionIONotificationActivity sendDebtPositionIONotificationActivity;

  /**
   * Temporal workflow will not allow to use injection in order to avoid <a href="https://docs.temporal.io/workflows#non-deterministic-change">non-deterministic changes</a> due to dynamic reconfiguration.<BR />
   * Anyway it allows to override ActivityOptions, but actually it's not supporting the override based on the particular workflow.<BR />
   * In {@link it.gov.pagopa.pu.workflow.config.TemporalWFImplementationCustomizer} we are already setting defaults to all workflows.<BR />
   * Use this as an example to override based on the particular workflow.
   */
  @Override
  public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
    SynchronizeDebtPositionWfConfig wfConfig = applicationContext.getBean(SynchronizeDebtPositionWfConfig.class);
    publishPaymentEventActivity = wfConfig.buildPublishPaymentEventActivityStub();
    sendDebtPositionIONotificationActivity = wfConfig.buildSendDebtPositionIONotificationActivityStub();
  }

  @Override
  public void synchronizeDpSync(DebtPositionDTO debtPosition, PaymentEventType paymentEventType) {
    log.info("Starting workflow to handle DebtPosition with ID: {}", debtPosition.getDebtPositionId());
    if(paymentEventType!=null){
      publishPaymentEventActivity.publish(debtPosition, paymentEventType, null);
    }
    sendDebtPositionIONotificationActivity.sendMessage(debtPosition);
    log.info("Message sent to IO for organizationId {} and debtPositionTypeOrgId {}", debtPosition.getOrganizationId(), debtPosition.getDebtPositionTypeOrgId());
  }
}
