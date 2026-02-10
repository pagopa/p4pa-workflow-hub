package it.gov.pagopa.pu.workflow.wf.pagopa.send.wfsendnotification;

import io.temporal.spring.boot.WorkflowImpl;
import io.temporal.workflow.Workflow;
import it.gov.pagopa.payhub.activities.activity.sendnotification.*;
import it.gov.pagopa.pu.sendnotification.dto.generated.ProgressResponseElementV25DTO;
import it.gov.pagopa.pu.sendnotification.dto.generated.SendStreamDTO;
import it.gov.pagopa.pu.workflow.config.temporal.TemporalWFImplementationCustomizer;
import it.gov.pagopa.pu.workflow.dto.SendEventStreamProcessResult;
import it.gov.pagopa.pu.workflow.exception.custom.WorkflowInternalErrorException;
import it.gov.pagopa.pu.workflow.service.wf.send.SendEventStreamProcessingService;
import it.gov.pagopa.pu.workflow.service.wf.send.SendEventStreamProcessingServiceImpl;
import it.gov.pagopa.pu.workflow.utilities.TaskQueueConstants;
import it.gov.pagopa.pu.workflow.wf.pagopa.send.config.SendNotificationProcessWfConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Slf4j
@WorkflowImpl(taskQueues = TaskQueueConstants.TASK_QUEUE_SEND_RESERVED_STREAM)
public class SendNotificationStreamConsumeWFImpl implements SendNotificationStreamConsumeWF, ApplicationContextAware {

  private static final int ACTIVITY_EXECUTIONS_BEFORE_CLEAN_WF_HISTORY = 1000;
  private static final int SEND_NOTIFICATION_STREAM_CONSUMER_READ_BASE_DELAY_IN_SECONDS = 5 * 60;
  private static final int SEND_NOTIFICATION_STREAM_CONSUMER_READ_RETRY_DELAY_IN_SECONDS = 15;

  private int activityExecutionCount = 0;
  private int workflowExecutionErrorCount = 0;

  private GetSendStreamActivity getSendStreamActivity;
  private GetSendNotificationEventsFromStreamActivity getSendNotificationEventsFromStreamActivity;
  private SendEventStreamProcessingService sendEventStreamProcessingService;

  /**
   * Temporal workflow will not allow to use injection in order to avoid <a href="https://docs.temporal.io/workflows#non-deterministic-change">non-deterministic changes</a> due to dynamic reconfiguration.<BR />
   * Anyway it allows to override ActivityOptions, but actually it's not supporting the override based on the particular workflow.<BR />
   * In {@link TemporalWFImplementationCustomizer} we are already setting defaults to all workflows.<BR />
   * Use this as an example to override based on the particular workflow.
   */
  @Override
  public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
    SendNotificationProcessWfConfig wfConfig = applicationContext.getBean(SendNotificationProcessWfConfig.class);

    getSendNotificationEventsFromStreamActivity = wfConfig.buildGetSendNotificationEventsFromStreamActivityStub();
    getSendStreamActivity = wfConfig.buildGetSendStreamActivityStub();
    sendEventStreamProcessingService = new SendEventStreamProcessingServiceImpl(
      wfConfig.buildUpdateSendNotificationStatusActivityStub(),
      wfConfig.buildSendNotificationDateRetrieveActivityStub(),
      wfConfig.buildPublishSendNotificationPaymentEventActivityStub()
    );
  }

  @Override
  public void readSendStream(String sendStreamId) {
    log.info("Start readSendStream Workflow for sendStreamId {}.", sendStreamId);

    SendStreamDTO sendStreamDTO = getSendStreamActivity.fetchSendStream(sendStreamId);
    if(sendStreamDTO == null) {
      log.error("[STREAMS_NOT_FOUND] Cannot fetch stream: SEND stream non found for sendStreamId {}", sendStreamId);
      throw new WorkflowInternalErrorException("[SEND_STATUS_ERROR] Workflow terminated during starting of readSendStream for sendStreamId %s with ERROR: cannot found SEND stream.".formatted(sendStreamId));
    }


    String lastProcessedEventId = sendStreamDTO.getLastEventId(); //start reading after latest processed event
    List<ProgressResponseElementV25DTO> streamEvents;

    do {
      streamEvents = tryFetchingStreamEventsOrWaitForNextStreamRead(sendStreamId, sendStreamDTO, lastProcessedEventId);
      if (streamEvents == null) continue;
      lastProcessedEventId = tryProcessingStreamEvents(sendStreamId, streamEvents, lastProcessedEventId, sendStreamDTO);
      waitForNextIteration(sendStreamId);
      activityExecutionCount += 1; //incrementing for activity in do-while condition
    } while (isStreamStillOpened(sendStreamId));

    log.info("Stopped readSendStream Workflow for sendStreamId {}, because SEND stream has been closed.", sendStreamId);
  }

  private List<ProgressResponseElementV25DTO> tryFetchingStreamEventsOrWaitForNextStreamRead(String sendStreamId, SendStreamDTO sendStreamDTO, String lastProcessedEventId) {
    List<ProgressResponseElementV25DTO> streamEvents;
    try {
      activityExecutionCount += 1;
      streamEvents = this.getSendNotificationEventsFromStreamActivity.fetchSendNotificationEventsFromStream(
        sendStreamDTO.getOrganizationId(),
        sendStreamId,
        lastProcessedEventId
      );
    } catch (Exception e) {
      workflowExecutionErrorCount += 1;
      log.warn("Cannot read new stream event batch: for sendStreamId {} and organizationId {}, last read event has id {}; will retry in {} seconds",
        sendStreamId,
        sendStreamDTO.getOrganizationId(),
        lastProcessedEventId,
        calculateNextRetryDelay()
      );
      waitForNextIteration(sendStreamId);
      return null;
    }
    return streamEvents;
  }

  private String tryProcessingStreamEvents(String sendStreamId, List<ProgressResponseElementV25DTO> streamEvents, String lastProcessedEventId, SendStreamDTO sendStreamDTO) {
    for (ProgressResponseElementV25DTO streamEvent : streamEvents) {
      try {
        SendEventStreamProcessResult eventStreamProcessResult =
          sendEventStreamProcessingService.processSendStreamEvent(sendStreamId, streamEvent);
        activityExecutionCount += eventStreamProcessResult.getActivityExecutionCount();
        if(eventStreamProcessResult.getLastProcessedEventId() != null)
          lastProcessedEventId = eventStreamProcessResult.getLastProcessedEventId();
        if(eventStreamProcessResult.getActivityExecutionCount() == streamEvents.size()) {
          workflowExecutionErrorCount = 0;
        }
      } catch (Exception e) {
        workflowExecutionErrorCount += 1;
        log.warn("Cannot complete reading stream event batch: for sendStreamId {} and organizationId {}, last read event has id {}; will retry in {} seconds",
          sendStreamId,
          sendStreamDTO.getOrganizationId(),
          lastProcessedEventId,
          calculateNextRetryDelay()
        );
        break;
      }
    }
    return lastProcessedEventId;
  }

  private boolean isStreamStillOpened(String sendStreamId) {
    try {
      return getSendStreamActivity.fetchSendStream(sendStreamId) != null;
    } catch (Exception e) {
      log.warn("STREAMS_NOT_FOUND] Cannot fetch stream: SEND stream non found for sendStreamId {}", sendStreamId);
      throw new WorkflowInternalErrorException("[SEND_STATUS_ERROR] Workflow terminated during isStreamStillOpened for sendStreamId " + sendStreamId + " with ERROR: " + e.getMessage());
    }
  }

  private void waitForNextIteration(String workflowId) {
    int sleepDelay = workflowExecutionErrorCount != 0 ?
      calculateNextRetryDelay() :
      SEND_NOTIFICATION_STREAM_CONSUMER_READ_BASE_DELAY_IN_SECONDS;
    Workflow.sleep(Duration.of(sleepDelay, ChronoUnit.SECONDS));
    if(activityExecutionCount >= ACTIVITY_EXECUTIONS_BEFORE_CLEAN_WF_HISTORY) {
      activityExecutionCount = 0;
      Workflow.continueAsNew(workflowId);
    }
  }

  private int calculateNextRetryDelay() {
    return Math.min(
      SEND_NOTIFICATION_STREAM_CONSUMER_READ_RETRY_DELAY_IN_SECONDS * workflowExecutionErrorCount,
      SEND_NOTIFICATION_STREAM_CONSUMER_READ_BASE_DELAY_IN_SECONDS
    );
  }

}
