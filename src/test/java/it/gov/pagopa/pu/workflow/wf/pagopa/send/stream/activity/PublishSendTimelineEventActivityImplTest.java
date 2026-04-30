package it.gov.pagopa.pu.workflow.wf.pagopa.send.stream.activity;

import io.temporal.activity.Activity;
import io.temporal.activity.ActivityExecutionContext;
import io.temporal.activity.ActivityInfo;
import it.gov.pagopa.pu.sendnotification.dto.generated.ProgressResponseElementV28DTO;
import it.gov.pagopa.pu.sendnotification.dto.generated.TimelineElementV27DTO;
import it.gov.pagopa.pu.workflow.event.registries.dto.RegistryEventSendTimelineDTO;
import it.gov.pagopa.pu.workflow.event.registries.producer.SendTimelineProducerService;
import it.gov.pagopa.pu.workflow.mapper.SendTimelineRegistryEventMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PublishSendTimelineEventActivityImplTest {

  public static final long ORGANIZATION_ID = 1L;
  public static final String WORKFLOW_ID = "workflowId";
  public static final String SEND_STREAM_ID = "sendStreamId";
  public static final String TRACE_ID = "traceId";

  @Mock
  private SendTimelineProducerService sendTimelineProducerServiceMock;
  @Mock
  private SendTimelineRegistryEventMapper sendTimelineRegistryEventMapperMock;

  @InjectMocks
  private PublishSendTimelineEventActivityImpl publishSendTimelineEventActivityImpl;

  @AfterEach
  void verifyNoMoreInteractions() {
    Mockito.verifyNoMoreInteractions(
      sendTimelineProducerServiceMock
    );
  }

  @Test
  void testPublishSendTimelineEvent() {
    //GIVEN
    ProgressResponseElementV28DTO event = new ProgressResponseElementV28DTO();
    event.setElement(new TimelineElementV27DTO());
    RegistryEventSendTimelineDTO registryEvent = new RegistryEventSendTimelineDTO();
    Mockito.when(sendTimelineRegistryEventMapperMock.mapSuccess(event, ORGANIZATION_ID, SEND_STREAM_ID, WORKFLOW_ID, TRACE_ID))
      .thenReturn(registryEvent);
    ActivityInfo activityInfo = Mockito.mock(ActivityInfo.class);
    Mockito.when(activityInfo.getWorkflowId()).thenReturn(WORKFLOW_ID);
    ActivityExecutionContext activityExecutionContext = Mockito.mock(ActivityExecutionContext.class);
    Mockito.when(activityExecutionContext.getInfo()).thenReturn(activityInfo);

    try (MockedStatic<Activity> activityMock = Mockito.mockStatic(Activity.class)) {
      activityMock.when(Activity::getExecutionContext).thenReturn(activityExecutionContext);
      //WHEN
      publishSendTimelineEventActivityImpl.publishSendTimelineEvent(event, ORGANIZATION_ID, SEND_STREAM_ID, TRACE_ID);

      //THEN
      Mockito.verify(sendTimelineProducerServiceMock)
        .notifySendTimelineEvent(
          registryEvent,
                SEND_STREAM_ID
        );
    }
  }

  @Test
  void testPublishSendTimelineErrorEvent() {
    //GIVEN
    String sendStreamId = "sendStreamId";
    String traceId = "traceId";
    ProgressResponseElementV28DTO event = new ProgressResponseElementV28DTO();
    event.setElement(new TimelineElementV27DTO());
    RegistryEventSendTimelineDTO registryEvent = new RegistryEventSendTimelineDTO();
    Mockito.when(sendTimelineRegistryEventMapperMock.mapError(event, ORGANIZATION_ID, SEND_STREAM_ID, WORKFLOW_ID, TRACE_ID))
      .thenReturn(registryEvent);
    ActivityInfo activityInfo = Mockito.mock(ActivityInfo.class);
    Mockito.when(activityInfo.getWorkflowId()).thenReturn(WORKFLOW_ID);
    ActivityExecutionContext activityExecutionContext = Mockito.mock(ActivityExecutionContext.class);
    Mockito.when(activityExecutionContext.getInfo()).thenReturn(activityInfo);

    try (MockedStatic<Activity> activityMock = Mockito.mockStatic(Activity.class)) {
      activityMock.when(Activity::getExecutionContext).thenReturn(activityExecutionContext);
      //WHEN
      publishSendTimelineEventActivityImpl.publishSendTimelineErrorEvent(event, ORGANIZATION_ID, sendStreamId, traceId);

      //THEN
      Mockito.verify(sendTimelineProducerServiceMock)
              .notifySendTimelineEvent(
                      registryEvent,
                      sendStreamId
              );
    }
  }
}
