package it.gov.pagopa.pu.workflow.wf.pagopa.send.config;

import io.temporal.workflow.Workflow;
import it.gov.pagopa.payhub.activities.activity.sendnotification.*;
import it.gov.pagopa.pu.workflow.config.temporal.BaseWfConfig;
import it.gov.pagopa.pu.workflow.config.temporal.TemporalWFImplementationCustomizer;
import it.gov.pagopa.pu.workflow.wf.pagopa.send.activity.PublishSendNotificationPaymentEventActivity;
import it.gov.pagopa.pu.workflow.wf.pagopa.send.activity.ScheduleSendNotificationDateRetrieveActivity;
import it.gov.pagopa.pu.workflow.wf.pagopa.send.wfsendnotification.SendNotificationProcessWFImpl;
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

  public NotificationStatusActivity buildNotificationStatusActivityStub() {
    return Workflow.newActivityStub(NotificationStatusActivity.class, TemporalWFImplementationCustomizer.baseWfConfig2ActivityOptions(this));
  }

  public GetSendNotificationActivity buildGetSendNotificationActivityStub() {
    return Workflow.newActivityStub(GetSendNotificationActivity.class, TemporalWFImplementationCustomizer.baseWfConfig2ActivityOptions(this));
  }

  public SendNotificationDateRetrieveActivity buildSendNotificationDateRetrieveActivityStub() {
    return Workflow.newActivityStub(SendNotificationDateRetrieveActivity.class, TemporalWFImplementationCustomizer.baseWfConfig2ActivityOptions(this));
  }

  public PublishSendNotificationPaymentEventActivity buildPublishSendNotificationPaymentEventActivityStub() {
    return Workflow.newActivityStub(PublishSendNotificationPaymentEventActivity.class,
      TemporalWFImplementationCustomizer.baseWfConfig2ActivityOptions(
        SendNotificationProcessWFImpl.TASK_QUEUE_SEND_NOTIFICATION_PROCESS_LOCAL_ACTIVITY, this));
  }

  public ScheduleSendNotificationDateRetrieveActivity buildScheduleSendNotificationDateRetrieveActivityStub() {
    return Workflow.newActivityStub(ScheduleSendNotificationDateRetrieveActivity.class,
      TemporalWFImplementationCustomizer.baseWfConfig2ActivityOptions(
        SendNotificationProcessWFImpl.TASK_QUEUE_SEND_NOTIFICATION_PROCESS_LOCAL_ACTIVITY, this));
  }
}
