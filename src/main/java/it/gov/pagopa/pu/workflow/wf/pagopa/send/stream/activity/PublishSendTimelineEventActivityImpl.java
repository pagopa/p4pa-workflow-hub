package it.gov.pagopa.pu.workflow.wf.pagopa.send.stream.activity;

import io.temporal.activity.Activity;
import io.temporal.spring.boot.ActivityImpl;
import it.gov.pagopa.pu.sendnotification.dto.generated.ProgressResponseElementV28DTO;
import it.gov.pagopa.pu.workflow.event.registries.dto.RegistryEventSendTimelineDTO;
import it.gov.pagopa.pu.workflow.event.registries.producer.SendTimelineProducerService;
import it.gov.pagopa.pu.workflow.mapper.SendTimelineRegistryEventMapper;
import it.gov.pagopa.pu.workflow.utilities.TaskQueueConstants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

@Service
@Lazy
@Slf4j
@ActivityImpl(taskQueues = TaskQueueConstants.TASK_QUEUE_SEND_RESERVED_PUBLISH_EVENT_LOCAL)
public class PublishSendTimelineEventActivityImpl implements PublishSendTimelineEventActivity {

  private final SendTimelineProducerService sendTimelineProducerService;
  private final SendTimelineRegistryEventMapper sendTimelineRegistryEventMapper;

  public PublishSendTimelineEventActivityImpl(
    SendTimelineProducerService sendTimelineProducerService,
    SendTimelineRegistryEventMapper sendTimelineRegistryEventMapper) {
    this.sendTimelineProducerService = sendTimelineProducerService;
    this.sendTimelineRegistryEventMapper = sendTimelineRegistryEventMapper;
  }

  @Override
  public void publishSendTimelineEvent(ProgressResponseElementV28DTO sendTimelineEventDTO, Long organizationId, String sendStreamId, String traceId) {
    log.info("Publishing SendNotification timeline event {} (IUN {}) for notificationRequest: {}", sendTimelineEventDTO.getElement().getCategory(), sendTimelineEventDTO.getIun(), sendTimelineEventDTO.getNotificationRequestId());
    String workflowId = Activity.getExecutionContext().getInfo().getWorkflowId();
    RegistryEventSendTimelineDTO registryEventSendTimelineDTO =
      sendTimelineRegistryEventMapper.mapSuccess(sendTimelineEventDTO, organizationId, sendStreamId, workflowId, traceId);
    sendTimelineProducerService.notifySendTimelineEvent(registryEventSendTimelineDTO, sendStreamId);
  }

  @Override
  public void publishSendTimelineErrorEvent(ProgressResponseElementV28DTO sendTimelineEventDTO, Long organizationId, String sendStreamId, String traceId) {
    log.info("Publishing SendNotification error for timeline event {} (IUN {}) for notificationRequest: {}", sendTimelineEventDTO.getElement().getCategory(), sendTimelineEventDTO.getIun(), sendTimelineEventDTO.getNotificationRequestId());
    String workflowId = Activity.getExecutionContext().getInfo().getWorkflowId();
    RegistryEventSendTimelineDTO registryEventSendTimelineDTO =
      sendTimelineRegistryEventMapper.mapError(sendTimelineEventDTO, organizationId, sendStreamId, workflowId, traceId);
    sendTimelineProducerService.notifySendTimelineEvent(registryEventSendTimelineDTO, sendStreamId);
  }
}
