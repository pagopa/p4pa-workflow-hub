package it.gov.pagopa.pu.workflow.wf.ingestionflow.paymentnotification.wfingestion;

import io.temporal.spring.boot.WorkflowImpl;
import it.gov.pagopa.payhub.activities.activity.ingestionflow.UpdateIngestionFlowStatusActivity;
import it.gov.pagopa.payhub.activities.activity.ingestionflow.email.SendEmailIngestionFlowActivity;
import it.gov.pagopa.payhub.activities.activity.ingestionflow.paymentnotification.PaymentNotificationIngestionActivity;
import it.gov.pagopa.payhub.activities.dto.paymentnotification.PaymentNotificationIngestionFlowFileActivityResult;
import it.gov.pagopa.pu.processexecutions.dto.generated.IngestionFlowFileStatus;
import it.gov.pagopa.pu.workflow.wf.ingestionflow.paymentnotification.activity.NotifyPaymentNotificationToIudClassificationActivity;
import it.gov.pagopa.pu.workflow.wf.ingestionflow.paymentnotification.config.PaymentNotificationIngestionWfConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import static it.gov.pagopa.pu.workflow.wf.ingestionflow.paymentnotification.wfingestion.PaymentNotificationIngestionWFImpl.TASK_QUEUE_PAYMENT_NOTIFICATION_INGESTION_WF;

@Slf4j
@WorkflowImpl(taskQueues = TASK_QUEUE_PAYMENT_NOTIFICATION_INGESTION_WF)
public class PaymentNotificationIngestionWFImpl implements PaymentNotificationIngestionWF, ApplicationContextAware {
  public static final String TASK_QUEUE_PAYMENT_NOTIFICATION_INGESTION_WF = "PaymentNotificationIngestionWF";
  public static final String TASK_QUEUE_PAYMENT_NOTIFICATION_INGESTION_LOCAL_ACTIVITY = "PaymentNotificationIngestionWF_LOCAL";

  private PaymentNotificationIngestionActivity paymentNotificationIngestionActivity;
  private SendEmailIngestionFlowActivity sendEmailIngestionFlowActivity;
  private UpdateIngestionFlowStatusActivity updateIngestionFlowStatusActivity;
  private NotifyPaymentNotificationToIudClassificationActivity notifyPaymentNotificationToIudClassificationActivity;

  /**
   * Temporal workflow will not allow to use injection in order to avoid <a href="https://docs.temporal.io/workflows#non-deterministic-change">non-deterministic changes</a> due to dynamic reconfiguration.<BR />
   * Anyway it allows to override ActivityOptions, but actually it's not supporting the override based on the particular workflow.<BR />
   * In {@link it.gov.pagopa.pu.workflow.config.TemporalWFImplementationCustomizer} we are already setting defaults to all workflows.<BR />
   * Use this as an example to override based on the particular workflow.
   */
  @Override
  public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
    PaymentNotificationIngestionWfConfig wfConfig = applicationContext.getBean(PaymentNotificationIngestionWfConfig.class);

    paymentNotificationIngestionActivity = wfConfig.buildPaymentNotificationIngestionActivityStub();
    sendEmailIngestionFlowActivity = wfConfig.buildSendEmailIngestionFlowActivityStub();
    updateIngestionFlowStatusActivity = wfConfig.buildUpdateIngestionFlowStatusActivityStub();
    notifyPaymentNotificationToIudClassificationActivity = wfConfig.buildNotifyPaymentNotificationToIudClassificationActivityStub();
  }

  @Override
  public void ingest(Long ingestionFlowFileId) {
    log.info("Handling Payment Notification Ingesting FlowFileId {}", ingestionFlowFileId);

    updateIngestionFlowStatusActivity.updateStatus(ingestionFlowFileId, IngestionFlowFileStatus.UPLOADED, IngestionFlowFileStatus.PROCESSING, null, null);
    String errorDescription = processFile(ingestionFlowFileId);

    boolean success = errorDescription==null;
    updateIngestionFlowStatusActivity.updateStatus(ingestionFlowFileId,
      IngestionFlowFileStatus.PROCESSING,
      success
        ? IngestionFlowFileStatus.COMPLETED
        : IngestionFlowFileStatus.ERROR,
      errorDescription,
      null);
    sendEmailIngestionFlowActivity.sendEmail(ingestionFlowFileId, success);

    log.info("Payment Notification Ingestion completed for file with ID {} with success {} and errorDescription {}",
      ingestionFlowFileId, success, errorDescription);
  }

  private String processFile(Long ingestionFlowFileId) {
    try{
      PaymentNotificationIngestionFlowFileActivityResult ingestionResult = paymentNotificationIngestionActivity.processFile(ingestionFlowFileId);
Long orgId= ingestionResult.getOrganizationId();

      ingestionResult.getIudList().forEach(
        iud -> notifyPaymentNotificationToIudClassificationActivity.signalIudClassificationWithStart(
          orgId,
          iud));

      return null;
    } catch (Exception e) {
      return e.getMessage();
    }
  }

}
