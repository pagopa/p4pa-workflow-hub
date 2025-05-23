package it.gov.pagopa.pu.workflow.wf.ingestionflow.paymentnotification.wfingestion;

import io.temporal.spring.boot.WorkflowImpl;
import it.gov.pagopa.payhub.activities.activity.ingestionflow.paymentnotification.PaymentNotificationIngestionActivity;
import it.gov.pagopa.payhub.activities.dto.ingestion.paymentnotification.PaymentNotificationIngestionFlowFileResult;
import it.gov.pagopa.pu.workflow.config.temporal.TemporalWFImplementationCustomizer;
import it.gov.pagopa.pu.workflow.wf.ingestionflow.BaseIngestionFlowFileWFImpl;
import it.gov.pagopa.pu.workflow.wf.ingestionflow.paymentnotification.activity.NotifyPaymentNotificationToIudClassificationActivity;
import it.gov.pagopa.pu.workflow.wf.ingestionflow.paymentnotification.config.PaymentNotificationIngestionWfConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;

import static it.gov.pagopa.pu.workflow.wf.ingestionflow.paymentnotification.wfingestion.PaymentNotificationIngestionWFImpl.TASK_QUEUE_PAYMENT_NOTIFICATION_INGESTION_WF;

@Slf4j
@WorkflowImpl(taskQueues = TASK_QUEUE_PAYMENT_NOTIFICATION_INGESTION_WF)
public class PaymentNotificationIngestionWFImpl extends BaseIngestionFlowFileWFImpl<PaymentNotificationIngestionFlowFileResult> implements PaymentNotificationIngestionWF {
  public static final String TASK_QUEUE_PAYMENT_NOTIFICATION_INGESTION_WF = "PaymentNotificationIngestionWF";
  public static final String TASK_QUEUE_PAYMENT_NOTIFICATION_INGESTION_LOCAL_ACTIVITY = "PaymentNotificationIngestionWF_LOCAL";

  private NotifyPaymentNotificationToIudClassificationActivity notifyPaymentNotificationToIudClassificationActivity;

  /**
   * Temporal workflow will not allow to use injection in order to avoid <a href="https://docs.temporal.io/workflows#non-deterministic-change">non-deterministic changes</a> due to dynamic reconfiguration.<BR />
   * Anyway it allows to override ActivityOptions, but actually it's not supporting the override based on the particular workflow.<BR />
   * In {@link TemporalWFImplementationCustomizer} we are already setting defaults to all workflows.<BR />
   * Use this as an example to override based on the particular workflow.
   */
  @Override
  public PaymentNotificationIngestionActivity buildActivityStubs(ApplicationContext applicationContext) {
    PaymentNotificationIngestionWfConfig wfConfig = applicationContext.getBean(PaymentNotificationIngestionWfConfig.class);

    PaymentNotificationIngestionActivity paymentNotificationIngestionActivity = wfConfig.buildPaymentNotificationIngestionActivityStub();
    notifyPaymentNotificationToIudClassificationActivity = wfConfig.buildNotifyPaymentNotificationToIudClassificationActivityStub();

    return paymentNotificationIngestionActivity;
  }

  @Override
  protected void afterProcessing(Long ingestionFlowFileId, PaymentNotificationIngestionFlowFileResult ingestionResult) {
    ingestionResult.getIudList().forEach(
      iud -> notifyPaymentNotificationToIudClassificationActivity.signalIudClassificationWithStart(
        ingestionResult.getOrganizationId(),
        iud));
  }
}
