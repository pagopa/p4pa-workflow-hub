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
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationContext;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

@ExtendWith(MockitoExtension.class)
class SendNotificationStreamConsumeWFImplTest {

  public static final long ORGANIZATION_ID = 1L;
  @Mock
  private ApplicationContext applicationContextMock;
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
      .thenReturn(null);

    //WHEN
    try (MockedStatic<Workflow> workflowMock = Mockito.mockStatic(Workflow.class)) {
      workflowMock.when(() -> Workflow.sleep(Mockito.any(Duration.class)))
        .then(invocation -> null);

      wf.readSendStream(sendStreamId);

      //THEN assert no other interaction wanted
    }
  }

  @Test
  void givenValidSendStreamIdWithAcceptedAndRefusedEventWhenReadSendStreamThenOK() {
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
    event1.setNewStatus(NotificationStatusDTO.ACCEPTED);
    event1.setIun("eventId1");
    event1.setNotificationRequestId(notificationRequestId);
    ProgressResponseElementV25DTO event2 = new ProgressResponseElementV25DTO();
    event2.setNewStatus(NotificationStatusDTO.REFUSED);
    event2.setIun("eventId2");
    event2.setNotificationRequestId(notificationRequestId);
    List<ProgressResponseElementV25DTO> streamEvents = List.of(
      event1,
      event2
    );

    Mockito.when(getSendStreamActivityMock.fetchSendStream(sendStreamId))
      .thenReturn(streamDTO)
      .thenReturn(null);
    Mockito.when(
      getSendNotificationEventsFromStreamActivityMock.fetchSendNotificationEventsFromStream(
        ORGANIZATION_ID, sendStreamId, lastEventId
      )
    ).thenReturn(streamEvents);
    Mockito.when(
      updateSendNotificationStatusActivityMock.updateSendNotificationStatus(
        notificationRequestId
      )
    ).thenReturn(sendNotificationDTO);
    Mockito.doNothing().when(publishSendNotificationPaymentEventActivityMock)
      .publishSendNotificationEvent(
        positionSendNotificationDTO,
        new PaymentEventRequestDTO(PaymentEventType.SEND_NOTIFICATION_CREATED, null)
      );

    //WHEN
    try (MockedStatic<Workflow> workflowMock = Mockito.mockStatic(Workflow.class)) {
      workflowMock.when(() -> Workflow.sleep(Mockito.any(Duration.class)))
        .then(invocation -> null);

      wf.readSendStream(sendStreamId);

      //THEN
    }
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
      .thenReturn(null);
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

      //THEN
    }
  }
}
