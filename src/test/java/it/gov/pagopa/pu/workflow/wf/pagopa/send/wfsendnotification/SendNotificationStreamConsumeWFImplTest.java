package it.gov.pagopa.pu.workflow.wf.pagopa.send.wfsendnotification;

import io.temporal.failure.ActivityFailure;
import io.temporal.failure.ApplicationFailure;
import io.temporal.workflow.Workflow;
import it.gov.pagopa.payhub.activities.activity.sendnotification.GetSendNotificationEventsFromStreamActivity;
import it.gov.pagopa.payhub.activities.activity.sendnotification.GetSendStreamActivity;
import it.gov.pagopa.payhub.activities.activity.sendnotification.UpdateLastProcessedStreamEventIdActivity;
import it.gov.pagopa.payhub.activities.exception.NotRetryableActivityException;
import it.gov.pagopa.payhub.activities.exception.RetryableActivityException;
import it.gov.pagopa.payhub.activities.exception.sendnotification.SendStreamSkippedEventException;
import it.gov.pagopa.pu.sendnotification.dto.generated.*;
import it.gov.pagopa.pu.workflow.exception.custom.WorkflowInternalErrorException;
import it.gov.pagopa.pu.workflow.wf.pagopa.send.service.SendEventStreamProcessingService;
import it.gov.pagopa.pu.workflow.wf.pagopa.send.config.SendNotificationProcessWfConfig;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpStatus;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.HttpClientErrorException;

import java.time.Duration;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@ExtendWith(MockitoExtension.class)
class SendNotificationStreamConsumeWFImplTest {

  public static final long ORGANIZATION_ID = 1L;
  public static final String NOTIFICATION_REQUEST_ID = "notificationRequestId";
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

  private SendNotificationStreamConsumeWFImpl wf;

  @BeforeEach
  void setUp() {
    SendNotificationProcessWfConfig wfConfigMock = Mockito.mock(SendNotificationProcessWfConfig.class);
    ApplicationContext applicationContextMock = Mockito.mock(ApplicationContext.class);

    Mockito.when(wfConfigMock.buildGetSendStreamActivityStub()).thenReturn(getSendStreamActivityMock);
    Mockito.when(wfConfigMock.buildGetSendNotificationEventsFromStreamActivityStub()).thenReturn(getSendNotificationEventsFromStreamActivityMock);
    Mockito.when(wfConfigMock.buildUpdateLastProcessedStreamEventIdActivityStub()).thenReturn(updateLastProcessedStreamEventIdActivityMock);

    Mockito.when(applicationContextMock.getBean(SendNotificationProcessWfConfig.class)).thenReturn(wfConfigMock);

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
      updateLastProcessedStreamEventIdActivityMock
    );
  }

  @Test
  void givenInvalidSendStreamIdWhenReadSendStreamThenStreamNotFound() {
    //GIVEN
    Mockito.when(getSendStreamActivityMock.fetchSendStream(INVALID_SEND_STREAM_ID))
      .thenReturn(null); //for not entering do-while loop

    try (MockedStatic<Workflow> workflowMock = Mockito.mockStatic(Workflow.class)) {
      workflowMock.when(() -> Workflow.sleep(Mockito.any(Duration.class)))
        .then(invocation -> null);

      //WHEN
      WorkflowInternalErrorException workflowInternalErrorException =
        Assertions.assertThrows(WorkflowInternalErrorException.class, () -> wf.readSendStream(INVALID_SEND_STREAM_ID));

      //THEN
      Mockito.verify(getSendStreamActivityMock).fetchSendStream(INVALID_SEND_STREAM_ID);
      Assertions.assertEquals(
        "[SEND_STATUS_ERROR] Workflow terminated during starting of readSendStream for sendStreamId %s with ERROR: cannot found SEND stream.".formatted(INVALID_SEND_STREAM_ID),
        workflowInternalErrorException.getMessage()
      );
    }

  }

  @Test
  void givenErrorInFetchSendNotificationEventsFromStreamWhenReadSendStreamThenStreamNotFound() {
    //GIVEN
    SendStreamDTO streamDTO = buildSendStreamDTO(INVALID_SEND_STREAM_ID);

    Mockito.when(getSendStreamActivityMock.fetchSendStream(INVALID_SEND_STREAM_ID))
      .thenReturn(streamDTO)
      .thenReturn(null); //for breaking from do-while loop

    Mockito.doThrow(new RuntimeException())
      .when(getSendNotificationEventsFromStreamActivityMock)
      .fetchSendNotificationEventsFromStream(ORGANIZATION_ID, INVALID_SEND_STREAM_ID);

    try (MockedStatic<Workflow> workflowMock = Mockito.mockStatic(Workflow.class)) {
      workflowMock.when(() -> Workflow.sleep(Mockito.any(Duration.class)))
        .then(invocation -> null);

      //WHEN
      wf.readSendStream(INVALID_SEND_STREAM_ID);

      //THEN
      Mockito.verify(getSendStreamActivityMock, Mockito.times(2)).fetchSendStream(INVALID_SEND_STREAM_ID);
    }

  }

  @Test
  void givenGeneralExceptionInProcessSendStreamEventWhenReadSendStreamThenOK() {
    //GIVEN
    SendStreamDTO streamDTO = buildSendStreamDTO(SEND_STREAM_ID);

    ProgressResponseElementV28DTO sendEvent1 = buildSendEvent("sendEventId1", NotificationStatusV26DTO.VIEWED);
    List<ProgressResponseElementV28DTO> streamEvents = List.of(
      sendEvent1
    );

    Mockito.when(getSendStreamActivityMock.fetchSendStream(SEND_STREAM_ID))
      .thenReturn(streamDTO)
      .thenReturn(null); //for breaking from do-while loop

    Mockito.when(
      getSendNotificationEventsFromStreamActivityMock.fetchSendNotificationEventsFromStream(
        ORGANIZATION_ID, SEND_STREAM_ID
      )
    ).thenReturn(streamEvents);

    Mockito.doThrow(new RuntimeException())
      .when(sendEventStreamProcessingServiceMock).processSendStreamEvent(
        Mockito.eq(SEND_STREAM_ID),
        Mockito.isA(ProgressResponseElementV28DTO.class)
      );

    try (MockedStatic<Workflow> workflowMock = Mockito.mockStatic(Workflow.class)) {
      workflowMock.when(() -> Workflow.sleep(Mockito.any(Duration.class)))
        .then(invocation -> null);

      //WHEN
      wf.readSendStream(SEND_STREAM_ID);

      //THEN
      Mockito.verify(getSendStreamActivityMock, Mockito.times(2)).fetchSendStream(SEND_STREAM_ID);
    }

  }

  @Test
  void givenSendStreamSkippedEventExceptionInProcessSendStreamEventWhenReadSendStreamThenOK() {
    //GIVEN
    SendStreamDTO streamDTO = buildSendStreamDTO(SEND_STREAM_ID);
    streamDTO.setLastEventId("lastSendEventId");

    ProgressResponseElementV28DTO sendEvent1 = buildSendEvent("sendEventId1", NotificationStatusV26DTO.ACCEPTED);
    ProgressResponseElementV28DTO sendEvent2 = buildSendEvent("sendEventId2", NotificationStatusV26DTO.ACCEPTED);
    List<ProgressResponseElementV28DTO> streamEvents = List.of(
      sendEvent1,
      sendEvent2
    );

    Mockito.when(getSendStreamActivityMock.fetchSendStream(SEND_STREAM_ID))
      .thenReturn(streamDTO)
      .thenReturn(null); //for breaking from do-while loop

    Mockito.when(
      getSendNotificationEventsFromStreamActivityMock.fetchSendNotificationEventsFromStream(
        ORGANIZATION_ID, SEND_STREAM_ID
      )
    ).thenReturn(streamEvents);

    ActivityFailure activityFailureMock = Mockito.mock(ActivityFailure.class);
    Mockito.when(activityFailureMock.getCause())
        .thenReturn(ApplicationFailure.newNonRetryableFailure("error", SendStreamSkippedEventException.class.getName()));

    Mockito.when(sendEventStreamProcessingServiceMock.processSendStreamEvent(
      Mockito.eq(SEND_STREAM_ID),
      Mockito.isA(ProgressResponseElementV28DTO.class)
    )).thenReturn(sendEvent1.getEventId())
      .thenThrow(activityFailureMock);

    try (MockedStatic<Workflow> workflowMock = Mockito.mockStatic(Workflow.class)) {
      workflowMock.when(() -> Workflow.sleep(Mockito.any(Duration.class)))
        .then(invocation -> null);

      //WHEN
      wf.readSendStream(SEND_STREAM_ID);

      //THEN
      Mockito.verify(getSendStreamActivityMock, Mockito.times(2)).fetchSendStream(SEND_STREAM_ID);
      Mockito.verify(updateLastProcessedStreamEventIdActivityMock)
        .updateLastProcessedStreamEventId(
          SEND_STREAM_ID,
          sendEvent2.getEventId()
        );
    }

  }

  @Test
  void givenGeneralNotRetryableActivityFailureInProcessSendStreamEventWhenReadSendStreamThenOK() {
    //GIVEN
    SendStreamDTO streamDTO = buildSendStreamDTO(SEND_STREAM_ID);
    streamDTO.setLastEventId("lastSendEventId");

    ProgressResponseElementV28DTO sendEvent1 = buildSendEvent("sendEventId1", NotificationStatusV26DTO.ACCEPTED);
    ProgressResponseElementV28DTO sendEvent2 = buildSendEvent("sendEventId2", NotificationStatusV26DTO.ACCEPTED);
    List<ProgressResponseElementV28DTO> streamEvents = List.of(
      sendEvent1,
      sendEvent2
    );

    Mockito.when(getSendStreamActivityMock.fetchSendStream(SEND_STREAM_ID))
      .thenReturn(streamDTO)
      .thenReturn(null); //for breaking from do-while loop

    Mockito.when(
      getSendNotificationEventsFromStreamActivityMock.fetchSendNotificationEventsFromStream(
        ORGANIZATION_ID, SEND_STREAM_ID
      )
    ).thenReturn(streamEvents);

    ActivityFailure activityFailureMock = Mockito.mock(ActivityFailure.class);
    Mockito.when(activityFailureMock.getCause())
      .thenReturn(ApplicationFailure.newNonRetryableFailure("error", NotRetryableActivityException.class.getName()));

    Mockito.when(sendEventStreamProcessingServiceMock.processSendStreamEvent(
        Mockito.eq(SEND_STREAM_ID),
        Mockito.isA(ProgressResponseElementV28DTO.class)
      )).thenReturn(sendEvent1.getEventId())
      .thenThrow(activityFailureMock);

    try (MockedStatic<Workflow> workflowMock = Mockito.mockStatic(Workflow.class)) {
      workflowMock.when(() -> Workflow.sleep(Mockito.any(Duration.class)))
        .then(invocation -> null);

      //WHEN
      wf.readSendStream(SEND_STREAM_ID);

      //THEN
      Mockito.verify(getSendStreamActivityMock, Mockito.times(2)).fetchSendStream(SEND_STREAM_ID);
      Mockito.verify(updateLastProcessedStreamEventIdActivityMock)
        .updateLastProcessedStreamEventId(
          SEND_STREAM_ID,
          sendEvent1.getEventId()
        );
    }

  }

  @Test
  void givenGeneralRetryableActivityFailureInProcessSendStreamEventWhenReadSendStreamThenOK() {
    //GIVEN
    SendStreamDTO streamDTO = buildSendStreamDTO(SEND_STREAM_ID);
    streamDTO.setLastEventId("lastSendEventId");

    ProgressResponseElementV28DTO sendEvent1 = buildSendEvent("sendEventId1", NotificationStatusV26DTO.ACCEPTED);
    ProgressResponseElementV28DTO sendEvent2 = buildSendEvent("sendEventId2", NotificationStatusV26DTO.ACCEPTED);
    List<ProgressResponseElementV28DTO> streamEvents = List.of(
      sendEvent1,
      sendEvent2
    );

    Mockito.when(getSendStreamActivityMock.fetchSendStream(SEND_STREAM_ID))
      .thenReturn(streamDTO)
      .thenReturn(null); //for breaking from do-while loop

    Mockito.when(
      getSendNotificationEventsFromStreamActivityMock.fetchSendNotificationEventsFromStream(
        ORGANIZATION_ID, SEND_STREAM_ID
      )
    ).thenReturn(streamEvents);

    ActivityFailure activityFailureMock = Mockito.mock(ActivityFailure.class);
    Mockito.when(activityFailureMock.getCause())
      .thenReturn(ApplicationFailure.newFailure("error", RetryableActivityException.class.getName()));

    Mockito.when(sendEventStreamProcessingServiceMock.processSendStreamEvent(
        Mockito.eq(SEND_STREAM_ID),
        Mockito.isA(ProgressResponseElementV28DTO.class)
      )).thenReturn(sendEvent1.getEventId())
      .thenThrow(activityFailureMock);

    try (MockedStatic<Workflow> workflowMock = Mockito.mockStatic(Workflow.class)) {
      workflowMock.when(() -> Workflow.sleep(Mockito.any(Duration.class)))
        .then(invocation -> null);

      //WHEN
      wf.readSendStream(SEND_STREAM_ID);

      //THEN
      Mockito.verify(getSendStreamActivityMock, Mockito.times(2)).fetchSendStream(SEND_STREAM_ID);
      Mockito.verify(updateLastProcessedStreamEventIdActivityMock)
        .updateLastProcessedStreamEventId(
          SEND_STREAM_ID,
          sendEvent1.getEventId()
        );
    }

  }

  @Test
  void givenRetryableActivityFailureInProcessSendStreamEventWhenReadSendStreamThenOK() {
    //GIVEN
    SendStreamDTO streamDTO = buildSendStreamDTO(SEND_STREAM_ID);
    streamDTO.setLastEventId("lastSendEventId");

    ProgressResponseElementV28DTO sendEvent1 = buildSendEvent("sendEventId1", NotificationStatusV26DTO.ACCEPTED);
    ProgressResponseElementV28DTO sendEvent2 = buildSendEvent("sendEventId2", NotificationStatusV26DTO.ACCEPTED);
    List<ProgressResponseElementV28DTO> streamEvents = List.of(
      sendEvent1,
      sendEvent2
    );

    Mockito.when(getSendStreamActivityMock.fetchSendStream(SEND_STREAM_ID))
      .thenReturn(streamDTO)
      .thenReturn(null); //for breaking from do-while loop

    Mockito.when(
      getSendNotificationEventsFromStreamActivityMock.fetchSendNotificationEventsFromStream(
        ORGANIZATION_ID, SEND_STREAM_ID
      )
    ).thenReturn(streamEvents);

    ActivityFailure activityFailureMock = Mockito.mock(ActivityFailure.class);
    Mockito.when(activityFailureMock.getCause())
      .thenReturn(ApplicationFailure.newFailureWithCause("error", NotRetryableActivityException.class.getName(), null));

    Mockito.when(sendEventStreamProcessingServiceMock.processSendStreamEvent(
        Mockito.eq(SEND_STREAM_ID),
        Mockito.isA(ProgressResponseElementV28DTO.class)
      )).thenReturn(sendEvent1.getEventId())
      .thenThrow(activityFailureMock);

    try (MockedStatic<Workflow> workflowMock = Mockito.mockStatic(Workflow.class)) {
      workflowMock.when(() -> Workflow.sleep(Mockito.any(Duration.class)))
        .then(invocation -> null);

      //WHEN
      wf.readSendStream(SEND_STREAM_ID);

      //THEN
      Mockito.verify(getSendStreamActivityMock, Mockito.times(2)).fetchSendStream(SEND_STREAM_ID);
      Mockito.verify(updateLastProcessedStreamEventIdActivityMock)
        .updateLastProcessedStreamEventId(
          SEND_STREAM_ID,
          sendEvent1.getEventId()
        );
    }

  }

  @Test
  void givenActivityFailureWithCauseNotAnApplicationFailureInProcessSendStreamEventWhenReadSendStreamThenOK() {
    //GIVEN
    SendStreamDTO streamDTO = buildSendStreamDTO(SEND_STREAM_ID);
    streamDTO.setLastEventId("lastSendEventId");

    ProgressResponseElementV28DTO sendEvent1 = buildSendEvent("sendEventId1", NotificationStatusV26DTO.ACCEPTED);
    ProgressResponseElementV28DTO sendEvent2 = buildSendEvent("sendEventId2", NotificationStatusV26DTO.ACCEPTED);
    List<ProgressResponseElementV28DTO> streamEvents = List.of(
      sendEvent1,
      sendEvent2
    );

    Mockito.when(getSendStreamActivityMock.fetchSendStream(SEND_STREAM_ID))
      .thenReturn(streamDTO)
      .thenReturn(null); //for breaking from do-while loop

    Mockito.when(
      getSendNotificationEventsFromStreamActivityMock.fetchSendNotificationEventsFromStream(
        ORGANIZATION_ID, SEND_STREAM_ID
      )
    ).thenReturn(streamEvents);

    ActivityFailure activityFailureMock = Mockito.mock(ActivityFailure.class);
    Mockito.when(activityFailureMock.getCause())
      .thenReturn(new RuntimeException());

    Mockito.when(sendEventStreamProcessingServiceMock.processSendStreamEvent(
        Mockito.eq(SEND_STREAM_ID),
        Mockito.isA(ProgressResponseElementV28DTO.class)
      )).thenReturn(sendEvent1.getEventId())
      .thenThrow(activityFailureMock);

    try (MockedStatic<Workflow> workflowMock = Mockito.mockStatic(Workflow.class)) {
      workflowMock.when(() -> Workflow.sleep(Mockito.any(Duration.class)))
        .then(invocation -> null);

      //WHEN
      wf.readSendStream(SEND_STREAM_ID);

      //THEN
      Mockito.verify(getSendStreamActivityMock, Mockito.times(2)).fetchSendStream(SEND_STREAM_ID);
      Mockito.verify(updateLastProcessedStreamEventIdActivityMock)
        .updateLastProcessedStreamEventId(
          SEND_STREAM_ID,
          sendEvent1.getEventId()
        );
    }

  }

  @Test
  void givenValidSendStreamIdEventWhenReadSendStreamThenContinueAsNew() {
    //GIVEN
    SendStreamDTO streamDTO = buildSendStreamDTO(SEND_STREAM_ID);
    streamDTO.setLastEventId("lastSendEventId");

    ProgressResponseElementV28DTO sendEvent1 = buildSendEvent("sendEventId1", NotificationStatusV26DTO.ACCEPTED);
    ProgressResponseElementV28DTO sendEvent2 = buildSendEvent("sendEventId2", NotificationStatusV26DTO.ACCEPTED);
    List<ProgressResponseElementV28DTO> streamEvents = List.of(
      sendEvent1,
      sendEvent2
    );

    Mockito.when(getSendStreamActivityMock.fetchSendStream(SEND_STREAM_ID))
      .thenReturn(streamDTO)
      .thenReturn(null); //for breaking from do-while loop

    Mockito.when(
      getSendNotificationEventsFromStreamActivityMock.fetchSendNotificationEventsFromStream(
        ORGANIZATION_ID, SEND_STREAM_ID
      )
    ).thenReturn(streamEvents);

    Mockito.when(sendEventStreamProcessingServiceMock.processSendStreamEvent(
        Mockito.eq(SEND_STREAM_ID),
        Mockito.isA(ProgressResponseElementV28DTO.class)
      )).thenReturn(sendEvent1.getEventId())
      .thenReturn(sendEvent2.getEventId());

    try (MockedStatic<Workflow> workflowMock = Mockito.mockStatic(Workflow.class)) {
      workflowMock.when(() -> Workflow.sleep(Mockito.any(Duration.class)))
        .then(invocation -> null);

      //WHEN
      wf.readSendStream(SEND_STREAM_ID);

      //THEN
      Mockito.verify(getSendStreamActivityMock, Mockito.times(2)).fetchSendStream(SEND_STREAM_ID);
      Mockito.verify(updateLastProcessedStreamEventIdActivityMock)
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
    SendStreamDTO streamDTO = buildSendStreamDTO(SEND_STREAM_ID);
    streamDTO.setLastEventId("lastSendEventId");

    ProgressResponseElementV28DTO sendEvent1 = buildSendEvent("sendEventId1", NotificationStatusV26DTO.ACCEPTED);
    ProgressResponseElementV28DTO sendEvent2 = buildSendEvent("sendEventId2", NotificationStatusV26DTO.ACCEPTED);
    List<ProgressResponseElementV28DTO> streamEvents = List.of(
      sendEvent1,
      sendEvent2
    );

    Mockito.when(getSendStreamActivityMock.fetchSendStream(SEND_STREAM_ID))
      .thenReturn(streamDTO)
      .thenReturn(null); //for breaking from do-while loop

    Mockito.when(
      getSendNotificationEventsFromStreamActivityMock.fetchSendNotificationEventsFromStream(
        ORGANIZATION_ID, SEND_STREAM_ID
      )
    ).thenReturn(streamEvents);

    Mockito.when(sendEventStreamProcessingServiceMock.processSendStreamEvent(
      Mockito.eq(SEND_STREAM_ID),
      Mockito.isA(ProgressResponseElementV28DTO.class)
    )).thenReturn(sendEvent1.getEventId())
      .thenReturn(sendEvent2.getEventId());

    try (MockedStatic<Workflow> workflowMock = Mockito.mockStatic(Workflow.class)) {
      workflowMock.when(() -> Workflow.sleep(Mockito.any(Duration.class)))
        .then(invocation -> null);

      //WHEN
      wf.readSendStream(SEND_STREAM_ID);

      //THEN
      Mockito.verify(getSendStreamActivityMock, Mockito.times(2)).fetchSendStream(SEND_STREAM_ID);
      Mockito.verify(updateLastProcessedStreamEventIdActivityMock)
        .updateLastProcessedStreamEventId(
          SEND_STREAM_ID,
          sendEvent2.getEventId()
        );
    }

  }

  @Test
  void givenValidSendStreamIdWithEmptyStreamEventsWhenReadSendStreamThenOK() {
    //GIVEN
    SendStreamDTO streamDTO = buildSendStreamDTO(SEND_STREAM_ID);
    streamDTO.setLastEventId("lastSendEventId");

    Mockito.when(getSendStreamActivityMock.fetchSendStream(SEND_STREAM_ID))
      .thenReturn(streamDTO)
      .thenReturn(null); //for breaking from do-while loop

    Mockito.when(
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
      Mockito.verify(getSendStreamActivityMock, Mockito.times(2)).fetchSendStream(SEND_STREAM_ID);
    }

  }

  @Test
  void givenGenericErrorInCallIsStreamStillOpenWhenReadSendStreamThenOK() {
    //GIVEN
    SendStreamDTO streamDTO = buildSendStreamDTO(SEND_STREAM_ID);
    streamDTO.setLastEventId("lastSendEventId");

    ProgressResponseElementV28DTO sendEvent1 = buildSendEvent("sendEventId1", NotificationStatusV26DTO.ACCEPTED);
    List<ProgressResponseElementV28DTO> streamEvents = List.of(
      sendEvent1
    );

    Mockito.when(getSendStreamActivityMock.fetchSendStream(SEND_STREAM_ID))
      .thenReturn(streamDTO)
      .thenThrow(new RuntimeException("Error"))
      .thenReturn(null); //for breaking from do-while loop

    Mockito.when(
      getSendNotificationEventsFromStreamActivityMock.fetchSendNotificationEventsFromStream(
        ORGANIZATION_ID, SEND_STREAM_ID
      )
    ).thenReturn(streamEvents);

    Mockito.when(sendEventStreamProcessingServiceMock.processSendStreamEvent(
      Mockito.eq(SEND_STREAM_ID),
      Mockito.isA(ProgressResponseElementV28DTO.class)
    )).thenReturn(sendEvent1.getEventId());


    try (MockedStatic<Workflow> workflowMock = Mockito.mockStatic(Workflow.class)) {
      workflowMock.when(() -> Workflow.sleep(Mockito.any(Duration.class)))
        .then(invocation -> null);

      //WHEN
      wf.readSendStream(SEND_STREAM_ID);

      //THEN
      Mockito.verify(getSendStreamActivityMock, Mockito.times(3))
        .fetchSendStream(SEND_STREAM_ID);
      Mockito.verify(updateLastProcessedStreamEventIdActivityMock)
        .updateLastProcessedStreamEventId(SEND_STREAM_ID, sendEvent1.getEventId());
    }

  }

  @Test
  void givenNotFoundExceptionInCallIsStreamStillOpenWhenReadSendStreamThenOK() {
    //GIVEN
    SendStreamDTO streamDTO = buildSendStreamDTO(SEND_STREAM_ID);
    streamDTO.setLastEventId("lastSendEventId");

    ProgressResponseElementV28DTO sendEvent1 = buildSendEvent("sendEventId1", NotificationStatusV26DTO.ACCEPTED);
    List<ProgressResponseElementV28DTO> streamEvents = List.of(
      sendEvent1
    );

    Mockito.when(getSendStreamActivityMock.fetchSendStream(SEND_STREAM_ID))
      .thenReturn(streamDTO)
      .thenThrow(HttpClientErrorException.create(HttpStatus.NOT_FOUND, "NotFound", null, null, null));

    Mockito.when(
      getSendNotificationEventsFromStreamActivityMock.fetchSendNotificationEventsFromStream(
        ORGANIZATION_ID, SEND_STREAM_ID
      )
    ).thenReturn(streamEvents);

    Mockito.when(sendEventStreamProcessingServiceMock.processSendStreamEvent(
      Mockito.eq(SEND_STREAM_ID),
      Mockito.isA(ProgressResponseElementV28DTO.class)
    )).thenReturn(sendEvent1.getEventId());

    try (MockedStatic<Workflow> workflowMock = Mockito.mockStatic(Workflow.class)) {
      workflowMock.when(() -> Workflow.sleep(Mockito.any(Duration.class)))
        .then(invocation -> null);

      //WHEN
      WorkflowInternalErrorException actualException =
        Assertions.assertThrows(WorkflowInternalErrorException.class, () -> wf.readSendStream(SEND_STREAM_ID));

      Assertions.assertEquals(
        "[SEND_STATUS_ERROR] Workflow terminated during isStreamStillOpened for sendStreamId " + SEND_STREAM_ID + " with ERROR: 404 NotFound",
        actualException.getMessage()
      );

      //THEN
      Mockito.verify(getSendStreamActivityMock, Mockito.times(2))
        .fetchSendStream(SEND_STREAM_ID);
      Mockito.verify(updateLastProcessedStreamEventIdActivityMock)
        .updateLastProcessedStreamEventId(SEND_STREAM_ID, sendEvent1.getEventId());
    }

  }

  @Test
  void givenRepeatOneHundredTimesWhenReadSendStreamThenContinueAsNew() {
    //GIVEN
    SendStreamDTO streamDTO = buildSendStreamDTO(SEND_STREAM_ID);
    streamDTO.setLastEventId("lastSendEventId");

    ProgressResponseElementV28DTO sendEvent1 = buildSendEvent("sendEventId1", NotificationStatusV26DTO.ACCEPTED);
    List<ProgressResponseElementV28DTO> streamEvents = List.of(
      sendEvent1
    );

    SendStreamDTO[] oneHundredStreams = new SendStreamDTO[100];
    Arrays.fill(oneHundredStreams, streamDTO);

    Mockito.when(getSendStreamActivityMock.fetchSendStream(SEND_STREAM_ID))
      .thenReturn(streamDTO, oneHundredStreams)
      .thenReturn(null); //for breaking from do-while loop

    Mockito.when(
      getSendNotificationEventsFromStreamActivityMock.fetchSendNotificationEventsFromStream(
        ORGANIZATION_ID, SEND_STREAM_ID
      )
    ).thenReturn(streamEvents);

    Mockito.when(sendEventStreamProcessingServiceMock.processSendStreamEvent(
      Mockito.eq(SEND_STREAM_ID),
      Mockito.isA(ProgressResponseElementV28DTO.class)
    )).thenReturn(sendEvent1.getEventId());

    try (MockedStatic<Workflow> workflowMock = Mockito.mockStatic(Workflow.class)) {
      workflowMock.when(() -> Workflow.sleep(Mockito.any(Duration.class)))
        .then(invocation -> null);

      //WHEN
      wf.readSendStream(SEND_STREAM_ID);

      //THEN
      Mockito.verify(getSendStreamActivityMock, Mockito.times(102))
        .fetchSendStream(SEND_STREAM_ID);
      Mockito.verify(updateLastProcessedStreamEventIdActivityMock)
        .updateLastProcessedStreamEventId(SEND_STREAM_ID, sendEvent1.getEventId());
      workflowMock.verify(() -> Workflow.continueAsNew(streamDTO.getStreamId()), Mockito.times(2));
    }

  }

  @Test
  void givenValidSendStreamIdWithDifferentEventIdWhenReadSendStreamThenOK() {
    //GIVEN
    SendStreamDTO streamDTO = buildSendStreamDTO(SEND_STREAM_ID);
    streamDTO.setLastEventId("lastSendEventId");

    ProgressResponseElementV28DTO sendEvent1 = buildSendEvent("sendEventId1", NotificationStatusV26DTO.ACCEPTED);
    List<ProgressResponseElementV28DTO> streamEvents = List.of(
      sendEvent1
    );

    Mockito.when(getSendStreamActivityMock.fetchSendStream(SEND_STREAM_ID))
      .thenReturn(streamDTO)
      .thenReturn(null); //for breaking from do-while loop

    Mockito.when(
      getSendNotificationEventsFromStreamActivityMock.fetchSendNotificationEventsFromStream(
        ORGANIZATION_ID, SEND_STREAM_ID
      )
    ).thenReturn(streamEvents);

    Mockito.when(sendEventStreamProcessingServiceMock.processSendStreamEvent(
        Mockito.eq(SEND_STREAM_ID),
        Mockito.isA(ProgressResponseElementV28DTO.class)
      )).thenReturn(sendEvent1.getEventId());

    try (MockedStatic<Workflow> workflowMock = Mockito.mockStatic(Workflow.class)) {
      workflowMock.when(() -> Workflow.sleep(Mockito.any(Duration.class)))
        .then(invocation -> null);

      //WHEN
      wf.readSendStream(SEND_STREAM_ID);

      //THEN
      Mockito.verify(getSendStreamActivityMock, Mockito.times(2))
        .fetchSendStream(SEND_STREAM_ID);
      Mockito.verify(updateLastProcessedStreamEventIdActivityMock)
        .updateLastProcessedStreamEventId(SEND_STREAM_ID, sendEvent1.getEventId());
    }

  }

  @Test
  void givenValidSendStreamIdWithSameEventIdWhenReadSendStreamThenOK() {
    //GIVEN
    SendStreamDTO streamDTO = buildSendStreamDTO(SEND_STREAM_ID);
    streamDTO.setLastEventId("lastSendEventId");

    ProgressResponseElementV28DTO sendEvent1 = buildSendEvent("lastSendEventId", NotificationStatusV26DTO.ACCEPTED);
    List<ProgressResponseElementV28DTO> streamEvents = List.of(
      sendEvent1
    );

    Mockito.when(getSendStreamActivityMock.fetchSendStream(SEND_STREAM_ID))
      .thenReturn(streamDTO)
      .thenReturn(null); //for breaking from do-while loop

    Mockito.when(
      getSendNotificationEventsFromStreamActivityMock.fetchSendNotificationEventsFromStream(
        ORGANIZATION_ID, SEND_STREAM_ID
      )
    ).thenReturn(streamEvents);

    Mockito.when(sendEventStreamProcessingServiceMock.processSendStreamEvent(
      Mockito.eq(SEND_STREAM_ID),
      Mockito.isA(ProgressResponseElementV28DTO.class)
    )).thenReturn(sendEvent1.getEventId());

    try (MockedStatic<Workflow> workflowMock = Mockito.mockStatic(Workflow.class)) {
      workflowMock.when(() -> Workflow.sleep(Mockito.any(Duration.class)))
        .then(invocation -> null);

      //WHEN
      wf.readSendStream(SEND_STREAM_ID);

      //THEN
      Mockito.verify(getSendStreamActivityMock, Mockito.times(2))
        .fetchSendStream(SEND_STREAM_ID);
      Mockito.verify(updateLastProcessedStreamEventIdActivityMock, Mockito.times(0))
        .updateLastProcessedStreamEventId(SEND_STREAM_ID, sendEvent1.getEventId());
    }

  }

  @Test
  void givenValidSendStreamIdWithNullEventIdWhenReadSendStreamThenOK() {
    //GIVEN
    SendStreamDTO streamDTO = buildSendStreamDTO(SEND_STREAM_ID);
    streamDTO.setLastEventId("lastSendEventId");

    ProgressResponseElementV28DTO sendEvent1 = buildSendEvent(null, NotificationStatusV26DTO.ACCEPTED);
    List<ProgressResponseElementV28DTO> streamEvents = List.of(
      sendEvent1
    );

    Mockito.when(getSendStreamActivityMock.fetchSendStream(SEND_STREAM_ID))
      .thenReturn(streamDTO)
      .thenReturn(null); //for breaking from do-while loop

    Mockito.when(
      getSendNotificationEventsFromStreamActivityMock.fetchSendNotificationEventsFromStream(
        ORGANIZATION_ID, SEND_STREAM_ID
      )
    ).thenReturn(streamEvents);

    Mockito.when(sendEventStreamProcessingServiceMock.processSendStreamEvent(
      Mockito.eq(SEND_STREAM_ID),
      Mockito.isA(ProgressResponseElementV28DTO.class)
    )).thenReturn(sendEvent1.getEventId());

    try (MockedStatic<Workflow> workflowMock = Mockito.mockStatic(Workflow.class)) {
      workflowMock.when(() -> Workflow.sleep(Mockito.any(Duration.class)))
        .then(invocation -> null);

      //WHEN
      wf.readSendStream(SEND_STREAM_ID);

      //THEN
      Mockito.verify(getSendStreamActivityMock, Mockito.times(2))
        .fetchSendStream(SEND_STREAM_ID);
      Mockito.verify(updateLastProcessedStreamEventIdActivityMock, Mockito.times(0))
        .updateLastProcessedStreamEventId(SEND_STREAM_ID, sendEvent1.getEventId());
    }

  }

  @Test
  void givenUpdateLastProcessedStreamEventIdThrowsErrorWhenReadSendStreamThenOK() {
    //GIVEN
    SendStreamDTO streamDTO = buildSendStreamDTO(SEND_STREAM_ID);
    streamDTO.setLastEventId("lastSendEventId");

    ProgressResponseElementV28DTO sendEvent1 = buildSendEvent("sendEventId1", NotificationStatusV26DTO.ACCEPTED);
    List<ProgressResponseElementV28DTO> streamEvents = List.of(
      sendEvent1
    );

    Mockito.when(getSendStreamActivityMock.fetchSendStream(SEND_STREAM_ID))
      .thenReturn(streamDTO)
      .thenReturn(null); //for breaking from do-while loop

    Mockito.when(
      getSendNotificationEventsFromStreamActivityMock.fetchSendNotificationEventsFromStream(
        ORGANIZATION_ID, SEND_STREAM_ID
      )
    ).thenReturn(streamEvents);

    Mockito.when(sendEventStreamProcessingServiceMock.processSendStreamEvent(
      Mockito.eq(SEND_STREAM_ID),
      Mockito.isA(ProgressResponseElementV28DTO.class)
    )).thenReturn(sendEvent1.getEventId());

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
      Mockito.verify(getSendStreamActivityMock, Mockito.times(2))
        .fetchSendStream(SEND_STREAM_ID);
      Mockito.verify(updateLastProcessedStreamEventIdActivityMock)
        .updateLastProcessedStreamEventId(SEND_STREAM_ID, sendEvent1.getEventId());
    }

  }

  private static ProgressResponseElementV28DTO buildSendEvent(String sendEventId, NotificationStatusV26DTO notificationStatus) {
    ProgressResponseElementV28DTO sendEvent = new ProgressResponseElementV28DTO();
    sendEvent.setNewStatus(notificationStatus);
    sendEvent.setEventId(sendEventId);
    sendEvent.setNotificationRequestId(SendNotificationStreamConsumeWFImplTest.NOTIFICATION_REQUEST_ID);
    return sendEvent;
  }

  private static SendStreamDTO buildSendStreamDTO(String sendStreamId) {
    SendStreamDTO streamDTO = new SendStreamDTO();
    streamDTO.setStreamId(sendStreamId);
    streamDTO.setOrganizationId(ORGANIZATION_ID);
    return streamDTO;
  }

}
