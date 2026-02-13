package it.gov.pagopa.pu.workflow.wf.pagopa.send.wfsendnotification;

import io.temporal.spring.boot.WorkflowImpl;
import io.temporal.workflow.Workflow;
import it.gov.pagopa.payhub.activities.activity.sendnotification.*;
import it.gov.pagopa.pu.sendnotification.dto.generated.ProgressResponseElementV25DTO;
import it.gov.pagopa.pu.sendnotification.dto.generated.SendStreamDTO;
import it.gov.pagopa.pu.workflow.config.temporal.TemporalWFImplementationCustomizer;
import it.gov.pagopa.pu.workflow.exception.custom.WorkflowInternalErrorException;
import it.gov.pagopa.pu.workflow.utilities.Utilities;
import it.gov.pagopa.pu.workflow.wf.pagopa.send.service.SendEventStreamProcessingService;
import it.gov.pagopa.pu.workflow.wf.pagopa.send.service.SendEventStreamProcessingServiceImpl;
import it.gov.pagopa.pu.workflow.utilities.TaskQueueConstants;
import it.gov.pagopa.pu.workflow.wf.pagopa.send.config.SendNotificationProcessWfConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.util.CollectionUtils;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Slf4j
@WorkflowImpl(taskQueues = TaskQueueConstants.TASK_QUEUE_SEND_RESERVED_STREAM)
public class SendNotificationStreamConsumeWFImpl implements SendNotificationStreamConsumeWF, ApplicationContextAware {

  private static final int LOOP_EXECUTIONS_BEFORE_CLEAN_WF_HISTORY = 100;
  private static final int WAITING_SECONDS_NEXT_POLL = 5 * 60;

  private int loopExecutionCount = 0;

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
      wfConfig.buildPublishSendNotificationPaymentEventActivityStub(),
      wfConfig.buildFetchSendLegalFactActivityStub()
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
    do {
      try {
        List<ProgressResponseElementV25DTO> streamEvents = this.getSendNotificationEventsFromStreamActivity.fetchSendNotificationEventsFromStream(
          sendStreamDTO.getOrganizationId(),
          sendStreamId,
          lastProcessedEventId
        );
        if (!CollectionUtils.isEmpty(streamEvents)) {
          lastProcessedEventId = processingStreamEvents(sendStreamId, streamEvents, lastProcessedEventId);
        }
      } catch(Throwable t) {
        log.error("Something went wrong processing stream {}: {}",
          sendStreamId, Utilities.getWorkflowExceptionMessage(t));
      }
      waitForNextIteration(sendStreamId);
    } while (isStreamStillOpened(sendStreamId));

    log.info("Stopped readSendStream Workflow for sendStreamId {}, because SEND stream has been closed.", sendStreamId);
  }

  private String processingStreamEvents(String sendStreamId, List<ProgressResponseElementV25DTO> streamEvents, String lastProcessedEventId) {
    for (ProgressResponseElementV25DTO streamEvent : streamEvents) {
      String lastEventId = sendEventStreamProcessingService.processSendStreamEvent(sendStreamId, streamEvent);
      if(lastEventId != null) lastProcessedEventId = lastEventId;
    }
    return lastProcessedEventId;
  }

  private boolean isStreamStillOpened(String sendStreamId) {
    try {
      return getSendStreamActivity.fetchSendStream(sendStreamId) != null;
    } catch (Exception e) {
      log.error("STREAMS_NOT_FOUND] Cannot fetch stream: SEND stream non found for sendStreamId {}", sendStreamId);
      throw new WorkflowInternalErrorException("[SEND_STATUS_ERROR] Workflow terminated during isStreamStillOpened for sendStreamId " + sendStreamId + " with ERROR: " + e.getMessage());
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
