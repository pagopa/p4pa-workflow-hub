package it.gov.pagopa.pu.workflow.wf.pagopa.send.payments.config;

import io.temporal.workflow.Workflow;
import it.gov.pagopa.payhub.activities.activity.sendnotification.payments.NotifyPaymentActivity;
import it.gov.pagopa.pu.workflow.config.temporal.BaseWfConfig;
import it.gov.pagopa.pu.workflow.config.temporal.TemporalWFImplementationCustomizer;
import it.gov.pagopa.pu.workflow.utilities.TaskQueueConstants;
import it.gov.pagopa.pu.workflow.wf.pagopa.send.payments.activity.ReceiptRetrieverActivity;
import it.gov.pagopa.pu.workflow.wf.pagopa.send.stream.activity.PublishSendTimelineEventActivity;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "workflow.send-notification-payment-handler")
public class SendNotificationPaymentHandlerWFConfig extends BaseWfConfig {

  public NotifyPaymentActivity buildNotifyPaymentActivityStub() {
    return Workflow.newActivityStub(NotifyPaymentActivity.class, TemporalWFImplementationCustomizer.baseWfConfig2ActivityOptions(this));
  }

  public ReceiptRetrieverActivity buildReceiptRetrieverActivityStub() {
    return Workflow.newActivityStub(ReceiptRetrieverActivity.class, TemporalWFImplementationCustomizer.baseWfConfig2ActivityOptions(this));
  }

  public PublishSendTimelineEventActivity buildPublishSendTimelineEventActivityStub() {
    return Workflow.newActivityStub(PublishSendTimelineEventActivity.class,
      TemporalWFImplementationCustomizer.baseWfConfig2ActivityOptions(TaskQueueConstants.TASK_QUEUE_SEND_RESERVED_PUBLISH_EVENT_LOCAL, this));
  }
}
