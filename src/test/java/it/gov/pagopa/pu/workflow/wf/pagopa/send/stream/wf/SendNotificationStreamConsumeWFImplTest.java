package it.gov.pagopa.pu.workflow.wf.pagopa.send.stream.wf;

import io.temporal.failure.ActivityFailure;
import io.temporal.failure.ApplicationFailure;
import io.temporal.workflow.Workflow;
import it.gov.pagopa.payhub.activities.activity.sendnotification.stream.*;
import it.gov.pagopa.payhub.activities.exception.NotRetryableActivityException;
import it.gov.pagopa.payhub.activities.exception.RetryableActivityException;
import it.gov.pagopa.payhub.activities.exception.sendnotification.SendStreamSkippedEventException;
import it.gov.pagopa.pu.sendnotification.dto.generated.*;
import it.gov.pagopa.pu.workflow.exception.custom.IllegalStateBusinessException;
import it.gov.pagopa.pu.workflow.wf.pagopa.send.create.config.SendNotificationProcessWfConfig;
import it.gov.pagopa.pu.workflow.wf.pagopa.send.stream.activity.PublishSendTimelineEventActivity;
import it.gov.pagopa.pu.workflow.wf.pagopa.send.stream.activity.StartDeleteSendLegalFactFileActivity;
import it.gov.pagopa.pu.workflow.wf.pagopa.send.stream.activity.StartDeleteSendNotificationFileActivity;
import it.gov.pagopa.pu.workflow.wf.pagopa.send.stream.config.SendNotificationStreamWfConfig;
import it.gov.pagopa.pu.workflow.wf.pagopa.send.stream.service.SendEventStreamProcessingService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpStatus;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.HttpClientErrorException;

import java.time.Duration;
import java.util.*;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SendNotificationStreamConsumeWFImplTest {

  public static final long ORGANIZATION_ID = 1L;
  public static final String NOTIFICATION_REQUEST_ID_1 = "notificationRequestId1";
  public static final String NOTIFICATION_REQUEST_ID_2 = "notificationRequestId2";
  public static final String INVALID_SEND_STREAM_ID = "invalidSendStreamId";
  public static final String SEND_STREAM_ID = "sendStreamId";

  @Mock
  private GetSendStreamActivity getSendStreamActivityMock;
  @Mock
  private GetSendNotificationEventsFromStreamActivity getSendNotificationEventsFromStreamActivityMock;
  @Mock
  private SendEventStreamProcessingService sendEventStreamProcessingServiceMock;
  @Mock
  private UpdateLastProcessedStreamEventIdActivity updateLastProcessedStreamEventIdActivityMock;
  @Mock
  private PublishSendTimelineEventActivity publishSendTimelineEventActivityMock;
  @Mock
  private StartDeleteSendNotificationFileActivity startDeleteSendNotificationFileActivityMock;
  @Mock
  private StartDeleteSendLegalFactFileActivity startDeleteSendLegalFactFileActivityMock;
  @Mock
  private NotifySendNotificationStreamEventsActivity notifySendNotificationStreamEventsActivityMock;

  private SendNotificationStreamConsumeWFImpl wf;

  @BeforeEach
  void setUp() {
    SendNotificationStreamWfConfig wfConfigMock = Mockito.mock(SendNotificationStreamWfConfig.class);
    SendNotificationProcessWfConfig wfSendProcessConfigMock = Mockito.mock(SendNotificationProcessWfConfig.class);
    ApplicationContext applicationContextMock = Mockito.mock(ApplicationContext.class);

    when(wfConfigMock.buildGetSendStreamActivityStub()).thenReturn(getSendStreamActivityMock);
    when(wfConfigMock.buildGetSendNotificationEventsFromStreamActivityStub()).thenReturn(getSendNotificationEventsFromStreamActivityMock);
    when(wfConfigMock.buildUpdateLastProcessedStreamEventIdActivityStub()).thenReturn(updateLastProcessedStreamEventIdActivityMock);
    when(wfConfigMock.buildPublishSendTimelineEventActivityStub()).thenReturn(publishSendTimelineEventActivityMock);
    when(wfConfigMock.buildStartDeleteSendNotificationFileActivityStub()).thenReturn(startDeleteSendNotificationFileActivityMock);
    when(wfConfigMock.buildStartDeleteSendLegalFactFileActivityStub()).thenReturn(startDeleteSendLegalFactFileActivityMock);
    when(wfConfigMock.buildNotifySendNotificationStreamEventsActivityStub()).thenReturn(notifySendNotificationStreamEventsActivityMock);

    when(applicationContextMock.getBean(SendNotificationStreamWfConfig.class)).thenReturn(wfConfigMock);

    when(applicationContextMock.getBean(SendNotificationProcessWfConfig.class)).thenReturn(wfSendProcessConfigMock);

    wf = new SendNotificationStreamConsumeWFImpl();
    wf.setApplicationContext(applicationContextMock);
    ReflectionTestUtils.setField(wf, "sendEventStreamProcessingService", sendEventStreamProcessingServiceMock);
  }

  @AfterEach
  void verifyNoMoreInteractions() {
    Mockito.verifyNoMoreInteractions(
      getSendStreamActivityMock,
      getSendNotificationEventsFromStreamActivityMock,
      sendEventStreamProcessingServiceMock,
      updateLastProcessedStreamEventIdActivityMock,
      publishSendTimelineEventActivityMock,
      notifySendNotificationStreamEventsActivityMock
    );
  }

  @Test
  void givenInvalidSendStreamIdWhenReadSendStreamThenStreamNotFound() {
    //GIVEN
    when(getSendStreamActivityMock.fetchSendStream(INVALID_SEND_STREAM_ID))
      .thenReturn(null); //for not entering do-while loop

    //WHEN
    IllegalStateBusinessException workflowInternalErrorException =
      Assertions.assertThrows(IllegalStateBusinessException.class, () -> wf.readSendStream(INVALID_SEND_STREAM_ID));

    //THEN
    verify(getSendStreamActivityMock).fetchSendStream(INVALID_SEND_STREAM_ID);
    Assertions.assertEquals("SEND_STATUS_ERROR", workflowInternalErrorException.getCode());
    Assertions.assertEquals(
      "Workflow terminated during starting of readSendStream for sendStreamId %s with ERROR: cannot found SEND stream.".formatted(INVALID_SEND_STREAM_ID),
      workflowInternalErrorException.getMessage()
    );
  }

  @Test
  void givenErrorInFetchSendNotificationEventsFromStreamWhenReadSendStreamThenStreamNotFound() {
    //GIVEN
    SendStreamDTO streamDTO = buildSendStreamDTO();

    when(getSendStreamActivityMock.fetchSendStream(SEND_STREAM_ID))
      .thenReturn(streamDTO)
      .thenReturn(null); //for breaking from do-while loop

    Mockito.doThrow(new RuntimeException())
      .when(getSendNotificationEventsFromStreamActivityMock)
      .fetchSendNotificationEventsFromStream(ORGANIZATION_ID, SEND_STREAM_ID);

    try (MockedStatic<Workflow> workflowMock = Mockito.mockStatic(Workflow.class)) {
      workflowMock.when(() -> Workflow.sleep(Mockito.any(Duration.class)))
        .then(invocation -> null);

      //WHEN
      wf.readSendStream(SEND_STREAM_ID);

      //THEN
      verify(getSendStreamActivityMock, times(2)).fetchSendStream(SEND_STREAM_ID);
    }

  }

  @Test
  void givenGeneralExceptionInProcessSendStreamEventWhenReadSendStreamThenOK() {
    //GIVEN
    SendStreamDTO streamDTO = buildSendStreamDTO();

    ProgressResponseElementV28DTO sendEvent1 = buildSendEvent("sendEventId1", NotificationStatusV26DTO.VIEWED);
    List<ProgressResponseElementV28DTO> streamEvents = List.of(
      sendEvent1
    );

    when(getSendStreamActivityMock.fetchSendStream(SEND_STREAM_ID))
      .thenReturn(streamDTO)
      .thenReturn(null); //for breaking from do-while loop

    when(
      getSendNotificationEventsFromStreamActivityMock.fetchSendNotificationEventsFromStream(
        ORGANIZATION_ID, SEND_STREAM_ID
      )
    ).thenReturn(streamEvents);

    Mockito.doThrow(new RuntimeException())
      .when(sendEventStreamProcessingServiceMock).processSendStreamEvent(
        Mockito.eq(SEND_STREAM_ID),
        Mockito.isA(ProgressResponseElementV28DTO.class)
      );

    Mockito.doNothing()
      .when(publishSendTimelineEventActivityMock)
      .publishSendTimelineErrorEvent(
        Mockito.eq(sendEvent1),
        Mockito.eq(ORGANIZATION_ID),
        Mockito.eq(SEND_STREAM_ID),
        Mockito.isNull()
      );

    try (MockedStatic<Workflow> workflowMock = Mockito.mockStatic(Workflow.class)) {
      workflowMock.when(() -> Workflow.sleep(Mockito.any(Duration.class)))
        .then(invocation -> null);

      //WHEN
      wf.readSendStream(SEND_STREAM_ID);

      //THEN
      verify(getSendStreamActivityMock, times(2)).fetchSendStream(SEND_STREAM_ID);
      verify(updateLastProcessedStreamEventIdActivityMock)
        .updateLastProcessedStreamEventId(
          SEND_STREAM_ID,
          sendEvent1.getEventId()
        );
    }

  }

  @Test
  void givenSendStreamSkippedEventExceptionInProcessSendStreamEventWhenReadSendStreamThenOK() {
    //GIVEN
    SendStreamDTO streamDTO = buildSendStreamDTO();
    streamDTO.setLastEventId("lastSendEventId");

    ProgressResponseElementV28DTO sendEvent1 = buildSendEvent("sendEventId1", NotificationStatusV26DTO.ACCEPTED);
    ProgressResponseElementV28DTO sendEvent2 = buildSendEvent("sendEventId2", NotificationStatusV26DTO.ACCEPTED);
    List<ProgressResponseElementV28DTO> streamEvents = List.of(
      sendEvent1,
      sendEvent2
    );

    when(getSendStreamActivityMock.fetchSendStream(SEND_STREAM_ID))
      .thenReturn(streamDTO)
      .thenReturn(null); //for breaking from do-while loop

    when(
      getSendNotificationEventsFromStreamActivityMock.fetchSendNotificationEventsFromStream(
        ORGANIZATION_ID, SEND_STREAM_ID
      )
    ).thenReturn(streamEvents);

    ActivityFailure activityFailureMock = Mockito.mock(ActivityFailure.class);
    when(activityFailureMock.getCause())
        .thenReturn(ApplicationFailure.newNonRetryableFailure("error", SendStreamSkippedEventException.class.getName()));

    when(sendEventStreamProcessingServiceMock.processSendStreamEvent(
      Mockito.eq(SEND_STREAM_ID),
      Mockito.isA(ProgressResponseElementV28DTO.class)
    )).thenReturn(sendEvent1.getEventId())
      .thenThrow(activityFailureMock);

    Mockito.doNothing()
      .when(publishSendTimelineEventActivityMock)
      .publishSendTimelineEvent(
        Mockito.isA(ProgressResponseElementV28DTO.class),
        Mockito.eq(ORGANIZATION_ID),
        Mockito.eq(SEND_STREAM_ID),
        Mockito.isNull()
      );

    try (MockedStatic<Workflow> workflowMock = Mockito.mockStatic(Workflow.class)) {
      workflowMock.when(() -> Workflow.sleep(Mockito.any(Duration.class)))
        .then(invocation -> null);

      //WHEN
      wf.readSendStream(SEND_STREAM_ID);

      //THEN
      verify(getSendStreamActivityMock, times(2)).fetchSendStream(SEND_STREAM_ID);
      verify(updateLastProcessedStreamEventIdActivityMock)
        .updateLastProcessedStreamEventId(
          SEND_STREAM_ID,
          sendEvent2.getEventId()
        );
    }

  }

  @Test
  void givenGeneralNotRetryableActivityFailureInProcessSendStreamEventWhenReadSendStreamThenOK() {
    //GIVEN
    SendStreamDTO streamDTO = buildSendStreamDTO();
    streamDTO.setLastEventId("lastSendEventId");

    ProgressResponseElementV28DTO sendEvent1 = buildSendEvent("sendEventId1", NotificationStatusV26DTO.ACCEPTED);
    ProgressResponseElementV28DTO sendEvent2 = buildSendEvent("sendEventId2", NotificationStatusV26DTO.ACCEPTED);
    List<ProgressResponseElementV28DTO> streamEvents = List.of(
      sendEvent1,
      sendEvent2
    );

    when(getSendStreamActivityMock.fetchSendStream(SEND_STREAM_ID))
      .thenReturn(streamDTO)
      .thenReturn(null); //for breaking from do-while loop

    when(
      getSendNotificationEventsFromStreamActivityMock.fetchSendNotificationEventsFromStream(
        ORGANIZATION_ID, SEND_STREAM_ID
      )
    ).thenReturn(streamEvents);

    ActivityFailure activityFailureMock = Mockito.mock(ActivityFailure.class);
    when(activityFailureMock.getCause())
      .thenReturn(ApplicationFailure.newNonRetryableFailure("error", NotRetryableActivityException.class.getName()));

    when(sendEventStreamProcessingServiceMock.processSendStreamEvent(
        Mockito.eq(SEND_STREAM_ID),
        Mockito.isA(ProgressResponseElementV28DTO.class)
      )).thenReturn(sendEvent1.getEventId())
      .thenThrow(activityFailureMock);

    Mockito.doNothing()
      .when(publishSendTimelineEventActivityMock)
      .publishSendTimelineEvent(
        Mockito.eq(sendEvent1),
        Mockito.eq(ORGANIZATION_ID),
        Mockito.eq(SEND_STREAM_ID),
        Mockito.isNull()
      );

    Mockito.doNothing()
      .when(publishSendTimelineEventActivityMock)
      .publishSendTimelineErrorEvent(
        Mockito.eq(sendEvent2),
        Mockito.eq(ORGANIZATION_ID),
        Mockito.eq(SEND_STREAM_ID),
        Mockito.isNull()
      );

    try (MockedStatic<Workflow> workflowMock = Mockito.mockStatic(Workflow.class)) {
      workflowMock.when(() -> Workflow.sleep(Mockito.any(Duration.class)))
        .then(invocation -> null);

      //WHEN
      wf.readSendStream(SEND_STREAM_ID);

      //THEN
      verify(getSendStreamActivityMock, times(2)).fetchSendStream(SEND_STREAM_ID);
      verify(updateLastProcessedStreamEventIdActivityMock)
        .updateLastProcessedStreamEventId(
          SEND_STREAM_ID,
          sendEvent2.getEventId()
        );
    }

  }

  @Test
  void givenGeneralRetryableActivityFailureInProcessSendStreamEventWhenReadSendStreamThenOK() {
    //GIVEN
    SendStreamDTO streamDTO = buildSendStreamDTO();
    streamDTO.setLastEventId("lastSendEventId");

    ProgressResponseElementV28DTO sendEvent1 = buildSendEvent("sendEventId1", NotificationStatusV26DTO.ACCEPTED);
    ProgressResponseElementV28DTO sendEvent2 = buildSendEvent("sendEventId2", NotificationStatusV26DTO.ACCEPTED);
    List<ProgressResponseElementV28DTO> streamEvents = List.of(
      sendEvent1,
      sendEvent2
    );

    when(getSendStreamActivityMock.fetchSendStream(SEND_STREAM_ID))
      .thenReturn(streamDTO)
      .thenReturn(null); //for breaking from do-while loop

    when(
      getSendNotificationEventsFromStreamActivityMock.fetchSendNotificationEventsFromStream(
        ORGANIZATION_ID, SEND_STREAM_ID
      )
    ).thenReturn(streamEvents);

    ActivityFailure activityFailureMock = Mockito.mock(ActivityFailure.class);
    when(activityFailureMock.getCause())
      .thenReturn(ApplicationFailure.newFailure("error", RetryableActivityException.class.getName()));

    when(sendEventStreamProcessingServiceMock.processSendStreamEvent(
        Mockito.eq(SEND_STREAM_ID),
        Mockito.isA(ProgressResponseElementV28DTO.class)
      )).thenReturn(sendEvent1.getEventId())
      .thenThrow(activityFailureMock);

    Mockito.doNothing()
      .when(publishSendTimelineEventActivityMock)
      .publishSendTimelineEvent(
        Mockito.eq(sendEvent1),
        Mockito.eq(ORGANIZATION_ID),
        Mockito.eq(SEND_STREAM_ID),
        Mockito.isNull()
      );

    Mockito.doNothing()
      .when(publishSendTimelineEventActivityMock)
      .publishSendTimelineErrorEvent(
        Mockito.eq(sendEvent2),
        Mockito.eq(ORGANIZATION_ID),
        Mockito.eq(SEND_STREAM_ID),
        Mockito.isNull()
      );

    try (MockedStatic<Workflow> workflowMock = Mockito.mockStatic(Workflow.class)) {
      workflowMock.when(() -> Workflow.sleep(Mockito.any(Duration.class)))
        .then(invocation -> null);

      //WHEN
      wf.readSendStream(SEND_STREAM_ID);

      //THEN
      verify(getSendStreamActivityMock, times(2)).fetchSendStream(SEND_STREAM_ID);
      verify(updateLastProcessedStreamEventIdActivityMock)
        .updateLastProcessedStreamEventId(
          SEND_STREAM_ID,
          sendEvent2.getEventId()
        );
    }

  }

  @Test
  void givenRetryableActivityFailureInProcessSendStreamEventWhenReadSendStreamThenOK() {
    //GIVEN
    SendStreamDTO streamDTO = buildSendStreamDTO();
    streamDTO.setLastEventId("lastSendEventId");

    ProgressResponseElementV28DTO sendEvent1 = buildSendEvent("sendEventId1", NotificationStatusV26DTO.ACCEPTED);
    ProgressResponseElementV28DTO sendEvent2 = buildSendEvent("sendEventId2", NotificationStatusV26DTO.ACCEPTED);
    List<ProgressResponseElementV28DTO> streamEvents = List.of(
      sendEvent1,
      sendEvent2
    );

    when(getSendStreamActivityMock.fetchSendStream(SEND_STREAM_ID))
      .thenReturn(streamDTO)
      .thenReturn(null); //for breaking from do-while loop

    when(
      getSendNotificationEventsFromStreamActivityMock.fetchSendNotificationEventsFromStream(
        ORGANIZATION_ID, SEND_STREAM_ID
      )
    ).thenReturn(streamEvents);

    ActivityFailure activityFailureMock = Mockito.mock(ActivityFailure.class);
    when(activityFailureMock.getCause())
      .thenReturn(ApplicationFailure.newFailureWithCause("error", NotRetryableActivityException.class.getName(), null));

    when(sendEventStreamProcessingServiceMock.processSendStreamEvent(
        Mockito.eq(SEND_STREAM_ID),
        Mockito.isA(ProgressResponseElementV28DTO.class)
      )).thenReturn(sendEvent1.getEventId())
      .thenThrow(activityFailureMock);

    Mockito.doNothing()
      .when(publishSendTimelineEventActivityMock)
      .publishSendTimelineEvent(
        Mockito.eq(sendEvent1),
        Mockito.eq(ORGANIZATION_ID),
        Mockito.eq(SEND_STREAM_ID),
        Mockito.isNull()
      );

    Mockito.doNothing()
      .when(publishSendTimelineEventActivityMock)
      .publishSendTimelineErrorEvent(
        Mockito.eq(sendEvent2),
        Mockito.eq(ORGANIZATION_ID),
        Mockito.eq(SEND_STREAM_ID),
        Mockito.isNull()
      );

    try (MockedStatic<Workflow> workflowMock = Mockito.mockStatic(Workflow.class)) {
      workflowMock.when(() -> Workflow.sleep(Mockito.any(Duration.class)))
        .then(invocation -> null);

      //WHEN
      wf.readSendStream(SEND_STREAM_ID);

      //THEN
      verify(getSendStreamActivityMock, times(2)).fetchSendStream(SEND_STREAM_ID);
      verify(updateLastProcessedStreamEventIdActivityMock)
        .updateLastProcessedStreamEventId(
          SEND_STREAM_ID,
          sendEvent2.getEventId()
        );
    }

  }

  @Test
  void givenActivityFailureWithCauseNotAnApplicationFailureInProcessSendStreamEventWhenReadSendStreamThenOK() {
    //GIVEN
    SendStreamDTO streamDTO = buildSendStreamDTO();
    streamDTO.setLastEventId("lastSendEventId");

    ProgressResponseElementV28DTO sendEvent1 = buildSendEvent("sendEventId1", NotificationStatusV26DTO.ACCEPTED);
    ProgressResponseElementV28DTO sendEvent2 = buildSendEvent("sendEventId2", NotificationStatusV26DTO.ACCEPTED);
    List<ProgressResponseElementV28DTO> streamEvents = List.of(
      sendEvent1,
      sendEvent2
    );

    when(getSendStreamActivityMock.fetchSendStream(SEND_STREAM_ID))
      .thenReturn(streamDTO)
      .thenReturn(null); //for breaking from do-while loop

    when(
      getSendNotificationEventsFromStreamActivityMock.fetchSendNotificationEventsFromStream(
        ORGANIZATION_ID, SEND_STREAM_ID
      )
    ).thenReturn(streamEvents);

    ActivityFailure activityFailureMock = Mockito.mock(ActivityFailure.class);
    when(activityFailureMock.getCause())
      .thenReturn(new RuntimeException());

    when(sendEventStreamProcessingServiceMock.processSendStreamEvent(
        Mockito.eq(SEND_STREAM_ID),
        Mockito.isA(ProgressResponseElementV28DTO.class)
      )).thenReturn(sendEvent1.getEventId())
      .thenThrow(activityFailureMock);

    Mockito.doNothing()
      .when(publishSendTimelineEventActivityMock)
      .publishSendTimelineEvent(
        Mockito.eq(sendEvent1),
        Mockito.eq(ORGANIZATION_ID),
        Mockito.eq(SEND_STREAM_ID),
        Mockito.isNull()
      );

    Mockito.doNothing()
      .when(publishSendTimelineEventActivityMock)
      .publishSendTimelineErrorEvent(
        Mockito.eq(sendEvent2),
        Mockito.eq(ORGANIZATION_ID),
        Mockito.eq(SEND_STREAM_ID),
        Mockito.isNull()
      );

    try (MockedStatic<Workflow> workflowMock = Mockito.mockStatic(Workflow.class)) {
      workflowMock.when(() -> Workflow.sleep(Mockito.any(Duration.class)))
        .then(invocation -> null);

      //WHEN
      wf.readSendStream(SEND_STREAM_ID);

      //THEN
      verify(getSendStreamActivityMock, times(2)).fetchSendStream(SEND_STREAM_ID);
      verify(updateLastProcessedStreamEventIdActivityMock)
        .updateLastProcessedStreamEventId(
          SEND_STREAM_ID,
          sendEvent2.getEventId()
        );
    }

  }

  @Test
  void givenValidSendStreamIdEventWhenReadSendStreamThenContinueAsNew() {
    //GIVEN
    SendStreamDTO streamDTO = buildSendStreamDTO();
    streamDTO.setLastEventId("lastSendEventId");

    ProgressResponseElementV28DTO sendEvent1 = buildSendEvent("sendEventId1", NotificationStatusV26DTO.ACCEPTED);
    ProgressResponseElementV28DTO sendEvent2 = buildSendEvent("sendEventId2", NotificationStatusV26DTO.ACCEPTED);
    List<ProgressResponseElementV28DTO> streamEvents = List.of(
      sendEvent1,
      sendEvent2
    );

    when(getSendStreamActivityMock.fetchSendStream(SEND_STREAM_ID))
      .thenReturn(streamDTO)
      .thenReturn(null); //for breaking from do-while loop

    when(
      getSendNotificationEventsFromStreamActivityMock.fetchSendNotificationEventsFromStream(
        ORGANIZATION_ID, SEND_STREAM_ID
      )
    ).thenReturn(streamEvents);

    when(sendEventStreamProcessingServiceMock.processSendStreamEvent(
        Mockito.eq(SEND_STREAM_ID),
        Mockito.isA(ProgressResponseElementV28DTO.class)
      )).thenReturn(sendEvent1.getEventId())
      .thenReturn(sendEvent2.getEventId());

    Mockito.doNothing()
      .when(publishSendTimelineEventActivityMock)
      .publishSendTimelineEvent(
        Mockito.isA(ProgressResponseElementV28DTO.class),
        Mockito.eq(ORGANIZATION_ID),
        Mockito.eq(SEND_STREAM_ID),
        Mockito.isNull()
      );

    try (MockedStatic<Workflow> workflowMock = Mockito.mockStatic(Workflow.class)) {
      workflowMock.when(() -> Workflow.sleep(Mockito.any(Duration.class)))
        .then(invocation -> null);

      //WHEN
      wf.readSendStream(SEND_STREAM_ID);

      //THEN
      verify(getSendStreamActivityMock, times(2)).fetchSendStream(SEND_STREAM_ID);
      verify(updateLastProcessedStreamEventIdActivityMock)
        .updateLastProcessedStreamEventId(
          SEND_STREAM_ID,
          sendEvent2.getEventId()
        );
      workflowMock.verify(() -> Workflow.continueAsNew(streamDTO.getStreamId()));
    }

  }

  @Test
  void givenValidSendStreamIdWithAcceptedEventWhenReadSendStreamThenOK() {
    //GIVEN
    SendStreamDTO streamDTO = buildSendStreamDTO();
    streamDTO.setLastEventId("lastSendEventId");

    ProgressResponseElementV28DTO sendEvent1 = buildSendEvent("sendEventId1", NotificationStatusV26DTO.ACCEPTED);
    ProgressResponseElementV28DTO sendEvent2 = buildSendEvent("sendEventId2", NotificationStatusV26DTO.ACCEPTED);
    List<ProgressResponseElementV28DTO> streamEvents = List.of(
      sendEvent1,
      sendEvent2
    );

    when(getSendStreamActivityMock.fetchSendStream(SEND_STREAM_ID))
      .thenReturn(streamDTO)
      .thenReturn(null); //for breaking from do-while loop

    when(
      getSendNotificationEventsFromStreamActivityMock.fetchSendNotificationEventsFromStream(
        ORGANIZATION_ID, SEND_STREAM_ID
      )
    ).thenReturn(streamEvents);

    when(sendEventStreamProcessingServiceMock.processSendStreamEvent(
      Mockito.eq(SEND_STREAM_ID),
      Mockito.isA(ProgressResponseElementV28DTO.class)
    )).thenReturn(sendEvent1.getEventId())
      .thenReturn(sendEvent2.getEventId());

    Mockito.doNothing()
      .when(publishSendTimelineEventActivityMock)
      .publishSendTimelineEvent(
        Mockito.isA(ProgressResponseElementV28DTO.class),
        Mockito.eq(ORGANIZATION_ID),
        Mockito.eq(SEND_STREAM_ID),
        Mockito.isNull()
      );

    try (MockedStatic<Workflow> workflowMock = Mockito.mockStatic(Workflow.class)) {
      workflowMock.when(() -> Workflow.sleep(Mockito.any(Duration.class)))
        .then(invocation -> null);

      //WHEN
      wf.readSendStream(SEND_STREAM_ID);

      //THEN
      verify(getSendStreamActivityMock, times(2)).fetchSendStream(SEND_STREAM_ID);
      verify(updateLastProcessedStreamEventIdActivityMock)
        .updateLastProcessedStreamEventId(
          SEND_STREAM_ID,
          sendEvent2.getEventId()
        );
    }

  }

  @Test
  void givenValidSendStreamIdWithEmptyStreamEventsWhenReadSendStreamThenOK() {
    //GIVEN
    SendStreamDTO streamDTO = buildSendStreamDTO();
    streamDTO.setLastEventId("lastSendEventId");

    when(getSendStreamActivityMock.fetchSendStream(SEND_STREAM_ID))
      .thenReturn(streamDTO)
      .thenReturn(null); //for breaking from do-while loop

    when(
      getSendNotificationEventsFromStreamActivityMock.fetchSendNotificationEventsFromStream(
        ORGANIZATION_ID, SEND_STREAM_ID
      )
    ).thenReturn(Collections.emptyList());

    try (MockedStatic<Workflow> workflowMock = Mockito.mockStatic(Workflow.class)) {
      workflowMock.when(() -> Workflow.sleep(Mockito.any(Duration.class)))
        .then(invocation -> null);

      //WHEN
      wf.readSendStream(SEND_STREAM_ID);

      //THEN
      verify(getSendStreamActivityMock, times(2)).fetchSendStream(SEND_STREAM_ID);
    }

  }

  @Test
  void givenGenericErrorInCallIsStreamStillOpenWhenReadSendStreamThenOK() {
    //GIVEN
    SendStreamDTO streamDTO = buildSendStreamDTO();
    streamDTO.setLastEventId("lastSendEventId");

    ProgressResponseElementV28DTO sendEvent1 = buildSendEvent("sendEventId1", NotificationStatusV26DTO.ACCEPTED);
    List<ProgressResponseElementV28DTO> streamEvents = List.of(
      sendEvent1
    );

    when(getSendStreamActivityMock.fetchSendStream(SEND_STREAM_ID))
      .thenReturn(streamDTO)
      .thenThrow(new RuntimeException("Error"))
      .thenReturn(null); //for breaking from do-while loop

    when(
      getSendNotificationEventsFromStreamActivityMock.fetchSendNotificationEventsFromStream(
        ORGANIZATION_ID, SEND_STREAM_ID
      )
    ).thenReturn(streamEvents)
    .thenReturn(Collections.emptyList());

    when(sendEventStreamProcessingServiceMock.processSendStreamEvent(
      Mockito.eq(SEND_STREAM_ID),
      Mockito.isA(ProgressResponseElementV28DTO.class)
    )).thenReturn(sendEvent1.getEventId());

    Mockito.doNothing()
      .when(publishSendTimelineEventActivityMock)
      .publishSendTimelineEvent(
        Mockito.eq(sendEvent1),
        Mockito.eq(ORGANIZATION_ID),
        Mockito.eq(SEND_STREAM_ID),
        Mockito.isNull()
      );

    try (MockedStatic<Workflow> workflowMock = Mockito.mockStatic(Workflow.class)) {
      workflowMock.when(() -> Workflow.sleep(Mockito.any(Duration.class)))
        .then(invocation -> null);

      //WHEN
      wf.readSendStream(SEND_STREAM_ID);

      //THEN
      verify(getSendStreamActivityMock, times(3))
        .fetchSendStream(SEND_STREAM_ID);
      verify(updateLastProcessedStreamEventIdActivityMock)
        .updateLastProcessedStreamEventId(SEND_STREAM_ID, sendEvent1.getEventId());
    }

  }

  @Test
  void givenNotFoundExceptionInCallIsStreamStillOpenWhenReadSendStreamThenOK() {
    //GIVEN
    SendStreamDTO streamDTO = buildSendStreamDTO();
    streamDTO.setLastEventId("lastSendEventId");

    ProgressResponseElementV28DTO sendEvent1 = buildSendEvent("sendEventId1", NotificationStatusV26DTO.ACCEPTED);
    List<ProgressResponseElementV28DTO> streamEvents = List.of(
      sendEvent1
    );

    when(getSendStreamActivityMock.fetchSendStream(SEND_STREAM_ID))
      .thenReturn(streamDTO)
      .thenThrow(HttpClientErrorException.create(HttpStatus.NOT_FOUND, "NotFound", null, null, null));

    when(
      getSendNotificationEventsFromStreamActivityMock.fetchSendNotificationEventsFromStream(
        ORGANIZATION_ID, SEND_STREAM_ID
      )
    ).thenReturn(streamEvents);

    when(sendEventStreamProcessingServiceMock.processSendStreamEvent(
      Mockito.eq(SEND_STREAM_ID),
      Mockito.isA(ProgressResponseElementV28DTO.class)
    )).thenReturn(sendEvent1.getEventId());

    Mockito.doNothing()
      .when(publishSendTimelineEventActivityMock)
      .publishSendTimelineEvent(
        Mockito.eq(sendEvent1),
        Mockito.eq(ORGANIZATION_ID),
        Mockito.eq(SEND_STREAM_ID),
        Mockito.isNull()
      );

    try (MockedStatic<Workflow> workflowMock = Mockito.mockStatic(Workflow.class)) {
      workflowMock.when(() -> Workflow.sleep(Mockito.any(Duration.class)))
        .then(invocation -> null);

      //WHEN
      IllegalStateBusinessException actualException =
        Assertions.assertThrows(IllegalStateBusinessException.class, () -> wf.readSendStream(SEND_STREAM_ID));

      Assertions.assertEquals("SEND_STATUS_ERROR", actualException.getCode());
      Assertions.assertEquals(
        "Workflow terminated during isStreamStillOpened for sendStreamId " + SEND_STREAM_ID + " with ERROR: 404 NotFound",
        actualException.getMessage()
      );

      //THEN
      verify(getSendStreamActivityMock, times(2))
        .fetchSendStream(SEND_STREAM_ID);
      verify(updateLastProcessedStreamEventIdActivityMock)
        .updateLastProcessedStreamEventId(SEND_STREAM_ID, sendEvent1.getEventId());
    }

  }

  @Test
  void givenRepeatOneHundredTimesWhenReadSendStreamThenContinueAsNew() {
    //GIVEN
    SendStreamDTO streamDTO = buildSendStreamDTO();
    streamDTO.setLastEventId("lastSendEventId");

    SendStreamDTO[] oneHundredStreams = new SendStreamDTO[100];
    Arrays.fill(oneHundredStreams, streamDTO);

    when(getSendStreamActivityMock.fetchSendStream(SEND_STREAM_ID))
      .thenReturn(streamDTO, oneHundredStreams)
      .thenReturn(null); //for breaking from do-while loop

    when(
      getSendNotificationEventsFromStreamActivityMock.fetchSendNotificationEventsFromStream(
        ORGANIZATION_ID, SEND_STREAM_ID
      )
    ).thenReturn(Collections.emptyList());

    try (MockedStatic<Workflow> workflowMock = Mockito.mockStatic(Workflow.class)) {
      workflowMock.when(() -> Workflow.sleep(Mockito.any(Duration.class)))
        .then(invocation -> null);

      //WHEN
      wf.readSendStream(SEND_STREAM_ID);

      //THEN
      verify(getSendStreamActivityMock, times(102))
        .fetchSendStream(SEND_STREAM_ID);
      verify(updateLastProcessedStreamEventIdActivityMock, times(0))
        .updateLastProcessedStreamEventId(
          Mockito.eq(SEND_STREAM_ID),
          Mockito.anyString()
        );
      workflowMock.verify(() -> Workflow.continueAsNew(streamDTO.getStreamId()));
    }

  }

  @Test
  void givenValidSendStreamIdWithDifferentEventIdWhenReadSendStreamThenOK() {
    //GIVEN
    SendStreamDTO streamDTO = buildSendStreamDTO();
    streamDTO.setLastEventId("lastSendEventId");

    ProgressResponseElementV28DTO sendEvent1 = buildSendEvent("sendEventId1", NotificationStatusV26DTO.ACCEPTED);
    List<ProgressResponseElementV28DTO> streamEvents = List.of(
      sendEvent1
    );

    when(getSendStreamActivityMock.fetchSendStream(SEND_STREAM_ID))
      .thenReturn(streamDTO)
      .thenReturn(null); //for breaking from do-while loop

    when(
      getSendNotificationEventsFromStreamActivityMock.fetchSendNotificationEventsFromStream(
        ORGANIZATION_ID, SEND_STREAM_ID
      )
    ).thenReturn(streamEvents);

    when(sendEventStreamProcessingServiceMock.processSendStreamEvent(
        Mockito.eq(SEND_STREAM_ID),
        Mockito.isA(ProgressResponseElementV28DTO.class)
      )).thenReturn(sendEvent1.getEventId());

    Mockito.doNothing()
      .when(publishSendTimelineEventActivityMock)
      .publishSendTimelineEvent(
        Mockito.eq(sendEvent1),
        Mockito.eq(ORGANIZATION_ID),
        Mockito.eq(SEND_STREAM_ID),
        Mockito.isNull()
      );

    try (MockedStatic<Workflow> workflowMock = Mockito.mockStatic(Workflow.class)) {
      workflowMock.when(() -> Workflow.sleep(Mockito.any(Duration.class)))
        .then(invocation -> null);

      //WHEN
      wf.readSendStream(SEND_STREAM_ID);

      //THEN
      verify(getSendStreamActivityMock, times(2))
        .fetchSendStream(SEND_STREAM_ID);
      verify(updateLastProcessedStreamEventIdActivityMock)
        .updateLastProcessedStreamEventId(SEND_STREAM_ID, sendEvent1.getEventId());
    }

  }

  @Test
  void givenValidSendStreamIdWithSameEventIdWhenReadSendStreamThenOK() {
    //GIVEN
    SendStreamDTO streamDTO = buildSendStreamDTO();
    streamDTO.setLastEventId("lastSendEventId");

    ProgressResponseElementV28DTO sendEvent1 = buildSendEvent("lastSendEventId", NotificationStatusV26DTO.ACCEPTED);
    List<ProgressResponseElementV28DTO> streamEvents = List.of(
      sendEvent1
    );

    when(getSendStreamActivityMock.fetchSendStream(SEND_STREAM_ID))
      .thenReturn(streamDTO)
      .thenReturn(null); //for breaking from do-while loop

    when(
      getSendNotificationEventsFromStreamActivityMock.fetchSendNotificationEventsFromStream(
        ORGANIZATION_ID, SEND_STREAM_ID
      )
    ).thenReturn(streamEvents);

    when(sendEventStreamProcessingServiceMock.processSendStreamEvent(
      Mockito.eq(SEND_STREAM_ID),
      Mockito.isA(ProgressResponseElementV28DTO.class)
    )).thenReturn(sendEvent1.getEventId());

    Mockito.doNothing()
      .when(publishSendTimelineEventActivityMock)
      .publishSendTimelineEvent(
        Mockito.eq(sendEvent1),
        Mockito.eq(ORGANIZATION_ID),
        Mockito.eq(SEND_STREAM_ID),
        Mockito.isNull()
      );

    try (MockedStatic<Workflow> workflowMock = Mockito.mockStatic(Workflow.class)) {
      workflowMock.when(() -> Workflow.sleep(Mockito.any(Duration.class)))
        .then(invocation -> null);

      //WHEN
      wf.readSendStream(SEND_STREAM_ID);

      //THEN
      verify(getSendStreamActivityMock, times(2))
        .fetchSendStream(SEND_STREAM_ID);
      verify(updateLastProcessedStreamEventIdActivityMock, times(0))
        .updateLastProcessedStreamEventId(SEND_STREAM_ID, sendEvent1.getEventId());
    }

  }

  @Test
  void givenValidSendStreamIdWithNullEventIdWhenReadSendStreamThenOK() {
    //GIVEN
    SendStreamDTO streamDTO = buildSendStreamDTO();
    streamDTO.setLastEventId("lastSendEventId");

    ProgressResponseElementV28DTO sendEvent1 = buildSendEvent(null, NotificationStatusV26DTO.ACCEPTED);
    List<ProgressResponseElementV28DTO> streamEvents = List.of(
      sendEvent1
    );

    when(getSendStreamActivityMock.fetchSendStream(SEND_STREAM_ID))
      .thenReturn(streamDTO)
      .thenReturn(null); //for breaking from do-while loop

    when(
      getSendNotificationEventsFromStreamActivityMock.fetchSendNotificationEventsFromStream(
        ORGANIZATION_ID, SEND_STREAM_ID
      )
    ).thenReturn(streamEvents);

    when(sendEventStreamProcessingServiceMock.processSendStreamEvent(
      Mockito.eq(SEND_STREAM_ID),
      Mockito.isA(ProgressResponseElementV28DTO.class)
    )).thenReturn(sendEvent1.getEventId());

    Mockito.doNothing()
      .when(publishSendTimelineEventActivityMock)
      .publishSendTimelineEvent(
        Mockito.eq(sendEvent1),
        Mockito.eq(ORGANIZATION_ID),
        Mockito.eq(SEND_STREAM_ID),
        Mockito.isNull()
      );

    try (MockedStatic<Workflow> workflowMock = Mockito.mockStatic(Workflow.class)) {
      workflowMock.when(() -> Workflow.sleep(Mockito.any(Duration.class)))
        .then(invocation -> null);

      //WHEN
      wf.readSendStream(SEND_STREAM_ID);

      //THEN
      verify(getSendStreamActivityMock, times(2))
        .fetchSendStream(SEND_STREAM_ID);
      verify(updateLastProcessedStreamEventIdActivityMock, times(0))
        .updateLastProcessedStreamEventId(SEND_STREAM_ID, sendEvent1.getEventId());
    }

  }

  @Test
  void givenUpdateLastProcessedStreamEventIdThrowsErrorWhenReadSendStreamThenOK() {
    //GIVEN
    SendStreamDTO streamDTO = buildSendStreamDTO();
    streamDTO.setLastEventId("lastSendEventId");

    ProgressResponseElementV28DTO sendEvent1 = buildSendEvent("sendEventId1", NotificationStatusV26DTO.ACCEPTED);
    List<ProgressResponseElementV28DTO> streamEvents = List.of(
      sendEvent1
    );

    when(getSendStreamActivityMock.fetchSendStream(SEND_STREAM_ID))
      .thenReturn(streamDTO)
      .thenReturn(null); //for breaking from do-while loop

    when(
      getSendNotificationEventsFromStreamActivityMock.fetchSendNotificationEventsFromStream(
        ORGANIZATION_ID, SEND_STREAM_ID
      )
    ).thenReturn(streamEvents);

    when(sendEventStreamProcessingServiceMock.processSendStreamEvent(
      Mockito.eq(SEND_STREAM_ID),
      Mockito.isA(ProgressResponseElementV28DTO.class)
    )).thenReturn(sendEvent1.getEventId());

    Mockito.doNothing()
      .when(publishSendTimelineEventActivityMock)
      .publishSendTimelineEvent(
        Mockito.eq(sendEvent1),
        Mockito.eq(ORGANIZATION_ID),
        Mockito.eq(SEND_STREAM_ID),
        Mockito.isNull()
      );

    Mockito.doThrow(new RuntimeException("Error"))
      .when(updateLastProcessedStreamEventIdActivityMock)
      .updateLastProcessedStreamEventId(
        Mockito.eq(SEND_STREAM_ID),
        Mockito.isA(String.class)
      );

    try (MockedStatic<Workflow> workflowMock = Mockito.mockStatic(Workflow.class)) {
      workflowMock.when(() -> Workflow.sleep(Mockito.any(Duration.class)))
        .then(invocation -> null);

      //WHEN
      wf.readSendStream(SEND_STREAM_ID);

      //THEN
      verify(getSendStreamActivityMock, times(2))
        .fetchSendStream(SEND_STREAM_ID);
      verify(updateLastProcessedStreamEventIdActivityMock)
        .updateLastProcessedStreamEventId(SEND_STREAM_ID, sendEvent1.getEventId());
    }

  }

  @Test
  void givenExpectedNotificationRequestIdToTimelineCategoriesMapWhenReadSendStreamThenNotifyStreamEvents() {
    //GIVEN
    SendStreamDTO streamDTO = buildSendStreamDTO();
    streamDTO.setLastEventId("lastSendEventId");

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

    when(getSendStreamActivityMock.fetchSendStream(SEND_STREAM_ID))
      .thenReturn(streamDTO)
      .thenReturn(null); //for breaking from do-while loop

    when(
      getSendNotificationEventsFromStreamActivityMock.fetchSendNotificationEventsFromStream(
        ORGANIZATION_ID, SEND_STREAM_ID
      )
    ).thenReturn(streamEvents);

    when(sendEventStreamProcessingServiceMock.processSendStreamEvent(
        Mockito.eq(SEND_STREAM_ID),
        Mockito.isA(ProgressResponseElementV28DTO.class)
      )).thenReturn(sendEvent1.getEventId())
      .thenReturn(sendEvent2.getEventId())
      .thenReturn(sendEvent3.getEventId())
      .thenReturn(sendEvent4.getEventId());

    Mockito.doNothing()
      .when(publishSendTimelineEventActivityMock)
      .publishSendTimelineEvent(
        Mockito.isA(ProgressResponseElementV28DTO.class),
        Mockito.eq(ORGANIZATION_ID),
        Mockito.eq(SEND_STREAM_ID),
        Mockito.isNull()
      );

    Mockito.doNothing()
      .when(notifySendNotificationStreamEventsActivityMock)
      .notifySendNotificationStreamEvents(
        notificationRequestIdToStreamEventsMapCaptor.capture()
      );

    try (MockedStatic<Workflow> workflowMock = Mockito.mockStatic(Workflow.class)) {
      workflowMock.when(() -> Workflow.sleep(Mockito.any(Duration.class)))
        .then(invocation -> null);

      //WHEN
      wf.readSendStream(SEND_STREAM_ID);

      //THEN
      verify(getSendStreamActivityMock, times(2)).fetchSendStream(SEND_STREAM_ID);
      verify(updateLastProcessedStreamEventIdActivityMock)
        .updateLastProcessedStreamEventId(
          SEND_STREAM_ID,
          sendEvent4.getEventId()
        );
      Assertions.assertEquals(
        expectedNotificationRequestIdToStreamEventsMap,
        notificationRequestIdToStreamEventsMapCaptor.getValue()
      );
      workflowMock.verify(() -> Workflow.continueAsNew(streamDTO.getStreamId()));
    }

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

  private static SendStreamDTO buildSendStreamDTO() {
    SendStreamDTO streamDTO = new SendStreamDTO();
    streamDTO.setStreamId(SEND_STREAM_ID);
    streamDTO.setOrganizationId(ORGANIZATION_ID);
    return streamDTO;
  }

}
