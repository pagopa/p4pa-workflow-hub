package it.gov.pagopa.pu.workflow.wf.pagopa.send.stream.wf;

import io.temporal.failure.ActivityFailure;
import io.temporal.failure.ApplicationFailure;
import it.gov.pagopa.payhub.activities.activity.sendnotification.stream.NotifySendNotificationStreamEventsActivity;
import it.gov.pagopa.payhub.activities.exception.NotRetryableActivityException;
import it.gov.pagopa.payhub.activities.exception.RetryableActivityException;
import it.gov.pagopa.payhub.activities.exception.sendnotification.SendStreamSkippedEventException;
import it.gov.pagopa.pu.sendnotification.dto.generated.*;
import it.gov.pagopa.pu.workflow.dto.SendStreamEventsProcessWFInputDTO;
import it.gov.pagopa.pu.workflow.wf.pagopa.send.create.config.SendNotificationProcessWfConfig;
import it.gov.pagopa.pu.workflow.wf.pagopa.send.stream.activity.PublishSendTimelineEventActivity;
import it.gov.pagopa.pu.workflow.wf.pagopa.send.stream.config.SendNotificationStreamWfConfig;
import it.gov.pagopa.pu.workflow.wf.pagopa.send.stream.service.SendEventStreamProcessingService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationContext;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.*;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SendNotificationStreamConsumeChildWFImplTest {

  public static final long ORGANIZATION_ID = 1L;
  public static final String NOTIFICATION_REQUEST_ID_1 = "notificationRequestId1";
  public static final String NOTIFICATION_REQUEST_ID_2 = "notificationRequestId2";
  public static final String SEND_STREAM_ID = "sendStreamId";

  @Mock
  private SendEventStreamProcessingService sendEventStreamProcessingServiceMock;
  @Mock
  private PublishSendTimelineEventActivity publishSendTimelineEventActivityMock;
  @Mock
  private NotifySendNotificationStreamEventsActivity notifySendNotificationStreamEventsActivityMock;

  private SendNotificationStreamConsumeChildWFImpl wf;

  @BeforeEach
  void setUp() {
    SendNotificationStreamWfConfig wfConfigMock = mock(SendNotificationStreamWfConfig.class);
    SendNotificationProcessWfConfig wfSendProcessConfigMock = mock(SendNotificationProcessWfConfig.class);
    ApplicationContext applicationContextMock = mock(ApplicationContext.class);

    when(wfConfigMock.buildPublishSendTimelineEventActivityStub()).thenReturn(publishSendTimelineEventActivityMock);
    when(wfConfigMock.buildNotifySendNotificationStreamEventsActivityStub()).thenReturn(notifySendNotificationStreamEventsActivityMock);

    when(applicationContextMock.getBean(SendNotificationStreamWfConfig.class)).thenReturn(wfConfigMock);

    when(applicationContextMock.getBean(SendNotificationProcessWfConfig.class)).thenReturn(wfSendProcessConfigMock);

    wf = new SendNotificationStreamConsumeChildWFImpl();
    wf.setApplicationContext(applicationContextMock);
    ReflectionTestUtils.setField(wf, "sendEventStreamProcessingService", sendEventStreamProcessingServiceMock);
  }

  @AfterEach
  void verifyNoMoreInteractions() {
    Mockito.verifyNoMoreInteractions(
      sendEventStreamProcessingServiceMock,
      publishSendTimelineEventActivityMock,
      notifySendNotificationStreamEventsActivityMock
    );
  }

  @Test
  void givenEmptyStreamEventBatchWhenProcessingStreamEventsThenReturnNull() {
    //GIVEN
    List<ProgressResponseElementV28DTO> streamEvents = new ArrayList<>();

    SendStreamEventsProcessWFInputDTO wfInput = new SendStreamEventsProcessWFInputDTO(
      ORGANIZATION_ID,
      SEND_STREAM_ID,
      streamEvents
    );

    //WHEN
    String lastProcessedEventId = wf.processingStreamEvents(wfInput);

    //THEN
    Assertions.assertNull(lastProcessedEventId);
  }

  @Test
  void givenSendStreamSkippedEventExceptionEventWhenProcessingStreamEventsThenReturnEventId() {
    //GIVEN
    ProgressResponseElementV28DTO sendEvent = buildSendEvent("sendEventId", NotificationStatusV26DTO.DELIVERED);
    List<ProgressResponseElementV28DTO> streamEvents = List.of(
      sendEvent
    );

    SendStreamEventsProcessWFInputDTO wfInput = new SendStreamEventsProcessWFInputDTO(
      ORGANIZATION_ID,
      SEND_STREAM_ID,
      streamEvents
    );

    ActivityFailure activityFailureMock = mock(ActivityFailure.class);
    when(activityFailureMock.getCause())
      .thenReturn(ApplicationFailure.newNonRetryableFailure("error", SendStreamSkippedEventException.class.getName()));

    when(sendEventStreamProcessingServiceMock.processSendStreamEvent(
      Mockito.eq(SEND_STREAM_ID),
      Mockito.isA(ProgressResponseElementV28DTO.class)
    )).thenThrow(activityFailureMock);

    //WHEN
    String lastProcessedEventId = wf.processingStreamEvents(wfInput);

    //THEN
    Assertions.assertNotNull(lastProcessedEventId);
    Assertions.assertEquals(sendEvent.getEventId(), lastProcessedEventId);
  }

  @Test
  void givenGeneralNotRetryableActivityExceptionWhenProcessingStreamEventsThenSendEventToDeadLetterAndReturnEventId() {
    //GIVEN
    ProgressResponseElementV28DTO sendEvent = buildSendEvent("sendEventId", NotificationStatusV26DTO.ACCEPTED);
    List<ProgressResponseElementV28DTO> streamEvents = List.of(
      sendEvent
    );

    SendStreamEventsProcessWFInputDTO wfInput = new SendStreamEventsProcessWFInputDTO(
      ORGANIZATION_ID,
      SEND_STREAM_ID,
      streamEvents
    );

    ActivityFailure activityFailureMock = mock(ActivityFailure.class);
    when(activityFailureMock.getCause())
      .thenReturn(ApplicationFailure.newNonRetryableFailure("error", NotRetryableActivityException.class.getName()));

    when(sendEventStreamProcessingServiceMock.processSendStreamEvent(
      Mockito.eq(SEND_STREAM_ID),
      Mockito.isA(ProgressResponseElementV28DTO.class)
    )).thenThrow(activityFailureMock);

    doNothing()
      .when(publishSendTimelineEventActivityMock).publishSendTimelineErrorEvent(
        Mockito.isA(ProgressResponseElementV28DTO.class),
        Mockito.eq(ORGANIZATION_ID),
        Mockito.eq(SEND_STREAM_ID),
        Mockito.isNull()
      );

    //WHEN
    String lastProcessedEventId = wf.processingStreamEvents(wfInput);

    //THEN
    Assertions.assertNotNull(lastProcessedEventId);
    Assertions.assertEquals(sendEvent.getEventId(), lastProcessedEventId);
    verify(publishSendTimelineEventActivityMock, times(1)).publishSendTimelineErrorEvent(
      Mockito.isA(ProgressResponseElementV28DTO.class),
      Mockito.eq(ORGANIZATION_ID),
      Mockito.eq(SEND_STREAM_ID),
      Mockito.isNull()
    );
  }

  @Test
  void givenGeneralRetryableActivityFailureWhenProcessingStreamEventsThenSendEventToDeadLetterAndReturnEventId() {
    //GIVEN
    ProgressResponseElementV28DTO sendEvent = buildSendEvent("sendEventId", NotificationStatusV26DTO.ACCEPTED);
    List<ProgressResponseElementV28DTO> streamEvents = List.of(
      sendEvent
    );

    SendStreamEventsProcessWFInputDTO wfInput = new SendStreamEventsProcessWFInputDTO(
      ORGANIZATION_ID,
      SEND_STREAM_ID,
      streamEvents
    );

    ActivityFailure activityFailureMock = mock(ActivityFailure.class);
    when(activityFailureMock.getCause())
      .thenReturn(ApplicationFailure.newFailure("error", RetryableActivityException.class.getName()));

    when(sendEventStreamProcessingServiceMock.processSendStreamEvent(
      Mockito.eq(SEND_STREAM_ID),
      Mockito.isA(ProgressResponseElementV28DTO.class)
    )).thenThrow(activityFailureMock);

    doNothing()
      .when(publishSendTimelineEventActivityMock).publishSendTimelineErrorEvent(
        Mockito.isA(ProgressResponseElementV28DTO.class),
        Mockito.eq(ORGANIZATION_ID),
        Mockito.eq(SEND_STREAM_ID),
        Mockito.isNull()
      );

    //WHEN
    String lastProcessedEventId = wf.processingStreamEvents(wfInput);

    //THEN
    Assertions.assertNotNull(lastProcessedEventId);
    Assertions.assertEquals(sendEvent.getEventId(), lastProcessedEventId);
    verify(publishSendTimelineEventActivityMock, times(1)).publishSendTimelineErrorEvent(
      Mockito.isA(ProgressResponseElementV28DTO.class),
      Mockito.eq(ORGANIZATION_ID),
      Mockito.eq(SEND_STREAM_ID),
      Mockito.isNull()
    );
  }

  @Test
  void givenGeneralActivityFailureFailureWhenProcessingStreamEventsThenSendEventToDeadLetterAndReturnEventId() {
    //GIVEN
    ProgressResponseElementV28DTO sendEvent = buildSendEvent("sendEventId", NotificationStatusV26DTO.ACCEPTED);
    List<ProgressResponseElementV28DTO> streamEvents = List.of(
      sendEvent
    );

    SendStreamEventsProcessWFInputDTO wfInput = new SendStreamEventsProcessWFInputDTO(
      ORGANIZATION_ID,
      SEND_STREAM_ID,
      streamEvents
    );

    ActivityFailure activityFailureMock = mock(ActivityFailure.class);
    when(activityFailureMock.getCause())
      .thenReturn(new RuntimeException());

    when(sendEventStreamProcessingServiceMock.processSendStreamEvent(
      Mockito.eq(SEND_STREAM_ID),
      Mockito.isA(ProgressResponseElementV28DTO.class)
    )).thenThrow(activityFailureMock);

    doNothing()
      .when(publishSendTimelineEventActivityMock).publishSendTimelineErrorEvent(
        Mockito.isA(ProgressResponseElementV28DTO.class),
        Mockito.eq(ORGANIZATION_ID),
        Mockito.eq(SEND_STREAM_ID),
        Mockito.isNull()
      );

    //WHEN
    String lastProcessedEventId = wf.processingStreamEvents(wfInput);

    //THEN
    Assertions.assertNotNull(lastProcessedEventId);
    Assertions.assertEquals(sendEvent.getEventId(), lastProcessedEventId);
    verify(publishSendTimelineEventActivityMock, times(1)).publishSendTimelineErrorEvent(
      Mockito.isA(ProgressResponseElementV28DTO.class),
      Mockito.eq(ORGANIZATION_ID),
      Mockito.eq(SEND_STREAM_ID),
      Mockito.isNull()
    );
  }

  @Test
  void givenNonActivityFailureExceptionWhenProcessingStreamEventsThenSendEventToDeadLetterAndReturnEventId() {
    //GIVEN
    ProgressResponseElementV28DTO sendEvent = buildSendEvent("sendEventId", NotificationStatusV26DTO.ACCEPTED);
    List<ProgressResponseElementV28DTO> streamEvents = List.of(
      sendEvent
    );

    SendStreamEventsProcessWFInputDTO wfInput = new SendStreamEventsProcessWFInputDTO(
      ORGANIZATION_ID,
      SEND_STREAM_ID,
      streamEvents
    );

    RuntimeException generalException = mock(RuntimeException.class);

    when(sendEventStreamProcessingServiceMock.processSendStreamEvent(
      Mockito.eq(SEND_STREAM_ID),
      Mockito.isA(ProgressResponseElementV28DTO.class)
    )).thenThrow(generalException);

    doNothing()
      .when(publishSendTimelineEventActivityMock).publishSendTimelineErrorEvent(
        Mockito.isA(ProgressResponseElementV28DTO.class),
        Mockito.eq(ORGANIZATION_ID),
        Mockito.eq(SEND_STREAM_ID),
        Mockito.isNull()
      );

    //WHEN
    String lastProcessedEventId = wf.processingStreamEvents(wfInput);

    //THEN
    Assertions.assertNotNull(lastProcessedEventId);
    Assertions.assertEquals(sendEvent.getEventId(), lastProcessedEventId);
    verify(publishSendTimelineEventActivityMock, times(1)).publishSendTimelineErrorEvent(
      Mockito.isA(ProgressResponseElementV28DTO.class),
      Mockito.eq(ORGANIZATION_ID),
      Mockito.eq(SEND_STREAM_ID),
      Mockito.isNull()
    );
  }

  @Test
  void givenNoExceptionEventWhenProcessingStreamEventsThenNotifySendNotificationStreamEvents() {
    //GIVEN
    ProgressResponseElementV28DTO sendEvent1 = buildSendEvent("sendEventId1", NOTIFICATION_REQUEST_ID_1, NotificationStatusV26DTO.ACCEPTED, TimelineElementCategoryV27DTO.REQUEST_ACCEPTED);
    ProgressResponseElementV28DTO sendEvent2 = buildSendEvent("sendEventId2", NOTIFICATION_REQUEST_ID_1, NotificationStatusV26DTO.DELIVERING, TimelineElementCategoryV27DTO.SEND_ANALOG_PROGRESS);
    ProgressResponseElementV28DTO sendEvent3 = buildSendEvent("sendEventId3", NOTIFICATION_REQUEST_ID_2, NotificationStatusV26DTO.ACCEPTED, TimelineElementCategoryV27DTO.REQUEST_ACCEPTED);
    ProgressResponseElementV28DTO sendEvent4 = buildSendEvent("sendEventId4", NOTIFICATION_REQUEST_ID_2, NotificationStatusV26DTO.DELIVERING, TimelineElementCategoryV27DTO.ANALOG_FAILURE_WORKFLOW);
    List<ProgressResponseElementV28DTO> streamEvents = List.of(
      sendEvent1,
      sendEvent2,
      sendEvent3,
      sendEvent4
    );

    SendStreamEventsProcessWFInputDTO wfInput = new SendStreamEventsProcessWFInputDTO(
      ORGANIZATION_ID,
      SEND_STREAM_ID,
      streamEvents
    );

    Map<String, List<StreamEventSummaryDTO>> expectedNotificationRequestIdToStreamEventsMap = new HashMap<>();
    expectedNotificationRequestIdToStreamEventsMap.put(
      NOTIFICATION_REQUEST_ID_1, List.of(
        new StreamEventSummaryDTO(sendEvent1.getNewStatus(), sendEvent1.getElement().getCategory()),
        new StreamEventSummaryDTO(sendEvent2.getNewStatus(), sendEvent2.getElement().getCategory())
      )
    );
    expectedNotificationRequestIdToStreamEventsMap.put(
      NOTIFICATION_REQUEST_ID_2, List.of(
        new StreamEventSummaryDTO(sendEvent3.getNewStatus(), sendEvent3.getElement().getCategory()),
        new StreamEventSummaryDTO(sendEvent4.getNewStatus(), sendEvent4.getElement().getCategory())
      )
    );

    ArgumentCaptor<Map<String, List<StreamEventSummaryDTO>>> notificationRequestIdToStreamEventsMapCaptor = ArgumentCaptor.captor();

    when(sendEventStreamProcessingServiceMock.processSendStreamEvent(
      Mockito.eq(SEND_STREAM_ID),
      Mockito.isA(ProgressResponseElementV28DTO.class)
    )).thenReturn(sendEvent1.getEventId())
      .thenReturn(sendEvent2.getEventId())
      .thenReturn(sendEvent3.getEventId())
      .thenReturn(sendEvent4.getEventId());

    doNothing()
      .when(publishSendTimelineEventActivityMock).publishSendTimelineEvent(
        Mockito.isA(ProgressResponseElementV28DTO.class),
        Mockito.eq(ORGANIZATION_ID),
        Mockito.eq(SEND_STREAM_ID),
        Mockito.isNull()
      );

    doNothing()
      .when(notifySendNotificationStreamEventsActivityMock).notifySendNotificationStreamEvents(
        notificationRequestIdToStreamEventsMapCaptor.capture()
      );

    //WHEN
    String lastProcessedEventId = wf.processingStreamEvents(wfInput);

    //THEN
    Assertions.assertNotNull(lastProcessedEventId);
    Assertions.assertEquals(sendEvent4.getEventId(), lastProcessedEventId);
    verify(publishSendTimelineEventActivityMock, times(4))
      .publishSendTimelineEvent(
        Mockito.isA(ProgressResponseElementV28DTO.class),
        Mockito.eq(ORGANIZATION_ID),
        Mockito.eq(SEND_STREAM_ID),
        Mockito.isNull()
      );
    Assertions.assertEquals(
      expectedNotificationRequestIdToStreamEventsMap,
      notificationRequestIdToStreamEventsMapCaptor.getValue()
    );
  }

  @Test
  void givenEventCategoryOrNewNotificationStatusNullWhenProcessingStreamEventsThenProcessButDoNotNotifySendNotificationStreamEvents() {
    //GIVEN
    ProgressResponseElementV28DTO sendEvent = buildSendEvent("sendEventId", NotificationStatusV26DTO.DELIVERING);
    List<ProgressResponseElementV28DTO> streamEvents = Collections.nCopies(101, sendEvent);

    SendStreamEventsProcessWFInputDTO wfInput = new SendStreamEventsProcessWFInputDTO(
      ORGANIZATION_ID,
      SEND_STREAM_ID,
      streamEvents
    );

    when(sendEventStreamProcessingServiceMock.processSendStreamEvent(
      Mockito.eq(SEND_STREAM_ID),
      Mockito.isA(ProgressResponseElementV28DTO.class)
    )).thenReturn(null);

    doNothing()
      .when(publishSendTimelineEventActivityMock).publishSendTimelineEvent(
        Mockito.isA(ProgressResponseElementV28DTO.class),
        Mockito.eq(ORGANIZATION_ID),
        Mockito.eq(SEND_STREAM_ID),
        Mockito.isNull()
      );

    //WHEN
    String lastProcessedEventId = wf.processingStreamEvents(wfInput);

    //THEN
    Assertions.assertNull(lastProcessedEventId);
    verify(publishSendTimelineEventActivityMock, times(100))
      .publishSendTimelineEvent(
        Mockito.isA(ProgressResponseElementV28DTO.class),
        Mockito.eq(ORGANIZATION_ID),
        Mockito.eq(SEND_STREAM_ID),
        Mockito.isNull()
      );
  }

  @Test
  void givenTooBigStreamEventBatchWhenProcessingStreamEventsThenProcessPartialBatch() {
    //GIVEN
    ProgressResponseElementV28DTO sendEvent1 = buildSendEvent("sendEventId1", NOTIFICATION_REQUEST_ID_1, null, TimelineElementCategoryV27DTO.REQUEST_ACCEPTED);
    ProgressResponseElementV28DTO sendEvent2 = buildSendEvent("sendEventId2", NOTIFICATION_REQUEST_ID_1, NotificationStatusV26DTO.DELIVERING, null);
    List<ProgressResponseElementV28DTO> streamEvents = List.of(
      sendEvent1,
      sendEvent2
    );

    SendStreamEventsProcessWFInputDTO wfInput = new SendStreamEventsProcessWFInputDTO(
      ORGANIZATION_ID,
      SEND_STREAM_ID,
      streamEvents
    );

    when(sendEventStreamProcessingServiceMock.processSendStreamEvent(
      Mockito.eq(SEND_STREAM_ID),
      Mockito.isA(ProgressResponseElementV28DTO.class)
    )).thenReturn(sendEvent1.getEventId())
      .thenReturn(sendEvent2.getEventId());

    doNothing()
      .when(publishSendTimelineEventActivityMock).publishSendTimelineEvent(
        Mockito.isA(ProgressResponseElementV28DTO.class),
        Mockito.eq(ORGANIZATION_ID),
        Mockito.eq(SEND_STREAM_ID),
        Mockito.isNull()
      );

    //WHEN
    String lastProcessedEventId = wf.processingStreamEvents(wfInput);

    //THEN
    Assertions.assertNotNull(lastProcessedEventId);
    Assertions.assertEquals(sendEvent2.getEventId(), lastProcessedEventId);
    verify(publishSendTimelineEventActivityMock, times(2))
      .publishSendTimelineEvent(
        Mockito.isA(ProgressResponseElementV28DTO.class),
        Mockito.eq(ORGANIZATION_ID),
        Mockito.eq(SEND_STREAM_ID),
        Mockito.isNull()
      );
  }

  private static ProgressResponseElementV28DTO buildSendEvent(String sendEventId, NotificationStatusV26DTO notificationStatus) {
    return buildSendEvent(sendEventId, NOTIFICATION_REQUEST_ID_1, notificationStatus, null);
  }

  private static ProgressResponseElementV28DTO buildSendEvent(String sendEventId, String notificationRequestId, NotificationStatusV26DTO notificationStatus, TimelineElementCategoryV27DTO category) {
    ProgressResponseElementV28DTO sendEvent = new ProgressResponseElementV28DTO();
    sendEvent.setNewStatus(notificationStatus);
    sendEvent.setEventId(sendEventId);
    sendEvent.setNotificationRequestId(notificationRequestId);
    sendEvent.setElement(buildSendEventElement(category));
    return sendEvent;
  }

  private static TimelineElementV27DTO buildSendEventElement(TimelineElementCategoryV27DTO category) {
    TimelineElementV27DTO timelineElement = new TimelineElementV27DTO();
    timelineElement.setCategory(category);
    return timelineElement;
  }


}
