package it.gov.pagopa.pu.workflow.wf.pagopa.send.activity;

import io.temporal.activity.ActivityInterface;
import io.temporal.activity.ActivityMethod;
import it.gov.pagopa.pu.sendnotification.dto.generated.ProgressResponseElementV28DTO;

/** It will publish an event related to SEND timeline on Kafka */
@ActivityInterface
public interface PublishSendTimelineEventActivity {
  @ActivityMethod
  void publishSendTimelineEvent(ProgressResponseElementV28DTO sendNotification, Long organizationId, String sendStreamId, String traceId);
  @ActivityMethod
  void publishSendTimelineErrorEvent(ProgressResponseElementV28DTO sendNotification, Long organizationId, String sendStreamId, String traceId);
}
