package it.gov.pagopa.pu.workflow.wf.pagopa.send.stream.wf;

import io.temporal.spring.boot.WorkflowImpl;
import io.temporal.workflow.ChildWorkflowOptions;
import io.temporal.workflow.Workflow;
import it.gov.pagopa.payhub.activities.activity.sendnotification.stream.GetSendNotificationEventsFromStreamActivity;
import it.gov.pagopa.payhub.activities.activity.sendnotification.stream.GetSendStreamActivity;
import it.gov.pagopa.payhub.activities.activity.sendnotification.stream.UpdateLastProcessedStreamEventIdActivity;
import it.gov.pagopa.payhub.activities.exception.common.RestInvokeNotFoundException;
import it.gov.pagopa.pu.sendnotification.dto.generated.*;
import it.gov.pagopa.pu.workflow.config.temporal.TemporalWFImplementationCustomizer;
import it.gov.pagopa.pu.workflow.dto.SendStreamEventsDTO;
import it.gov.pagopa.pu.workflow.exception.custom.IllegalStateBusinessException;
import it.gov.pagopa.pu.workflow.utilities.ErrorCodeConstants;
import it.gov.pagopa.pu.workflow.utilities.TaskQueueConstants;
import it.gov.pagopa.pu.workflow.utilities.Utilities;
import it.gov.pagopa.pu.workflow.wf.pagopa.send.stream.config.SendNotificationStreamWfConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.util.CollectionUtils;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static it.gov.pagopa.pu.workflow.utilities.Utilities.generateWorkflowId;

@Slf4j
@WorkflowImpl(taskQueues = TaskQueueConstants.TASK_QUEUE_SEND_RESERVED_STREAM)
public class SendNotificationStreamConsumerWFImpl implements SendNotificationStreamConsumerWF, ApplicationContextAware {

  private static final int LOOP_EXECUTIONS_BEFORE_CLEAN_WF_HISTORY = 100;
  private static final int WAITING_SECONDS_NEXT_POLL = 5 * 60;

  private int loopExecutionCount = 0;

  private GetSendStreamActivity getSendStreamActivity;
  private GetSendNotificationEventsFromStreamActivity getSendNotificationEventsFromStreamActivity;
  private UpdateLastProcessedStreamEventIdActivity updateLastProcessedStreamEventIdActivity;

  /**
   * Temporal workflow will not allow to use injection in order to avoid <a href="https://docs.temporal.io/workflows#non-deterministic-change">non-deterministic changes</a> due to dynamic reconfiguration.<BR />
   * Anyway it allows to override ActivityOptions, but actually it's not supporting the override based on the particular workflow.<BR />
   * In {@link TemporalWFImplementationCustomizer} we are already setting defaults to all workflows.<BR />
   * Use this as an example to override based on the particular workflow.
   */
  @Override
  public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
    SendNotificationStreamWfConfig wfConfig = applicationContext.getBean(SendNotificationStreamWfConfig.class);

    getSendNotificationEventsFromStreamActivity = wfConfig.buildGetSendNotificationEventsFromStreamActivityStub();
    getSendStreamActivity = wfConfig.buildGetSendStreamActivityStub();
    updateLastProcessedStreamEventIdActivity = wfConfig.buildUpdateLastProcessedStreamEventIdActivityStub();
  }

  @Override
  public void readSendStream(String sendStreamId) {
    log.info("Start readSendStream Workflow for sendStreamId {}.", sendStreamId);

    SendStreamDTO sendStreamDTO = getSendStreamActivity.fetchSendStream(sendStreamId);
    if(sendStreamDTO == null) {
      log.error("[STREAMS_NOT_FOUND] Cannot fetch stream: SEND stream non found for sendStreamId {}", sendStreamId);
      throw new IllegalStateBusinessException(ErrorCodeConstants.ERROR_CODE_SEND_STATUS_ERROR, "Workflow terminated during starting of readSendStream for sendStreamId %s with ERROR: cannot found SEND stream.".formatted(sendStreamId));
    }

    String lastProcessedEventId = sendStreamDTO.getLastEventId();
    do {
      try {
        List<ProgressResponseElementV28DTO> streamEvents = this.getSendNotificationEventsFromStreamActivity.fetchSendNotificationEventsFromStream(
          sendStreamDTO.getOrganizationId(),
          sendStreamId
        );
        if (!CollectionUtils.isEmpty(streamEvents)) {
          SendNotificationEventsConsumerWF sendNotificationEventsConsumerWF = stubChildWorkflow(sendStreamId);
          SendStreamEventsDTO childWorkflowInput = buildChildWorkflowInput(sendStreamDTO.getOrganizationId(), sendStreamId, streamEvents);
          lastProcessedEventId = sendNotificationEventsConsumerWF.processingStreamEvents(childWorkflowInput);
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

  private SendNotificationEventsConsumerWF stubChildWorkflow(String sendStreamId) {
    ChildWorkflowOptions childWorkflowOptions = ChildWorkflowOptions.newBuilder()
      .setWorkflowId(generateWorkflowId(sendStreamId, SendNotificationEventsConsumerWF.class))
      .build();
    return Workflow.newChildWorkflowStub(
      SendNotificationEventsConsumerWF.class,
      childWorkflowOptions);
  }

  private SendStreamEventsDTO buildChildWorkflowInput(Long organizationId, String sendStreamId, List<ProgressResponseElementV28DTO> streamEvents) {
    return SendStreamEventsDTO.builder()
      .organizationId(organizationId)
      .sendStreamId(sendStreamId)
      .streamEventBatch(streamEvents)
      .build();
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
    } catch (RestInvokeNotFoundException e) {
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
