package it.gov.pagopa.pu.workflow.wf.pagopa.send.create.config;

import io.temporal.workflow.Workflow;
import it.gov.pagopa.payhub.activities.activity.sendnotification.create.DeliveryNotificationActivity;
import it.gov.pagopa.payhub.activities.activity.sendnotification.create.GetSendNotificationActivity;
import it.gov.pagopa.payhub.activities.activity.sendnotification.create.PreloadSendFileActivity;
import it.gov.pagopa.payhub.activities.activity.sendnotification.create.UploadSendFileActivity;
import it.gov.pagopa.pu.workflow.config.temporal.BaseWfConfig;
import it.gov.pagopa.pu.workflow.config.temporal.TemporalWFImplementationCustomizer;
import it.gov.pagopa.pu.workflow.utilities.TaskQueueConstants;
import it.gov.pagopa.pu.workflow.wf.pagopa.send.create.activity.PublishSendNotificationPaymentEventActivity;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "workflow.send-notification")
public class SendNotificationProcessWfConfig extends BaseWfConfig {

  public PreloadSendFileActivity buildPreloadSendFileActivityStub() {
    return Workflow.newActivityStub(PreloadSendFileActivity.class, TemporalWFImplementationCustomizer.baseWfConfig2ActivityOptions(this));
  }

  public UploadSendFileActivity buildUploadSendFileActivityStub() {
    return Workflow.newActivityStub(UploadSendFileActivity.class, TemporalWFImplementationCustomizer.baseWfConfig2ActivityOptions(this));
  }

  public DeliveryNotificationActivity buildDeliveryNotificationActivityStub() {
    return Workflow.newActivityStub(DeliveryNotificationActivity.class, TemporalWFImplementationCustomizer.baseWfConfig2ActivityOptions(this));
  }

  public GetSendNotificationActivity buildGetSendNotificationActivityStub() {
    return Workflow.newActivityStub(GetSendNotificationActivity.class, TemporalWFImplementationCustomizer.baseWfConfig2ActivityOptions(this));
  }

  public PublishSendNotificationPaymentEventActivity buildPublishSendNotificationPaymentEventActivityStub() {
    return Workflow.newActivityStub(PublishSendNotificationPaymentEventActivity.class,
      TemporalWFImplementationCustomizer.baseWfConfig2ActivityOptions(
        TaskQueueConstants.TASK_QUEUE_SEND_RESERVED_PUBLISH_EVENT_LOCAL, this));
  }

}
