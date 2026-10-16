package it.gov.pagopa.pu.workflow.wf.pagopa.send.stream.activity;

import io.temporal.activity.Activity;
import io.temporal.activity.ActivityExecutionContext;
import io.temporal.activity.ActivityInfo;
import it.gov.pagopa.pu.sendnotification.dto.generated.NotificationStatus;
import it.gov.pagopa.pu.sendnotification.dto.generated.ProgressResponseElementV28DTO;
import it.gov.pagopa.pu.sendnotification.dto.generated.SendNotificationDTO;
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

import java.time.OffsetDateTime;
import java.util.ArrayList;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PublishSendTimelineEventActivityImplTest {

  public static final long ORGANIZATION_ID = 1L;
  public static final String WORKFLOW_ID = "workflowId";
  public static final String SEND_STREAM_ID = "sendStreamId";
  public static final String CAMPAIGN_ID = "campaignId";
  public static final String SEND_NOTIFICATION_ID = "sendNotificationId";
  public static final String SUBJECT = "NOTIFICATION SUBJECT";
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
    SendNotificationDTO sendNotificationDTO = buildSendNotification();

    when(sendTimelineRegistryEventMapperMock.mapSuccess(event, sendNotificationDTO, SEND_STREAM_ID, WORKFLOW_ID, TRACE_ID))
      .thenReturn(registryEvent);
    ActivityInfo activityInfo = mock(ActivityInfo.class);
    when(activityInfo.getWorkflowId()).thenReturn(WORKFLOW_ID);
    ActivityExecutionContext activityExecutionContext = mock(ActivityExecutionContext.class);
    when(activityExecutionContext.getInfo()).thenReturn(activityInfo);

    try (MockedStatic<Activity> activityMock = Mockito.mockStatic(Activity.class)) {
      activityMock.when(Activity::getExecutionContext).thenReturn(activityExecutionContext);
      //WHEN
      publishSendTimelineEventActivityImpl.publishSendTimelineEvent(event, sendNotificationDTO, SEND_STREAM_ID, TRACE_ID);

      //THEN
      verify(sendTimelineProducerServiceMock)
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
    SendNotificationDTO sendNotificationDTO = buildSendNotification();

    when(sendTimelineRegistryEventMapperMock.mapError(event, sendNotificationDTO, SEND_STREAM_ID, WORKFLOW_ID, TRACE_ID))
      .thenReturn(registryEvent);
    ActivityInfo activityInfo = mock(ActivityInfo.class);
    when(activityInfo.getWorkflowId()).thenReturn(WORKFLOW_ID);
    ActivityExecutionContext activityExecutionContext = mock(ActivityExecutionContext.class);
    when(activityExecutionContext.getInfo()).thenReturn(activityInfo);

    try (MockedStatic<Activity> activityMock = Mockito.mockStatic(Activity.class)) {
      activityMock.when(Activity::getExecutionContext).thenReturn(activityExecutionContext);
      //WHEN
      publishSendTimelineEventActivityImpl.publishSendTimelineErrorEvent(event, sendNotificationDTO, sendStreamId, traceId);

      //THEN
      verify(sendTimelineProducerServiceMock)
              .notifySendTimelineEvent(
                      registryEvent,
                      sendStreamId
              );
    }
  }

  private SendNotificationDTO buildSendNotification() {
    return SendNotificationDTO.builder()
      .organizationId(ORGANIZATION_ID)
      .campaignId(CAMPAIGN_ID)
      .sendNotificationId(SEND_NOTIFICATION_ID)
      .status(NotificationStatus.SENDING)
      .payments(new ArrayList<>())
      .subject(SUBJECT)
      .build();
  }

  @Test
  void testPublishPaymentTimelineEvent() {
    // GIVEN
    SendNotificationDTO sendNotificationDTO = buildSendNotification();
    OffsetDateTime paymentDate = OffsetDateTime.now();
    String sendStreamId = String.valueOf(ORGANIZATION_ID);
    RegistryEventSendTimelineDTO registryEvent = new RegistryEventSendTimelineDTO();

    when(sendTimelineRegistryEventMapperMock.mapPayment(sendNotificationDTO, paymentDate, sendStreamId, WORKFLOW_ID, TRACE_ID))
      .thenReturn(registryEvent);

    ActivityInfo activityInfo = mock(ActivityInfo.class);
    when(activityInfo.getWorkflowId()).thenReturn(WORKFLOW_ID);

    ActivityExecutionContext activityExecutionContext = mock(ActivityExecutionContext.class);
    when(activityExecutionContext.getInfo()).thenReturn(activityInfo);

    try (MockedStatic<Activity> activityMock = mockStatic(Activity.class)) {
      activityMock.when(Activity::getExecutionContext)
        .thenReturn(activityExecutionContext);

      // WHEN
      publishSendTimelineEventActivityImpl.publishPaymentTimelineEvent(
        sendNotificationDTO, paymentDate, TRACE_ID);

      // THEN
      verify(sendTimelineRegistryEventMapperMock).mapPayment(sendNotificationDTO, paymentDate, sendStreamId, WORKFLOW_ID, TRACE_ID);
      verify(sendTimelineProducerServiceMock).notifySendTimelineEvent(registryEvent, sendStreamId);
    }
  }
}
