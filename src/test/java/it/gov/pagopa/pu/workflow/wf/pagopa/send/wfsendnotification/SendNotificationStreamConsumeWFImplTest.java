package it.gov.pagopa.pu.workflow.wf.pagopa.send.wfsendnotification;

import io.temporal.workflow.Workflow;
import it.gov.pagopa.payhub.activities.activity.sendnotification.GetSendNotificationEventsFromStreamActivity;
import it.gov.pagopa.payhub.activities.activity.sendnotification.GetSendStreamActivity;
import it.gov.pagopa.payhub.activities.activity.sendnotification.SendNotificationDateRetrieveActivity;
import it.gov.pagopa.payhub.activities.activity.sendnotification.UpdateSendNotificationStatusActivity;
import it.gov.pagopa.pu.sendnotification.dto.generated.*;
import it.gov.pagopa.pu.workflow.dto.PaymentEventRequestDTO;
import it.gov.pagopa.pu.workflow.dto.generated.PaymentEventType;
import it.gov.pagopa.pu.workflow.wf.pagopa.send.activity.PublishSendNotificationPaymentEventActivity;
import it.gov.pagopa.pu.workflow.wf.pagopa.send.config.SendNotificationProcessWfConfig;
import it.gov.pagopa.pu.workflow.wf.pagopa.send.dto.DebtPositionSendNotificationDTO;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationContext;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

@ExtendWith(MockitoExtension.class)
class SendNotificationStreamConsumeWFImplTest {

  public static final long ORGANIZATION_ID = 1L;
  public static final String NOTIFICATION_REQUEST_ID = "notificationRequestId";
  @Mock
  private GetSendStreamActivity getSendStreamActivityMock;
  @Mock
  private GetSendNotificationEventsFromStreamActivity getSendNotificationEventsFromStreamActivityMock;
  @Mock
  private UpdateSendNotificationStatusActivity updateSendNotificationStatusActivityMock;
  @Mock
  private SendNotificationDateRetrieveActivity sendNotificationDateRetrieveActivityMock;
  @Mock
  private PublishSendNotificationPaymentEventActivity publishSendNotificationPaymentEventActivityMock;

  private SendNotificationStreamConsumeWFImpl wf;

  @BeforeEach
  void setUp() {
    SendNotificationProcessWfConfig wfConfigMock = Mockito.mock(SendNotificationProcessWfConfig.class);
    ApplicationContext applicationContextMock = Mockito.mock(ApplicationContext.class);

    Mockito.when(wfConfigMock.buildGetSendStreamActivityStub()).thenReturn(getSendStreamActivityMock);
    Mockito.when(wfConfigMock.buildGetSendNotificationEventsFromStreamActivityStub()).thenReturn(getSendNotificationEventsFromStreamActivityMock);
    Mockito.when(wfConfigMock.buildUpdateSendNotificationStatusActivityStub()).thenReturn(updateSendNotificationStatusActivityMock);
    Mockito.when(wfConfigMock.buildSendNotificationDateRetrieveActivityStub()).thenReturn(sendNotificationDateRetrieveActivityMock);
    Mockito.when(wfConfigMock.buildPublishSendNotificationPaymentEventActivityStub()).thenReturn(publishSendNotificationPaymentEventActivityMock);

    Mockito.when(applicationContextMock.getBean(SendNotificationProcessWfConfig.class)).thenReturn(wfConfigMock);

    wf = new SendNotificationStreamConsumeWFImpl();
    wf.setApplicationContext(applicationContextMock);
  }

  @AfterEach
  void verifyNoMoreInteractions() {
    Mockito.verifyNoMoreInteractions(
      getSendStreamActivityMock,
      getSendNotificationEventsFromStreamActivityMock,
      updateSendNotificationStatusActivityMock,
      sendNotificationDateRetrieveActivityMock,
      publishSendNotificationPaymentEventActivityMock
    );
  }

  @Test
  void givenInvalidSendStreamIdWhenReadSendStreamThenStreamNotFound() {
    //GIVEN
    String sendStreamId = "invalidSendStreamId";

    Mockito.when(getSendStreamActivityMock.fetchSendStream(sendStreamId))
      .thenReturn(null); //for not entering do-while loop

    //WHEN
    try (MockedStatic<Workflow> workflowMock = Mockito.mockStatic(Workflow.class)) {
      workflowMock.when(() -> Workflow.sleep(Mockito.any(Duration.class)))
        .then(invocation -> null);

      wf.readSendStream(sendStreamId);
    }

    //THEN
    Mockito.verify(getSendStreamActivityMock).fetchSendStream(sendStreamId);
  }

  @Test
  void givenErrorInFetchSendNotificationEventsFromStreamWhenReadSendStreamThenStreamNotFound() {
    //GIVEN
    String sendStreamId = "invalidSendStreamId";
    String lastEventId = "lastEventId";

    SendStreamDTO streamDTO = new SendStreamDTO();
    streamDTO.setStreamId(sendStreamId);
    streamDTO.setOrganizationId(ORGANIZATION_ID);
    streamDTO.setLastEventId(lastEventId);

    Mockito.when(getSendStreamActivityMock.fetchSendStream(sendStreamId))
      .thenReturn(streamDTO)
      .thenReturn(null); //for breaking from do-while loop

    Mockito.doThrow(new RuntimeException())
      .when(getSendNotificationEventsFromStreamActivityMock)
      .fetchSendNotificationEventsFromStream(ORGANIZATION_ID, sendStreamId, lastEventId);

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
  void givenErrorInProcessSendStreamEventWhenReadSendStreamThenOK() {
    //GIVEN
    String sendStreamId = "sendStreamId";
    String lastEventId = "lastEventId";
    String notificationRequestId = "notificationRequestId";

    SendStreamDTO streamDTO = new SendStreamDTO();
    streamDTO.setStreamId(sendStreamId);
    streamDTO.setOrganizationId(ORGANIZATION_ID);
    streamDTO.setLastEventId(lastEventId);

    DebtPositionSendNotificationDTO positionSendNotificationDTO = new DebtPositionSendNotificationDTO();
    positionSendNotificationDTO.setNoticeCodes(new ArrayList<>());
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
        ORGANIZATION_ID, sendStreamId, lastEventId
      )
    ).thenReturn(streamEvents);
    Mockito.doThrow(new RuntimeException())
      .when(sendNotificationDateRetrieveActivityMock)
      .sendNotificationDateRetrieve(notificationRequestId);

    //WHEN
    try (MockedStatic<Workflow> workflowMock = Mockito.mockStatic(Workflow.class)) {
      workflowMock.when(() -> Workflow.sleep(Mockito.any(Duration.class)))
        .then(invocation -> null);

      wf.readSendStream(sendStreamId);
    }

    //THEN
    Mockito.verify(getSendStreamActivityMock, Mockito.times(2)).fetchSendStream(sendStreamId);
  }

  @ParameterizedTest
  @MethodSource("provideEventListScenarios")
  void givenValidSendStreamIdWithAcceptedAndRefusedEventWhenReadSendStreamThenOK(List<ProgressResponseElementV25DTO> streamEvents) {
    //GIVEN
    String sendStreamId = "sendStreamId";
    String lastEventId = "lastEventId";

    SendStreamDTO streamDTO = new SendStreamDTO();
    streamDTO.setStreamId(sendStreamId);
    streamDTO.setOrganizationId(ORGANIZATION_ID);
    streamDTO.setLastEventId(lastEventId);

    DebtPositionSendNotificationDTO positionSendNotificationDTO = new DebtPositionSendNotificationDTO();
    positionSendNotificationDTO.setNoticeCodes(new ArrayList<>());
    positionSendNotificationDTO.setStatus(
      NotificationStatusDTO.ACCEPTED.equals(streamEvents.getFirst().getNewStatus()) ?
      NotificationStatus.ACCEPTED : NotificationStatus.ERROR
    );
    SendNotificationPaymentsDTO sendNotificationPaymentsDTO = new SendNotificationPaymentsDTO();
    SendNotificationDTO sendNotificationDTO = new SendNotificationDTO();
    sendNotificationDTO.setPayments(List.of(sendNotificationPaymentsDTO));
    sendNotificationDTO.setStatus(
      NotificationStatusDTO.ACCEPTED.equals(streamEvents.getFirst().getNewStatus()) ?
        NotificationStatus.ACCEPTED : NotificationStatus.ERROR
    );

    Mockito.when(getSendStreamActivityMock.fetchSendStream(sendStreamId))
      .thenReturn(streamDTO)
      .thenReturn(null); //for breaking from do-while loop
    Mockito.when(
      getSendNotificationEventsFromStreamActivityMock.fetchSendNotificationEventsFromStream(
        ORGANIZATION_ID, sendStreamId, lastEventId
      )
    ).thenReturn(streamEvents);
    Mockito.when(
      updateSendNotificationStatusActivityMock.updateSendNotificationStatus(
        NOTIFICATION_REQUEST_ID
      )
    ).thenReturn(sendNotificationDTO);
    if(NotificationStatusDTO.ACCEPTED.equals(streamEvents.getFirst().getNewStatus())) {
      Mockito.doNothing().when(publishSendNotificationPaymentEventActivityMock)
        .publishSendNotificationEvent(
          positionSendNotificationDTO,
          new PaymentEventRequestDTO(PaymentEventType.SEND_NOTIFICATION_CREATED, null)
        );
    } else {
      Mockito.doNothing().when(publishSendNotificationPaymentEventActivityMock)
        .publishSendNotificationErrorEvent(
          positionSendNotificationDTO,
          new PaymentEventRequestDTO(PaymentEventType.SEND_NOTIFICATION_ERROR, null)
        );
    }

    //WHEN
    try (MockedStatic<Workflow> workflowMock = Mockito.mockStatic(Workflow.class)) {
      workflowMock.when(() -> Workflow.sleep(Mockito.any(Duration.class)))
        .then(invocation -> null);

      wf.readSendStream(sendStreamId);
    }

    //THEN
    if(NotificationStatusDTO.ACCEPTED.equals(streamEvents.getFirst().getNewStatus())) {
      Mockito.verify(publishSendNotificationPaymentEventActivityMock)
        .publishSendNotificationEvent(
          positionSendNotificationDTO,
          new PaymentEventRequestDTO(PaymentEventType.SEND_NOTIFICATION_CREATED, null)
        );
    } else {
      Mockito.verify(publishSendNotificationPaymentEventActivityMock)
        .publishSendNotificationErrorEvent(
          positionSendNotificationDTO,
          new PaymentEventRequestDTO(PaymentEventType.SEND_NOTIFICATION_ERROR, null)
        );
    }
  }

  private static Stream<List<ProgressResponseElementV25DTO>> provideEventListScenarios() {
    ProgressResponseElementV25DTO event1 = new ProgressResponseElementV25DTO();
    event1.setNewStatus(NotificationStatusDTO.ACCEPTED);
    event1.setEventId("eventId1");
    event1.setNotificationRequestId(NOTIFICATION_REQUEST_ID);
    ProgressResponseElementV25DTO event2 = new ProgressResponseElementV25DTO();
    event2.setNewStatus(NotificationStatusDTO.REFUSED);
    event2.setEventId("eventId2");
    event2.setNotificationRequestId(NOTIFICATION_REQUEST_ID);
    return Stream.of(
      List.of(event1),
      List.of(event2)
    );
  }

  @Test
  void givenValidSendStreamIdWithViewedEventWhenReadSendStreamThenOK() {
    //GIVEN
    String sendStreamId = "sendStreamId";
    String lastEventId = "lastEventId";
    String notificationRequestId = "notificationRequestId";

    SendStreamDTO streamDTO = new SendStreamDTO();
    streamDTO.setStreamId(sendStreamId);
    streamDTO.setOrganizationId(ORGANIZATION_ID);
    streamDTO.setLastEventId(lastEventId);

    DebtPositionSendNotificationDTO positionSendNotificationDTO = new DebtPositionSendNotificationDTO();
    positionSendNotificationDTO.setNoticeCodes(new ArrayList<>());
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
        ORGANIZATION_ID, sendStreamId, lastEventId
      )
    ).thenReturn(streamEvents);
    Mockito.when(
      sendNotificationDateRetrieveActivityMock.sendNotificationDateRetrieve(
        notificationRequestId
      )
    ).thenReturn(sendNotificationDTO);

    //WHEN
    try (MockedStatic<Workflow> workflowMock = Mockito.mockStatic(Workflow.class)) {
      workflowMock.when(() -> Workflow.sleep(Mockito.any(Duration.class)))
        .then(invocation -> null);

      wf.readSendStream(sendStreamId);
    }

    //THEN
    Mockito.verify(getSendStreamActivityMock, Mockito.times(2)).fetchSendStream(sendStreamId);
  }

  @ParameterizedTest
  @MethodSource("provideStreamEventWithInvalidNotificationStatusScenarios")
  void givenNotMapperEventStatusWhenReadSendStreamThenSkipEvent(List<ProgressResponseElementV25DTO> streamEvents) {
    //GIVEN
    String sendStreamId = "sendStreamId";
    String lastEventId = "lastEventId";

    SendStreamDTO streamDTO = new SendStreamDTO();
    streamDTO.setStreamId(sendStreamId);
    streamDTO.setOrganizationId(ORGANIZATION_ID);
    streamDTO.setLastEventId(lastEventId);

    DebtPositionSendNotificationDTO positionSendNotificationDTO = new DebtPositionSendNotificationDTO();
    positionSendNotificationDTO.setNoticeCodes(new ArrayList<>());
    SendNotificationPaymentsDTO sendNotificationPaymentsDTO = new SendNotificationPaymentsDTO();
    SendNotificationDTO sendNotificationDTO = new SendNotificationDTO();
    sendNotificationDTO.setPayments(List.of(sendNotificationPaymentsDTO));

    Mockito.when(getSendStreamActivityMock.fetchSendStream(sendStreamId))
      .thenReturn(streamDTO) //first time
      .thenReturn(streamDTO) //second time
      .thenReturn(null); //for breaking from do-while loop
    Mockito.when(
      getSendNotificationEventsFromStreamActivityMock.fetchSendNotificationEventsFromStream(
        ORGANIZATION_ID, sendStreamId, lastEventId
      )
    ).thenReturn(streamEvents);

    //WHEN
    try (MockedStatic<Workflow> workflowMock = Mockito.mockStatic(Workflow.class)) {
      workflowMock.when(() -> Workflow.sleep(Mockito.any(Duration.class)))
        .then(invocation -> null);

      wf.readSendStream(sendStreamId);
    }

    //THEN
    Mockito.verify(getSendStreamActivityMock, Mockito.times(3)).fetchSendStream(sendStreamId);
  }

  private static Stream<List<ProgressResponseElementV25DTO>> provideStreamEventWithInvalidNotificationStatusScenarios() {
    ProgressResponseElementV25DTO event1 = new ProgressResponseElementV25DTO();
    event1.setNewStatus(null);
    ProgressResponseElementV25DTO event2 = new ProgressResponseElementV25DTO();
    event2.setNewStatus(NotificationStatusDTO.DELIVERED);
    return Stream.of(
      List.of(event1),
      List.of(event2)
    );
  }
}
