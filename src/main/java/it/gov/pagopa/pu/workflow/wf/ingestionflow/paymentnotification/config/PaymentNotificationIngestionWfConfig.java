package it.gov.pagopa.pu.workflow.wf.ingestionflow.paymentnotification.config;

import io.temporal.workflow.Workflow;
import it.gov.pagopa.payhub.activities.activity.ingestionflow.paymentnotification.PaymentNotificationIngestionActivity;
import it.gov.pagopa.pu.workflow.config.temporal.BaseWfConfig;
import it.gov.pagopa.pu.workflow.config.temporal.TemporalWFImplementationCustomizer;
import it.gov.pagopa.pu.workflow.wf.ingestionflow.paymentnotification.activity.NotifyPaymentNotificationToIudClassificationActivity;
import it.gov.pagopa.pu.workflow.wf.ingestionflow.paymentnotification.wfingestion.PaymentNotificationIngestionWFImpl;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "workflow.payment-notification-ingestion")
public class PaymentNotificationIngestionWfConfig extends BaseWfConfig {

  public PaymentNotificationIngestionActivity buildPaymentNotificationIngestionActivityStub() {
    return Workflow.newActivityStub(PaymentNotificationIngestionActivity.class, TemporalWFImplementationCustomizer.baseWfConfig2ActivityOptions(this));
  }

  public NotifyPaymentNotificationToIudClassificationActivity buildNotifyPaymentNotificationToIudClassificationActivityStub() {
    return Workflow.newActivityStub(NotifyPaymentNotificationToIudClassificationActivity.class,
      TemporalWFImplementationCustomizer.baseWfConfig2ActivityOptions(
        PaymentNotificationIngestionWFImpl.TASK_QUEUE_PAYMENT_NOTIFICATION_INGESTION_LOCAL_ACTIVITY,
        this));
  }

}

