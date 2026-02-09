package it.gov.pagopa.pu.workflow.wf.pagopa.send.wfsendnotification;

import io.temporal.spring.boot.WorkflowImpl;
import io.temporal.workflow.Workflow;
import it.gov.pagopa.payhub.activities.activity.sendnotification.*;
import it.gov.pagopa.pu.sendnotification.dto.generated.NotificationStatus;
import it.gov.pagopa.pu.sendnotification.dto.generated.ProgressResponseElementV25DTO;
import it.gov.pagopa.pu.sendnotification.dto.generated.SendNotificationDTO;
import it.gov.pagopa.pu.sendnotification.dto.generated.SendStreamDTO;
import it.gov.pagopa.pu.workflow.config.temporal.TemporalWFImplementationCustomizer;
import it.gov.pagopa.pu.workflow.dto.PaymentEventRequestDTO;
import it.gov.pagopa.pu.workflow.utilities.TaskQueueConstants;
import it.gov.pagopa.pu.workflow.wf.pagopa.send.activity.PublishSendNotificationPaymentEventActivity;
import it.gov.pagopa.pu.workflow.wf.pagopa.send.config.SendNotificationProcessWfConfig;
import it.gov.pagopa.pu.workflow.wf.pagopa.send.mapper.SendNotification2DebtPositionSendNotificationsMapper;
import it.gov.pagopa.pu.workflow.dto.generated.PaymentEventType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@WorkflowImpl(taskQueues = TaskQueueConstants.TASK_QUEUE_SEND_RESERVED_STREAM)
public class SendNotificationStreamConsumeWFImpl implements SendNotificationStreamConsumeWF, ApplicationContextAware {

  private static final int ACTIVITY_EXECUTIONS_BEFORE_CLEAN_WF_HISTORY = 1000;
  private static final int SEND_NOTIFICATION_STREAM_CONSUMER_READ_BASE_DELAY_IN_SECONDS = 5 * 60;
  private static final int SEND_NOTIFICATION_STREAM_CONSUMER_READ_RETRY_DELAY_IN_SECONDS = 15;

  private PublishSendNotificationPaymentEventActivity publishSendNotificationPaymentEventActivity;
  private GetSendStreamActivity getSendStreamActivity;
  private GetSendNotificationEventsFromStreamActivity getSendNotificationEventsFromStreamActivity;
  private UpdateSendNotificationStatusActivity updateSendNotificationStatusActivity;
  private SendNotificationDateRetrieveActivity sendNotificationDateRetrieveActivity;
  private GetSendNotificationActivity getSendNotificationActivity;
  /**
   * Temporal workflow will not allow to use injection in order to avoid <a href="https://docs.temporal.io/workflows#non-deterministic-change">non-deterministic changes</a> due to dynamic reconfiguration.<BR />
   * Anyway it allows to override ActivityOptions, but actually it's not supporting the override based on the particular workflow.<BR />
   * In {@link TemporalWFImplementationCustomizer} we are already setting defaults to all workflows.<BR />
   * Use this as an example to override based on the particular workflow.
   */
  @Override
  public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
    SendNotificationProcessWfConfig wfConfig = applicationContext.getBean(SendNotificationProcessWfConfig.class);

    publishSendNotificationPaymentEventActivity = wfConfig.buildPublishSendNotificationPaymentEventActivityStub();
    getSendNotificationEventsFromStreamActivity = wfConfig.buildGetSendNotificationEventsFromStreamActivityStub();
    updateSendNotificationStatusActivity = wfConfig.buildUpdateSendNotificationStatusActivityStub();
    sendNotificationDateRetrieveActivity = wfConfig.buildSendNotificationDateRetrieveActivityStub();
    getSendStreamActivity = wfConfig.buildGetSendStreamActivityStub();
  }

  @Override
  public void readSendStream(String sendStreamId) {
    log.info("Start readSendStream Workflow for sendStreamId {}.", sendStreamId);

    SendStreamDTO sendStreamDTO = getSendStreamActivity.fetchSendStream(sendStreamId);
    if(sendStreamDTO == null) {
      log.error("Cannot read stream: SEND stream non found for sendStreamId {}", sendStreamId);
      return;
    }

    AtomicInteger activityExecutionCount = new AtomicInteger(1);
    AtomicInteger workflowExecutionErrorCount = new AtomicInteger(0);
    String lastProcessedEventId = sendStreamDTO.getLastEventId(); //start reading after latest processed event
    List<ProgressResponseElementV25DTO> streamEvents;

    do {
      try {
        activityExecutionCount.incrementAndGet();
        streamEvents = this.getSendNotificationEventsFromStreamActivity.fetchSendNotificationEventsFromStream(
          sendStreamDTO.getOrganizationId(),
          sendStreamId,
          lastProcessedEventId
        );
      } catch (Exception e) {
        log.warn("Cannot read new stream event batch: for sendStreamId {} and organizationId {} last read event has id {}; will retry in {} seconds",
          sendStreamId,
          sendStreamDTO.getOrganizationId(),
          lastProcessedEventId,
          calculateNextRetryDelay(workflowExecutionErrorCount.incrementAndGet())
        );
        waitForNextIteration(sendStreamId, workflowExecutionErrorCount.get(), activityExecutionCount);
        continue;
      }
      for (ProgressResponseElementV25DTO streamEvent : streamEvents) {
        try {
          String lastEventIdProcessedInCurrentBatch = processSendStreamEvent(sendStreamId, streamEvent, activityExecutionCount);
          if(lastEventIdProcessedInCurrentBatch != null)
            lastProcessedEventId = lastEventIdProcessedInCurrentBatch;
        } catch (Exception e) {
          log.warn("Cannot complete reading stream event batch: for sendStreamId {} and organizationId {} read until event with id {}; will retry in {} seconds",
            sendStreamId,
            sendStreamDTO.getOrganizationId(),
            lastProcessedEventId,
            calculateNextRetryDelay(workflowExecutionErrorCount.incrementAndGet())
          );
          break;
        }
      }
      waitForNextIteration(sendStreamId, workflowExecutionErrorCount.get(), activityExecutionCount);
      activityExecutionCount.getAndIncrement(); //incrementing for activity in do-while condition
    } while (getSendStreamActivity.fetchSendStream(sendStreamId) != null);

    log.info("Stopped readSendStream Workflow for sendStreamId {}, because SEND stream has been closed.", sendStreamId);
  }

  private static void waitForNextIteration(String workflowId, int workflowExecutionErrorCount, AtomicInteger activityExecutionCount) {
    int sleepDelay = workflowExecutionErrorCount != 0 ?
      calculateNextRetryDelay(workflowExecutionErrorCount) :
      SEND_NOTIFICATION_STREAM_CONSUMER_READ_BASE_DELAY_IN_SECONDS;
    Workflow.sleep(Duration.of(sleepDelay, ChronoUnit.SECONDS));
    if(activityExecutionCount.get() >= ACTIVITY_EXECUTIONS_BEFORE_CLEAN_WF_HISTORY) {
      activityExecutionCount.set(0);
      Workflow.continueAsNew(workflowId);
    }
  }

  private static int calculateNextRetryDelay(int workflowExecutionErrorCount) {
    return SEND_NOTIFICATION_STREAM_CONSUMER_READ_RETRY_DELAY_IN_SECONDS * workflowExecutionErrorCount;
  }

  private String processSendStreamEvent(String sendStreamId, ProgressResponseElementV25DTO streamEvent, AtomicInteger activitiesCount) {
    return switch (streamEvent.getNewStatus()) {
      case ACCEPTED, REFUSED -> {
        activitiesCount.incrementAndGet();
        SendNotificationDTO sendNotification = this.updateSendNotificationStatusActivity.updateSendNotificationStatus(streamEvent.getNotificationRequestId());
        if (NotificationStatus.ACCEPTED.equals(sendNotification.getStatus())) {
          this.publishSendEvent(sendNotification, new PaymentEventRequestDTO(PaymentEventType.SEND_NOTIFICATION_CREATED, null), activitiesCount);
        } else {
          this.publishSendErrorEvent(sendNotification, new PaymentEventRequestDTO(PaymentEventType.SEND_NOTIFICATION_ERROR, null), activitiesCount);
        }
        yield streamEvent.getEventId();
      }
      case VIEWED -> {
        activitiesCount.incrementAndGet();
        this.sendNotificationDateRetrieveActivity.sendNotificationDateRetrieve(streamEvent.getNotificationRequestId());
        yield streamEvent.getEventId();
      }
      case null -> {
        log.warn("Skipping event with status 'null' for SEND stream with id {}", sendStreamId);
        yield null;
      }
      default -> {
        log.warn("Skipping event with status {} for SEND stream with id {}", streamEvent.getNewStatus(), sendStreamId);
        yield null;
      }
    };
  }

  private void publishSendEvent(SendNotificationDTO sendNotificationDTO, PaymentEventRequestDTO eventRequestDTO, AtomicInteger activitiesCount) {
    SendNotification2DebtPositionSendNotificationsMapper.map(sendNotificationDTO)
      .forEach(p -> {
        activitiesCount.incrementAndGet();
        publishSendNotificationPaymentEventActivity.publishSendNotificationEvent(p, eventRequestDTO);
      });
  }

  private void publishSendErrorEvent(SendNotificationDTO sendNotificationDTO, PaymentEventRequestDTO eventRequestDTO, AtomicInteger activitiesCount) {
    SendNotification2DebtPositionSendNotificationsMapper.map(sendNotificationDTO)
      .forEach(p -> {
        activitiesCount.incrementAndGet();
        publishSendNotificationPaymentEventActivity.publishSendNotificationErrorEvent(p, eventRequestDTO);
      });
  }

}
