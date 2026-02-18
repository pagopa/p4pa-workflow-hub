package it.gov.pagopa.pu.workflow.wf.pagopa.send.wfsendnotification;

import io.temporal.workflow.Workflow;
import it.gov.pagopa.payhub.activities.activity.sendnotification.GetSendNotificationEventsFromStreamActivity;
import it.gov.pagopa.payhub.activities.activity.sendnotification.GetSendStreamActivity;
import it.gov.pagopa.payhub.activities.activity.sendnotification.UpdateLastProcessedStreamEventIdActivity;
import it.gov.pagopa.pu.sendnotification.dto.generated.*;
import it.gov.pagopa.pu.workflow.exception.custom.WorkflowInternalErrorException;
import it.gov.pagopa.pu.workflow.wf.pagopa.send.service.SendEventStreamProcessingService;
import it.gov.pagopa.pu.workflow.wf.pagopa.send.config.SendNotificationProcessWfConfig;
import it.gov.pagopa.pu.workflow.wf.pagopa.send.dto.DebtPositionSendNotificationDTO;
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@ExtendWith(MockitoExtension.class)
class SendNotificationStreamConsumeWFImplTest {

  public static final long ORGANIZATION_ID = 1L;
  public static final String NOTIFICATION_REQUEST_ID = "notificationRequestId";
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
    String sendStreamId = "invalidSendStreamId";

    Mockito.when(getSendStreamActivityMock.fetchSendStream(sendStreamId))
      .thenReturn(null); //for not entering do-while loop

    WorkflowInternalErrorException workflowInternalErrorException;
    //WHEN
    try (MockedStatic<Workflow> workflowMock = Mockito.mockStatic(Workflow.class)) {
      workflowMock.when(() -> Workflow.sleep(Mockito.any(Duration.class)))
        .then(invocation -> null);

      workflowInternalErrorException =
        Assertions.assertThrows(WorkflowInternalErrorException.class, () -> wf.readSendStream(sendStreamId));
    }

    //THEN
    Mockito.verify(getSendStreamActivityMock).fetchSendStream(sendStreamId);
    Assertions.assertEquals(
      "[SEND_STATUS_ERROR] Workflow terminated during starting of readSendStream for sendStreamId %s with ERROR: cannot found SEND stream.".formatted(sendStreamId),
      workflowInternalErrorException.getMessage()
    );
  }

  @Test
  void givenErrorInFetchSendNotificationEventsFromStreamWhenReadSendStreamThenStreamNotFound() {
    //GIVEN
    String sendStreamId = "invalidSendStreamId";

    SendStreamDTO streamDTO = new SendStreamDTO();
    streamDTO.setStreamId(sendStreamId);
    streamDTO.setOrganizationId(ORGANIZATION_ID);

    Mockito.when(getSendStreamActivityMock.fetchSendStream(sendStreamId))
      .thenReturn(streamDTO)
      .thenReturn(null); //for breaking from do-while loop

    Mockito.doThrow(new RuntimeException())
      .when(getSendNotificationEventsFromStreamActivityMock)
      .fetchSendNotificationEventsFromStream(ORGANIZATION_ID, sendStreamId);

    //WHEN
    try (MockedStatic<Workflow> workflowMock = Mockito.mockStatic(Workflow.class)) {
      workflowMock.when(() -> Workflow.sleep(Mockito.any(Duration.class)))
        .then(invocation -> null);

      wf.readSendStream(sendStreamId);
    }

    //THEN
    Mockito.verify(getSendStreamActivityMock, Mockito.times(2)).fetchSendStream(sendStreamId);
  }

  @Test
  void givenGeneralExceptionInProcessSendStreamEventWhenReadSendStreamThenOK() {
    //GIVEN
    String sendStreamId = "sendStreamId";
    String notificationRequestId = "notificationRequestId";

    SendStreamDTO streamDTO = new SendStreamDTO();
    streamDTO.setStreamId(sendStreamId);
    streamDTO.setOrganizationId(ORGANIZATION_ID);

    DebtPositionSendNotificationDTO debtPositionSendNotificationDTO = new DebtPositionSendNotificationDTO();
    debtPositionSendNotificationDTO.setNoticeCodes(new ArrayList<>());
    SendNotificationPaymentsDTO sendNotificationPaymentsDTO = new SendNotificationPaymentsDTO();
    SendNotificationDTO sendNotificationDTO = new SendNotificationDTO();
    sendNotificationDTO.setPayments(List.of(sendNotificationPaymentsDTO));

    ProgressResponseElementV25DTO event1 = new ProgressResponseElementV25DTO();
    event1.setNewStatus(NotificationStatusDTO.VIEWED);
    event1.setIun("eventId1");
    event1.setNotificationRequestId(notificationRequestId);
    List<ProgressResponseElementV25DTO> streamEvents = List.of(
      event1
    );

    Mockito.when(getSendStreamActivityMock.fetchSendStream(sendStreamId))
      .thenReturn(streamDTO)
      .thenReturn(null); //for breaking from do-while loop
    Mockito.when(
      getSendNotificationEventsFromStreamActivityMock.fetchSendNotificationEventsFromStream(
        ORGANIZATION_ID, sendStreamId
      )
    ).thenReturn(streamEvents);
    Mockito.doThrow(new RuntimeException())
      .when(sendEventStreamProcessingServiceMock).processSendStreamEvent(
        sendStreamId,
        event1
      );

    //WHEN
    try (MockedStatic<Workflow> workflowMock = Mockito.mockStatic(Workflow.class)) {
      workflowMock.when(() -> Workflow.sleep(Mockito.any(Duration.class)))
        .then(invocation -> null);

      wf.readSendStream(sendStreamId);
    }

    //THEN
    Mockito.verify(getSendStreamActivityMock, Mockito.times(2)).fetchSendStream(sendStreamId);
  }

  @Test
  void givenNotFoundInProcessSendStreamEventWhenReadSendStreamThenOK() {
    //GIVEN
    String sendStreamId = "sendStreamId";
    String lastEventId = "lastEventId";
    String eventId1 = "lastEventId1";
    String eventId2 = "lastEventId2";
    String eventId3 = "lastEventId3";

    ProgressResponseElementV25DTO streamEvent1 = new ProgressResponseElementV25DTO();
    streamEvent1.setNewStatus(NotificationStatusDTO.ACCEPTED);
    streamEvent1.setEventId(eventId1);
    streamEvent1.setNotificationRequestId(NOTIFICATION_REQUEST_ID);
    ProgressResponseElementV25DTO streamEvent2 = new ProgressResponseElementV25DTO();
    streamEvent2.setNewStatus(NotificationStatusDTO.ACCEPTED);
    streamEvent2.setEventId(eventId2);
    streamEvent2.setNotificationRequestId(NOTIFICATION_REQUEST_ID);
    ProgressResponseElementV25DTO streamEvent3 = new ProgressResponseElementV25DTO();
    streamEvent3.setNewStatus(NotificationStatusDTO.ACCEPTED);
    streamEvent3.setEventId(eventId3);
    streamEvent3.setNotificationRequestId(NOTIFICATION_REQUEST_ID);

    SendStreamDTO streamDTO = new SendStreamDTO();
    streamDTO.setStreamId(sendStreamId);
    streamDTO.setOrganizationId(ORGANIZATION_ID);
    streamDTO.setLastEventId(lastEventId);

    DebtPositionSendNotificationDTO debtPositionSendNotificationDTO = new DebtPositionSendNotificationDTO();
    debtPositionSendNotificationDTO.setNoticeCodes(new ArrayList<>());
    debtPositionSendNotificationDTO.setStatus(NotificationStatus.ACCEPTED);
    SendNotificationPaymentsDTO sendNotificationPaymentsDTO = new SendNotificationPaymentsDTO();
    SendNotificationDTO sendNotificationDTO = new SendNotificationDTO();
    sendNotificationDTO.setPayments(List.of(sendNotificationPaymentsDTO));
    sendNotificationDTO.setStatus(NotificationStatus.ACCEPTED);

    HttpClientErrorException notFound = HttpClientErrorException.create(HttpStatus.NOT_FOUND, "NotFound", null, null, null);

    Mockito.when(getSendStreamActivityMock.fetchSendStream(sendStreamId))
      .thenReturn(streamDTO)
      .thenReturn(null); //for breaking from do-while loop
    Mockito.when(
      getSendNotificationEventsFromStreamActivityMock.fetchSendNotificationEventsFromStream(
        ORGANIZATION_ID, sendStreamId
      )
    ).thenReturn(List.of(streamEvent1, streamEvent2, streamEvent3));
    Mockito.when(sendEventStreamProcessingServiceMock.processSendStreamEvent(
      Mockito.eq(sendStreamId),
      Mockito.isA(ProgressResponseElementV25DTO.class)
    )).thenReturn(eventId1)
      .thenThrow(notFound)
      .thenReturn(null);

    //WHEN
    try (MockedStatic<Workflow> workflowMock = Mockito.mockStatic(Workflow.class)) {
      workflowMock.when(() -> Workflow.sleep(Mockito.any(Duration.class)))
        .then(invocation -> null);

      wf.readSendStream(sendStreamId);
    }

    //THEN
    Mockito.verify(getSendStreamActivityMock, Mockito.times(2)).fetchSendStream(sendStreamId);
    Mockito.verify(updateLastProcessedStreamEventIdActivityMock).updateLastProcessedStreamEventId(Mockito.isA(String.class), Mockito.isA(String.class));
  }

  @Test
  void givenValidSendStreamIdWithAcceptedEventWhenReadSendStreamThenOK() {
    //GIVEN
    String sendStreamId = "sendStreamId";
    String lastEventId = "lastEventId";
    String eventId1 = "lastEventId1";
    String eventId2 = "lastEventId2";
    String eventId3 = "lastEventId3";

    ProgressResponseElementV25DTO streamEvent1 = new ProgressResponseElementV25DTO();
    streamEvent1.setNewStatus(NotificationStatusDTO.ACCEPTED);
    streamEvent1.setEventId(eventId1);
    streamEvent1.setNotificationRequestId(NOTIFICATION_REQUEST_ID);
    ProgressResponseElementV25DTO streamEvent2 = new ProgressResponseElementV25DTO();
    streamEvent2.setNewStatus(NotificationStatusDTO.ACCEPTED);
    streamEvent2.setEventId(eventId2);
    streamEvent2.setNotificationRequestId(NOTIFICATION_REQUEST_ID);
    ProgressResponseElementV25DTO streamEvent3 = new ProgressResponseElementV25DTO();
    streamEvent3.setNewStatus(NotificationStatusDTO.ACCEPTED);
    streamEvent3.setEventId(eventId3);
    streamEvent3.setNotificationRequestId(NOTIFICATION_REQUEST_ID);

    SendStreamDTO streamDTO = new SendStreamDTO();
    streamDTO.setStreamId(sendStreamId);
    streamDTO.setOrganizationId(ORGANIZATION_ID);
    streamDTO.setLastEventId(lastEventId);

    DebtPositionSendNotificationDTO debtPositionSendNotificationDTO = new DebtPositionSendNotificationDTO();
    debtPositionSendNotificationDTO.setNoticeCodes(new ArrayList<>());
    debtPositionSendNotificationDTO.setStatus(NotificationStatus.ACCEPTED);
    SendNotificationPaymentsDTO sendNotificationPaymentsDTO = new SendNotificationPaymentsDTO();
    SendNotificationDTO sendNotificationDTO = new SendNotificationDTO();
    sendNotificationDTO.setPayments(List.of(sendNotificationPaymentsDTO));
    sendNotificationDTO.setStatus(NotificationStatus.ACCEPTED);

    Mockito.when(getSendStreamActivityMock.fetchSendStream(sendStreamId))
      .thenReturn(streamDTO)
      .thenReturn(streamDTO)
      .thenReturn(null); //for breaking from do-while loop
    Mockito.when(
      getSendNotificationEventsFromStreamActivityMock.fetchSendNotificationEventsFromStream(
        ORGANIZATION_ID, sendStreamId
      )
    ).thenReturn(List.of(streamEvent1, streamEvent2, streamEvent3));
    Mockito.when(sendEventStreamProcessingServiceMock.processSendStreamEvent(
      Mockito.eq(sendStreamId),
      Mockito.isA(ProgressResponseElementV25DTO.class)
    )).thenReturn(eventId1)
      .thenReturn(eventId2)
      .thenReturn(null);

    //WHEN
    try (MockedStatic<Workflow> workflowMock = Mockito.mockStatic(Workflow.class)) {
      workflowMock.when(() -> Workflow.sleep(Mockito.any(Duration.class)))
        .then(invocation -> null);

      wf.readSendStream(sendStreamId);
    }

    //THEN
    Mockito.verify(getSendStreamActivityMock, Mockito.times(3)).fetchSendStream(sendStreamId);
    Mockito.verify(updateLastProcessedStreamEventIdActivityMock, Mockito.times(2)).updateLastProcessedStreamEventId(Mockito.isA(String.class), Mockito.isA(String.class));
  }

  @Test
  void givenValidSendStreamIdWithEmptyStreamEventsWhenReadSendStreamThenOK() {
    //GIVEN
    String sendStreamId = "sendStreamId";
    String lastEventId = "lastEventId";

    SendStreamDTO streamDTO = new SendStreamDTO();
    streamDTO.setStreamId(sendStreamId);
    streamDTO.setOrganizationId(ORGANIZATION_ID);
    streamDTO.setLastEventId(lastEventId);

    DebtPositionSendNotificationDTO debtPositionSendNotificationDTO = new DebtPositionSendNotificationDTO();
    debtPositionSendNotificationDTO.setNoticeCodes(new ArrayList<>());
    debtPositionSendNotificationDTO.setStatus(NotificationStatus.ACCEPTED);
    SendNotificationPaymentsDTO sendNotificationPaymentsDTO = new SendNotificationPaymentsDTO();
    SendNotificationDTO sendNotificationDTO = new SendNotificationDTO();
    sendNotificationDTO.setPayments(List.of(sendNotificationPaymentsDTO));
    sendNotificationDTO.setStatus(NotificationStatus.ACCEPTED);

    Mockito.when(getSendStreamActivityMock.fetchSendStream(sendStreamId))
      .thenReturn(streamDTO)
      .thenReturn(null); //for breaking from do-while loop
    Mockito.when(
      getSendNotificationEventsFromStreamActivityMock.fetchSendNotificationEventsFromStream(
        ORGANIZATION_ID, sendStreamId
      )
    ).thenReturn(Collections.emptyList());

    //WHEN
    try (MockedStatic<Workflow> workflowMock = Mockito.mockStatic(Workflow.class)) {
      workflowMock.when(() -> Workflow.sleep(Mockito.any(Duration.class)))
        .then(invocation -> null);

      wf.readSendStream(sendStreamId);
    }

    //THEN
    Mockito.verify(getSendStreamActivityMock, Mockito.times(2)).fetchSendStream(sendStreamId);
    Mockito.verify(updateLastProcessedStreamEventIdActivityMock).updateLastProcessedStreamEventId(Mockito.isA(String.class), Mockito.isA(String.class));
  }

  @Test
  void givenErrorInCallIsStreamStillOpenWhenReadSendStreamThenOK() {
    //GIVEN
    String sendStreamId = "sendStreamId";
    String lastEventId = "lastEventId";
    String newLastEventId = "newLastEventId";

    ProgressResponseElementV25DTO streamEvent = new ProgressResponseElementV25DTO();
    streamEvent.setNewStatus(NotificationStatusDTO.ACCEPTED);
    streamEvent.setEventId("eventId");
    streamEvent.setNotificationRequestId(NOTIFICATION_REQUEST_ID);

    SendStreamDTO streamDTO = new SendStreamDTO();
    streamDTO.setStreamId(sendStreamId);
    streamDTO.setOrganizationId(ORGANIZATION_ID);
    streamDTO.setLastEventId(lastEventId);

    DebtPositionSendNotificationDTO debtPositionSendNotificationDTO = new DebtPositionSendNotificationDTO();
    debtPositionSendNotificationDTO.setNoticeCodes(new ArrayList<>());
    debtPositionSendNotificationDTO.setStatus(NotificationStatus.ACCEPTED);
    SendNotificationPaymentsDTO sendNotificationPaymentsDTO = new SendNotificationPaymentsDTO();
    SendNotificationDTO sendNotificationDTO = new SendNotificationDTO();
    sendNotificationDTO.setPayments(List.of(sendNotificationPaymentsDTO));
    sendNotificationDTO.setStatus(NotificationStatus.ACCEPTED);

    HttpClientErrorException notFound = HttpClientErrorException.create(HttpStatus.NOT_FOUND, "NotFound", null, null, null);

    Mockito.when(getSendStreamActivityMock.fetchSendStream(sendStreamId))
      .thenReturn(streamDTO)
      .thenThrow(notFound);
    Mockito.when(
      getSendNotificationEventsFromStreamActivityMock.fetchSendNotificationEventsFromStream(
        ORGANIZATION_ID, sendStreamId
      )
    ).thenReturn(List.of(streamEvent));
    Mockito.when(sendEventStreamProcessingServiceMock.processSendStreamEvent(
      sendStreamId,
      streamEvent
    )).thenReturn(newLastEventId);

    //WHEN
    try (MockedStatic<Workflow> workflowMock = Mockito.mockStatic(Workflow.class)) {
      workflowMock.when(() -> Workflow.sleep(Mockito.any(Duration.class)))
        .then(invocation -> null);

      WorkflowInternalErrorException actualException =
        Assertions.assertThrows(WorkflowInternalErrorException.class, () -> wf.readSendStream(sendStreamId));

      Assertions.assertEquals("[SEND_STATUS_ERROR] Workflow terminated during isStreamStillOpened for sendStreamId " + sendStreamId + " with ERROR: %s".formatted(notFound.getMessage()), actualException.getMessage());
    }

    //THEN
    Mockito.verify(getSendStreamActivityMock, Mockito.times(2)).fetchSendStream(sendStreamId);
    Mockito.verify(updateLastProcessedStreamEventIdActivityMock).updateLastProcessedStreamEventId(Mockito.isA(String.class), Mockito.isA(String.class));
  }

  @Test
  void givenRepeatOneHundredTimesWhenReadSendStreamThenContinueAsNew() {
    //GIVEN
    String sendStreamId = "sendStreamId";
    String lastEventId = "lastEventId";
    String newLastEventId = "newLastEventId";

    ProgressResponseElementV25DTO streamEvent = new ProgressResponseElementV25DTO();
    streamEvent.setNewStatus(NotificationStatusDTO.ACCEPTED);
    streamEvent.setEventId("eventId");
    streamEvent.setNotificationRequestId(NOTIFICATION_REQUEST_ID);

    SendStreamDTO streamDTO = new SendStreamDTO();
    streamDTO.setStreamId(sendStreamId);
    streamDTO.setOrganizationId(ORGANIZATION_ID);
    streamDTO.setLastEventId(lastEventId);

    DebtPositionSendNotificationDTO debtPositionSendNotificationDTO = new DebtPositionSendNotificationDTO();
    debtPositionSendNotificationDTO.setNoticeCodes(new ArrayList<>());
    debtPositionSendNotificationDTO.setStatus(NotificationStatus.ACCEPTED);
    SendNotificationPaymentsDTO sendNotificationPaymentsDTO = new SendNotificationPaymentsDTO();
    SendNotificationDTO sendNotificationDTO = new SendNotificationDTO();
    sendNotificationDTO.setPayments(List.of(sendNotificationPaymentsDTO));
    sendNotificationDTO.setStatus(NotificationStatus.ACCEPTED);

    SendStreamDTO[] oneHundredStreams = new SendStreamDTO[100];
    Arrays.fill(oneHundredStreams, streamDTO);
    Mockito.when(getSendStreamActivityMock.fetchSendStream(sendStreamId))
      .thenReturn(streamDTO, oneHundredStreams)
      .thenReturn(null); //for breaking from do-while loop
    Mockito.when(
      getSendNotificationEventsFromStreamActivityMock.fetchSendNotificationEventsFromStream(
        ORGANIZATION_ID, sendStreamId
      )
    ).thenReturn(List.of(streamEvent));
    Mockito.when(sendEventStreamProcessingServiceMock.processSendStreamEvent(
      sendStreamId,
      streamEvent
    )).thenReturn(newLastEventId);

    //WHEN
    try (MockedStatic<Workflow> workflowMock = Mockito.mockStatic(Workflow.class)) {
      workflowMock.when(() -> Workflow.sleep(Mockito.any(Duration.class)))
        .then(invocation -> null);

      wf.readSendStream(sendStreamId);
    }

    //THEN
    Mockito.verify(getSendStreamActivityMock, Mockito.times(102)).fetchSendStream(sendStreamId);
    Mockito.verify(updateLastProcessedStreamEventIdActivityMock, Mockito.times(101)).updateLastProcessedStreamEventId(Mockito.isA(String.class), Mockito.isA(String.class));
  }

}
