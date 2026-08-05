package it.gov.pagopa.pu.workflow.wf.pagopa.send.stream.wf;

import io.temporal.failure.ActivityFailure;
import io.temporal.failure.ApplicationFailure;
import io.temporal.spring.boot.WorkflowImpl;
import io.temporal.workflow.Workflow;
import it.gov.pagopa.payhub.activities.activity.sendnotification.stream.*;
import it.gov.pagopa.payhub.activities.exception.sendnotification.SendStreamSkippedEventException;
import it.gov.pagopa.pu.sendnotification.dto.generated.*;
import it.gov.pagopa.pu.workflow.config.temporal.TemporalWFImplementationCustomizer;
import it.gov.pagopa.pu.workflow.exception.custom.IllegalStateBusinessException;
import it.gov.pagopa.pu.workflow.utilities.ErrorCodeConstants;
import it.gov.pagopa.pu.workflow.utilities.TaskQueueConstants;
import it.gov.pagopa.pu.workflow.utilities.Utilities;
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
import org.springframework.util.CollectionUtils;
import org.springframework.web.client.HttpClientErrorException;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Slf4j
@WorkflowImpl(taskQueues = TaskQueueConstants.TASK_QUEUE_SEND_RESERVED_STREAM)
public class SendNotificationStreamConsumeWFImpl implements SendNotificationStreamConsumeWF, ApplicationContextAware {
  private static final Logger SKIPPED_EVENT_LOGGER = LoggerFactory.getLogger("SEND_NOTIFICATION_STREAM_SKIPPED_EVENT_LOG");

  private static final int LOOP_EXECUTIONS_BEFORE_CLEAN_WF_HISTORY = 100;
  private static final int WAITING_SECONDS_NEXT_POLL = 5 * 60;

  private int loopExecutionCount = 0;

  private GetSendStreamActivity getSendStreamActivity;
  private GetSendNotificationEventsFromStreamActivity getSendNotificationEventsFromStreamActivity;
  private SendEventStreamProcessingService sendEventStreamProcessingService;
  private UpdateLastProcessedStreamEventIdActivity updateLastProcessedStreamEventIdActivity;
  private PublishSendTimelineEventActivity publishSendTimelineEventActivity;
  private NotifySendNotificationStreamEventsActivity notifySendNotificationStreamEventsActivity;

  /**
   * Temporal workflow will not allow to use injection in order to avoid <a href="https://docs.temporal.io/workflows#non-deterministic-change">non-deterministic changes</a> due to dynamic reconfiguration.<BR />
   * Anyway it allows to override ActivityOptions, but actually it's not supporting the override based on the particular workflow.<BR />
   * In {@link TemporalWFImplementationCustomizer} we are already setting defaults to all workflows.<BR />
   * Use this as an example to override based on the particular workflow.
   */
  @Override
  public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
    SendNotificationStreamWfConfig wfConfig = applicationContext.getBean(SendNotificationStreamWfConfig.class);
    SendNotificationProcessWfConfig wfNotificationProcessConfig = applicationContext.getBean(SendNotificationProcessWfConfig.class);

    getSendNotificationEventsFromStreamActivity = wfConfig.buildGetSendNotificationEventsFromStreamActivityStub();
    getSendStreamActivity = wfConfig.buildGetSendStreamActivityStub();
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
    updateLastProcessedStreamEventIdActivity = wfConfig.buildUpdateLastProcessedStreamEventIdActivityStub();
    publishSendTimelineEventActivity = wfConfig.buildPublishSendTimelineEventActivityStub();
    notifySendNotificationStreamEventsActivity = wfConfig.buildNotifySendNotificationStreamEventsActivityStub();
  }

  @Override
  public void readSendStream(String sendStreamId) {
    log.info("Start readSendStream Workflow for sendStreamId {}.", sendStreamId);

    SendStreamDTO sendStreamDTO = getSendStreamActivity.fetchSendStream(sendStreamId);
    if(sendStreamDTO == null) {
      log.error("[STREAMS_NOT_FOUND] Cannot fetch stream: SEND stream non found for sendStreamId {}", sendStreamId);
      throw new IllegalStateBusinessException(ErrorCodeConstants.ERROR_CODE_SEND_STATUS_ERROR, "Workflow terminated during starting of readSendStream for sendStreamId %s with ERROR: cannot found SEND stream.".formatted(sendStreamId));
    }

    String lastProcessedEventId = sendStreamDTO.getLastEventId(); //start reading after latest processed event
    do {
      try {
        List<ProgressResponseElementV28DTO> streamEvents = this.getSendNotificationEventsFromStreamActivity.fetchSendNotificationEventsFromStream(
          sendStreamDTO.getOrganizationId(),
          sendStreamId
        );
        if (!CollectionUtils.isEmpty(streamEvents)) {
          lastProcessedEventId = processingStreamEvents(sendStreamDTO.getOrganizationId(), sendStreamId, streamEvents, lastProcessedEventId);
        }
      } catch(Throwable t) {
        log.error("Something went wrong processing stream {}: {}",
          sendStreamId, Utilities.getWorkflowExceptionMessage(t));
      }
      boolean hasCommitedAnEvent = this.commitLastProcessedEventId(sendStreamDTO, lastProcessedEventId);
      if(hasCommitedAnEvent) {
        Workflow.continueAsNew(sendStreamId);
      }
      waitForNextIteration(sendStreamId);
    } while (isStreamStillOpened(sendStreamId));

    log.info("Stopped readSendStream Workflow for sendStreamId {}, because SEND stream has been closed.", sendStreamId);
  }

  private String processingStreamEvents(Long organizationId, String sendStreamId, List<ProgressResponseElementV28DTO> streamEventBatch, String lastProcessedEventId) {
    String traceId = it.gov.pagopa.payhub.activities.util.Utilities.getTraceId();
    Map<String, List<StreamEventSummaryDTO>> notificationRequestIdToStreamEventsMap = new HashMap<>();
    for (ProgressResponseElementV28DTO streamEvent : streamEventBatch) {
      String lastEventId;
      try {
        lastEventId = sendEventStreamProcessingService.processSendStreamEvent(sendStreamId, streamEvent);
        publishSendTimelineEventActivity.publishSendTimelineEvent(streamEvent, organizationId, sendStreamId, traceId);
        if(lastEventId != null) {
          lastProcessedEventId = lastEventId;
          collectStreamEventSummaries(streamEvent, notificationRequestIdToStreamEventsMap);
        }
      } catch (Exception e) {
        if(e instanceof ActivityFailure &&
          e.getCause() instanceof ApplicationFailure af &&
          af.isNonRetryable() &&
          SendStreamSkippedEventException.class.getName().equals(af.getType())
        ) {
          SKIPPED_EVENT_LOGGER.error("Stream event processing skipped for streamId {} event id {}, for error: {}", sendStreamId, streamEvent.getEventId(), e.getMessage());
          lastProcessedEventId = streamEvent.getEventId(); //skip events for NotRetryableActivityException
        } else {
          log.error("Stream events processing blocked for streamId %s, for error: %s".formatted(sendStreamId, e.getMessage()));
          publishSendTimelineEventActivity.publishSendTimelineErrorEvent(streamEvent, organizationId, sendStreamId, traceId);
          lastProcessedEventId = streamEvent.getEventId(); //skip events sent to Dead Letter
          break;
        }
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

  private boolean commitLastProcessedEventId(SendStreamDTO sendStreamDTO, String lastProcessedEventId) {
    if(lastProcessedEventId==null || lastProcessedEventId.equals(sendStreamDTO.getLastEventId())) {
      return false;
    }
    try {
      updateLastProcessedStreamEventIdActivity.updateLastProcessedStreamEventId(sendStreamDTO.getStreamId(), lastProcessedEventId);
      sendStreamDTO.setLastEventId(lastProcessedEventId);
      return true;
    } catch (Exception e) {
      log.error("Error in updating last processed event id for stream with id %s".formatted(sendStreamDTO.getStreamId()), e);
      return false;
    }
  }

  private boolean isStreamStillOpened(String sendStreamId) {
    try {
      return getSendStreamActivity.fetchSendStream(sendStreamId) != null;
    } catch (HttpClientErrorException.NotFound e) {
      log.error("STREAMS_NOT_FOUND] Cannot fetch stream: SEND stream non found for sendStreamId {}", sendStreamId);
      throw new IllegalStateBusinessException(ErrorCodeConstants.ERROR_CODE_SEND_STATUS_ERROR, "Workflow terminated during isStreamStillOpened for sendStreamId " + sendStreamId + " with ERROR: " + e.getMessage());
    } catch (Exception e) {
      return true;
    }
  }

  private void waitForNextIteration(String sendStreamId) {
    Workflow.sleep(
      Duration.of(
        WAITING_SECONDS_NEXT_POLL,
        ChronoUnit.SECONDS
      )
    );
    loopExecutionCount += 1;
    if(loopExecutionCount >= LOOP_EXECUTIONS_BEFORE_CLEAN_WF_HISTORY) {
      loopExecutionCount = 0;
      Workflow.continueAsNew(sendStreamId);
    }
  }

}
