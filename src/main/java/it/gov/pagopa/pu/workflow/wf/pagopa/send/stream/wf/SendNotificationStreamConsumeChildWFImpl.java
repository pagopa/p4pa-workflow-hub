package it.gov.pagopa.pu.workflow.wf.pagopa.send.stream.wf;

import io.temporal.failure.ActivityFailure;
import io.temporal.failure.ApplicationFailure;
import io.temporal.spring.boot.WorkflowImpl;
import it.gov.pagopa.payhub.activities.activity.sendnotification.stream.NotifySendNotificationStreamEventsActivity;
import it.gov.pagopa.payhub.activities.exception.sendnotification.SendStreamSkippedEventException;
import it.gov.pagopa.pu.sendnotification.dto.generated.NotificationStatusV26DTO;
import it.gov.pagopa.pu.sendnotification.dto.generated.ProgressResponseElementV28DTO;
import it.gov.pagopa.pu.sendnotification.dto.generated.StreamEventSummaryDTO;
import it.gov.pagopa.pu.sendnotification.dto.generated.TimelineElementCategoryV27DTO;
import it.gov.pagopa.pu.workflow.dto.SendStreamEventsProcessWFInputDTO;
import it.gov.pagopa.pu.workflow.utilities.TaskQueueConstants;
import it.gov.pagopa.pu.workflow.wf.pagopa.send.create.config.SendNotificationProcessWfConfig;
import it.gov.pagopa.pu.workflow.wf.pagopa.send.stream.activity.PublishSendTimelineEventActivity;
import it.gov.pagopa.pu.workflow.wf.pagopa.send.stream.config.SendNotificationStreamWfConfig;
import it.gov.pagopa.pu.workflow.wf.pagopa.send.stream.service.SendEventStreamProcessingService;
import it.gov.pagopa.pu.workflow.wf.pagopa.send.stream.service.SendEventStreamProcessingServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@WorkflowImpl(taskQueues = TaskQueueConstants.TASK_QUEUE_SEND_RESERVED_STREAM)
public class SendNotificationStreamConsumeChildWFImpl implements SendNotificationStreamConsumeChildWF, ApplicationContextAware {

  private static final Logger SKIPPED_EVENT_LOGGER = LoggerFactory.getLogger("SEND_NOTIFICATION_STREAM_SKIPPED_EVENT_LOG");

  private static final int LOOP_EXECUTIONS_BEFORE_CLOSE_CHILD_WF = 100;

  private int loopExecutionCount = 0;

  private SendEventStreamProcessingService sendEventStreamProcessingService;

  private PublishSendTimelineEventActivity publishSendTimelineEventActivity;
  private NotifySendNotificationStreamEventsActivity notifySendNotificationStreamEventsActivity;

  @Override
  public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
    SendNotificationStreamWfConfig wfConfig = applicationContext.getBean(SendNotificationStreamWfConfig.class);
    SendNotificationProcessWfConfig wfNotificationProcessConfig = applicationContext.getBean(SendNotificationProcessWfConfig.class);

    sendEventStreamProcessingService = new SendEventStreamProcessingServiceImpl(
      wfConfig.buildUpdateSendNotificationStatusActivityStub(),
      wfConfig.buildValidateSendNotificationStatusActivityStub(),
      wfConfig.buildSendNotificationDateRetrieveActivityStub(),
      wfNotificationProcessConfig.buildPublishSendNotificationPaymentEventActivityStub(),
      wfConfig.buildFetchSendLegalFactActivityStub(),
      wfConfig.buildStartDeleteSendNotificationFileActivityStub(),
      wfConfig.buildStartDeleteSendLegalFactFileActivityStub(),
      wfConfig.buildGetSendNotificationByNotificationRequestIdActivityStub()
    );
    publishSendTimelineEventActivity = wfConfig.buildPublishSendTimelineEventActivityStub();
    notifySendNotificationStreamEventsActivity = wfConfig.buildNotifySendNotificationStreamEventsActivityStub();
  }

  @Override
  public String processingStreamEvents(SendStreamEventsProcessWFInputDTO sendStreamEventsProcessWFInputDTO) {
    String traceId = it.gov.pagopa.payhub.activities.util.Utilities.getTraceId();
    String sendStreamId = sendStreamEventsProcessWFInputDTO.getSendStreamId();
    Long organizationId = sendStreamEventsProcessWFInputDTO.getOrganizationId();
    Map<String, List<StreamEventSummaryDTO>> notificationRequestIdToStreamEventsMap = new HashMap<>();
    String lastProcessedEventId = null;
    for (ProgressResponseElementV28DTO streamEvent : sendStreamEventsProcessWFInputDTO.getStreamEventBatch()) {
      String lastEventId;
      try {
        lastEventId = sendEventStreamProcessingService.processSendStreamEvent(sendStreamId, streamEvent);
        publishSendTimelineEventActivity.publishSendTimelineEvent(streamEvent, organizationId, sendStreamId, traceId);
        if(lastEventId != null) {
          lastProcessedEventId = lastEventId;
          collectStreamEventSummaries(streamEvent, notificationRequestIdToStreamEventsMap);
        }
      } catch (Exception e) {
        if(
          e instanceof ActivityFailure &&
          e.getCause() instanceof ApplicationFailure af &&
          af.isNonRetryable() &&
          SendStreamSkippedEventException.class.getName().equals(af.getType())
        ) {
          SKIPPED_EVENT_LOGGER.error("Stream event processing skipped for streamId {} event id {}, for error: {}", sendStreamId, streamEvent.getEventId(), e.getMessage());
          lastProcessedEventId = streamEvent.getEventId(); //skip event for NotRetryableActivityException
        } else {
          log.error("Stream event processing skipped for streamId %s, event id %s, for error: %s".formatted(sendStreamId, streamEvent.getEventId(), e.getMessage()));
          publishSendTimelineEventActivity.publishSendTimelineErrorEvent(streamEvent, organizationId, sendStreamId, traceId);
          lastProcessedEventId = streamEvent.getEventId(); //skipped event sent to Dead Letter
          break;
        }
      }
      if(++loopExecutionCount >= LOOP_EXECUTIONS_BEFORE_CLOSE_CHILD_WF) {
        break; //process at most a maximum number of stream events
      }
    }
    if(!notificationRequestIdToStreamEventsMap.isEmpty()) {
      notifySendNotificationStreamEventsActivity.notifySendNotificationStreamEvents(
        notificationRequestIdToStreamEventsMap
      );
    }
    return lastProcessedEventId;
  }

  private void collectStreamEventSummaries(ProgressResponseElementV28DTO streamEvent,  Map<String, List<StreamEventSummaryDTO>> notificationRequestIdToStreamEventsMap) {
    TimelineElementCategoryV27DTO eventCategory = streamEvent.getElement().getCategory();
    NotificationStatusV26DTO newNotificationStatus = streamEvent.getNewStatus();
    if(eventCategory != null && newNotificationStatus != null) {
      StreamEventSummaryDTO eventSummaryDTO = new StreamEventSummaryDTO(newNotificationStatus, eventCategory);
      List<StreamEventSummaryDTO> notificationEvents =
        notificationRequestIdToStreamEventsMap.computeIfAbsent(
          streamEvent.getNotificationRequestId(),
          k -> new ArrayList<>()
        );
      notificationEvents.add(eventSummaryDTO);
    }
  }

}
