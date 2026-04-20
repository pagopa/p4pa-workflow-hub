package it.gov.pagopa.pu.workflow.wf.pagopa.send.stream.config;

import io.temporal.workflow.Workflow;
import it.gov.pagopa.payhub.activities.activity.sendnotification.stream.GetSendNotificationEventsFromStreamActivity;
import it.gov.pagopa.payhub.activities.activity.sendnotification.stream.GetSendStreamActivity;
import it.gov.pagopa.payhub.activities.activity.sendnotification.stream.UpdateLastProcessedStreamEventIdActivity;
import it.gov.pagopa.payhub.activities.activity.sendnotification.stream.processing.FetchSendLegalFactActivity;
import it.gov.pagopa.payhub.activities.activity.sendnotification.stream.processing.SendNotificationDateRetrieveActivity;
import it.gov.pagopa.payhub.activities.activity.sendnotification.stream.processing.UpdateSendNotificationStatusActivity;
import it.gov.pagopa.payhub.activities.activity.sendnotification.stream.processing.ValidateSendNotificationStatusActivity;
import it.gov.pagopa.pu.workflow.config.temporal.BaseWfConfig;
import it.gov.pagopa.pu.workflow.config.temporal.TemporalWFImplementationCustomizer;
import it.gov.pagopa.pu.workflow.utilities.TaskQueueConstants;
import it.gov.pagopa.pu.workflow.wf.pagopa.send.stream.activity.PublishSendTimelineEventActivity;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "workflow.send-notification-stream")
public class SendNotificationStreamWfConfig extends BaseWfConfig {

  public FetchSendLegalFactActivity buildFetchSendLegalFactActivityStub() {
    return Workflow.newActivityStub(FetchSendLegalFactActivity.class, TemporalWFImplementationCustomizer.baseWfConfig2ActivityOptions(this));
  }

  public SendNotificationDateRetrieveActivity buildSendNotificationDateRetrieveActivityStub() {
    return Workflow.newActivityStub(SendNotificationDateRetrieveActivity.class, TemporalWFImplementationCustomizer.baseWfConfig2ActivityOptions(this));
  }

  public UpdateSendNotificationStatusActivity buildUpdateSendNotificationStatusActivityStub() {
    return Workflow.newActivityStub(UpdateSendNotificationStatusActivity.class, TemporalWFImplementationCustomizer.baseWfConfig2ActivityOptions(this));
  }

  public ValidateSendNotificationStatusActivity buildValidateSendNotificationStatusActivityStub() {
    return Workflow.newActivityStub(ValidateSendNotificationStatusActivity.class, TemporalWFImplementationCustomizer.baseWfConfig2ActivityOptions(this));
  }

  public GetSendStreamActivity buildGetSendStreamActivityStub() {
    return Workflow.newActivityStub(GetSendStreamActivity.class, TemporalWFImplementationCustomizer.baseWfConfig2ActivityOptions(this));
  }

  public GetSendNotificationEventsFromStreamActivity buildGetSendNotificationEventsFromStreamActivityStub() {
    return Workflow.newActivityStub(GetSendNotificationEventsFromStreamActivity.class, TemporalWFImplementationCustomizer.baseWfConfig2ActivityOptions(this));
  }

  public UpdateLastProcessedStreamEventIdActivity buildUpdateLastProcessedStreamEventIdActivityStub() {
    return Workflow.newActivityStub(UpdateLastProcessedStreamEventIdActivity.class, TemporalWFImplementationCustomizer.baseWfConfig2ActivityOptions(this));
  }

  public PublishSendTimelineEventActivity buildPublishSendTimelineEventActivityStub() {
    return Workflow.newActivityStub(PublishSendTimelineEventActivity.class,
      TemporalWFImplementationCustomizer.baseWfConfig2ActivityOptions(
        TaskQueueConstants.TASK_QUEUE_SEND_RESERVED_PUBLISH_EVENT_LOCAL, this));
  }

}
