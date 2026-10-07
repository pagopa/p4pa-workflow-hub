package it.gov.pagopa.pu.workflow.wf.pagopa.send.stream.activity;

import io.temporal.activity.ActivityInterface;
import io.temporal.activity.ActivityMethod;
import it.gov.pagopa.pu.sendnotification.dto.generated.ProgressResponseElementV28DTO;
import it.gov.pagopa.pu.sendnotification.dto.generated.SendNotificationDTO;

/** It will publish an event related to SEND timeline on Kafka */
@ActivityInterface
public interface PublishSendTimelineEventActivity {
  @ActivityMethod
  void publishSendTimelineEvent(ProgressResponseElementV28DTO sendTimelineEventDTO, SendNotificationDTO sendNotificationDTO, String sendStreamId, String traceId);
  @ActivityMethod
  void publishSendTimelineErrorEvent(ProgressResponseElementV28DTO sendTimelineEventDTO, SendNotificationDTO sendNotificationDTO, String sendStreamId, String traceId);
}
