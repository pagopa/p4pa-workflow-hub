package it.gov.pagopa.pu.workflow.wf.pagopa.send.service;

import it.gov.pagopa.payhub.activities.activity.sendnotification.FetchSendLegalFactActivity;
import it.gov.pagopa.payhub.activities.activity.sendnotification.SendNotificationDateRetrieveActivity;
import it.gov.pagopa.payhub.activities.activity.sendnotification.UpdateSendNotificationStatusActivity;
import it.gov.pagopa.pu.sendnotification.dto.generated.*;
import it.gov.pagopa.pu.workflow.dto.PaymentEventRequestDTO;
import it.gov.pagopa.pu.workflow.dto.generated.PaymentEventType;
import it.gov.pagopa.pu.workflow.wf.pagopa.send.activity.PublishSendNotificationPaymentEventActivity;
import it.gov.pagopa.pu.workflow.wf.pagopa.send.dto.DebtPositionSendNotificationDTO;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;

@ExtendWith(MockitoExtension.class)
class SendEventStreamProcessingServiceImplTest {

  public static final String SEND_STREAM_ID = "sendStreamId";
  public static final String NOTIFICATION_REQUEST_ID = "notificationRequestId";
  public static final String EVENT_ID = "eventId";

  @Mock
  private UpdateSendNotificationStatusActivity updateSendNotificationStatusActivityMock;
  @Mock
  private SendNotificationDateRetrieveActivity sendNotificationDateRetrieveActivityMock;
  @Mock
  private PublishSendNotificationPaymentEventActivity publishSendNotificationPaymentEventActivityMock;
  @Mock
  private FetchSendLegalFactActivity fetchSendLegalFactActivityMock;

  @InjectMocks
  private SendEventStreamProcessingServiceImpl sendEventStreamProcessingService;

  @AfterEach
  void verifyNoMoreInteractions() {
    Mockito.verifyNoMoreInteractions(
      updateSendNotificationStatusActivityMock,
      sendNotificationDateRetrieveActivityMock,
      publishSendNotificationPaymentEventActivityMock,
      fetchSendLegalFactActivityMock
    );
  }

  @Test
  void givenAcceptedEventWhenProcessSendStreamEventThenOk() {
    //GIVEN
    TimelineElementV25DTO streamElement = new TimelineElementV25DTO();
    LegalFactsIdV20DTO legalFactsId = LegalFactsIdV20DTO.builder().key("id").category(LegalFactCategoryDTO.ANALOG_DELIVERY.getValue()).build();
    streamElement.setLegalFactsIds(List.of(legalFactsId));
    ProgressResponseElementV25DTO sendEvent = new ProgressResponseElementV25DTO();
    sendEvent.setNewStatus(NotificationStatusDTO.ACCEPTED);
    sendEvent.setEventId(EVENT_ID);
    sendEvent.setNotificationRequestId(NOTIFICATION_REQUEST_ID);
    sendEvent.setElement(streamElement);

    SendNotificationPaymentsDTO sendNotificationPaymentsDTO = new SendNotificationPaymentsDTO();
    SendNotificationDTO sendNotificationDTO = new SendNotificationDTO();
    sendNotificationDTO.setPayments(List.of(sendNotificationPaymentsDTO));

    DebtPositionSendNotificationDTO debtPositionSendNotificationDTO = new DebtPositionSendNotificationDTO();
    debtPositionSendNotificationDTO.setNoticeCodes(new ArrayList<>());

    Mockito.when(updateSendNotificationStatusActivityMock.updateSendNotificationStatus(
      NOTIFICATION_REQUEST_ID
    )).thenReturn(sendNotificationDTO);
    Mockito.doNothing().when(publishSendNotificationPaymentEventActivityMock)
      .publishSendNotificationEvent(
        debtPositionSendNotificationDTO,
        new PaymentEventRequestDTO(PaymentEventType.SEND_NOTIFICATION_CREATED, null)
      );

    Mockito.doNothing()
      .when(fetchSendLegalFactActivityMock)
      .downloadAndArchiveSendLegalFact(
        sendEvent.getNotificationRequestId(),
        LegalFactCategoryDTO.valueOf(legalFactsId.getCategory()),
        legalFactsId.getKey()
      );

    //WHEN
    String actualResult =
      sendEventStreamProcessingService.processSendStreamEvent(
        SEND_STREAM_ID,
        sendEvent
      );

    //THEN
    Assertions.assertNotNull(actualResult);
    Assertions.assertEquals(sendEvent.getEventId(), actualResult);
  }

  @Test
  void givenRefusedEventWhenProcessSendStreamEventThenOk() {
    //GIVEN
    TimelineElementV25DTO streamElement = new TimelineElementV25DTO();
    LegalFactsIdV20DTO legalFactsId = LegalFactsIdV20DTO.builder().key("id").category(LegalFactCategoryDTO.ANALOG_DELIVERY.getValue()).build();
    streamElement.setLegalFactsIds(List.of(legalFactsId));
    ProgressResponseElementV25DTO sendEvent = new ProgressResponseElementV25DTO();
    sendEvent.setNewStatus(NotificationStatusDTO.REFUSED);
    sendEvent.setEventId(EVENT_ID);
    sendEvent.setNotificationRequestId(NOTIFICATION_REQUEST_ID);
    sendEvent.setElement(streamElement);

    SendNotificationPaymentsDTO sendNotificationPaymentsDTO = new SendNotificationPaymentsDTO();
    SendNotificationDTO sendNotificationDTO = new SendNotificationDTO();
    sendNotificationDTO.setPayments(List.of(sendNotificationPaymentsDTO));

    DebtPositionSendNotificationDTO debtPositionSendNotificationDTO = new DebtPositionSendNotificationDTO();
    debtPositionSendNotificationDTO.setNoticeCodes(new ArrayList<>());

    Mockito.when(updateSendNotificationStatusActivityMock.updateSendNotificationStatus(
      NOTIFICATION_REQUEST_ID
    )).thenReturn(sendNotificationDTO);
    Mockito.doNothing().when(publishSendNotificationPaymentEventActivityMock)
      .publishSendNotificationErrorEvent(
        debtPositionSendNotificationDTO,
        new PaymentEventRequestDTO(PaymentEventType.SEND_NOTIFICATION_ERROR, null)
      );

    Mockito.doNothing()
      .when(fetchSendLegalFactActivityMock)
      .downloadAndArchiveSendLegalFact(
        sendEvent.getNotificationRequestId(),
        LegalFactCategoryDTO.valueOf(legalFactsId.getCategory()),
        legalFactsId.getKey()
      );

    //WHEN
    String actualResult =
      sendEventStreamProcessingService.processSendStreamEvent(
        SEND_STREAM_ID,
        sendEvent
      );

    //THEN
    Assertions.assertNotNull(actualResult);
    Assertions.assertEquals(sendEvent.getEventId(), actualResult);
  }

  @Test
  void givenViewedEventWhenProcessSendStreamEventThenOk() {
    //GIVEN
    TimelineElementV25DTO streamElement = new TimelineElementV25DTO();
    LegalFactsIdV20DTO legalFactsId = LegalFactsIdV20DTO.builder().key("id").category(LegalFactCategoryDTO.ANALOG_DELIVERY.getValue()).build();
    streamElement.setLegalFactsIds(List.of(legalFactsId));
    ProgressResponseElementV25DTO sendEvent = new ProgressResponseElementV25DTO();
    sendEvent.setNewStatus(NotificationStatusDTO.VIEWED);
    sendEvent.setEventId(EVENT_ID);
    sendEvent.setNotificationRequestId(NOTIFICATION_REQUEST_ID);
    sendEvent.setElement(streamElement);

    SendNotificationPaymentsDTO sendNotificationPaymentsDTO = new SendNotificationPaymentsDTO();
    SendNotificationDTO sendNotificationDTO = new SendNotificationDTO();
    sendNotificationDTO.setPayments(List.of(sendNotificationPaymentsDTO));

    DebtPositionSendNotificationDTO debtPositionSendNotificationDTO = new DebtPositionSendNotificationDTO();
    debtPositionSendNotificationDTO.setNoticeCodes(new ArrayList<>());

    Mockito.when(sendNotificationDateRetrieveActivityMock.sendNotificationDateRetrieve(
      NOTIFICATION_REQUEST_ID
    )).thenReturn(sendNotificationDTO);

    Mockito.doNothing()
      .when(fetchSendLegalFactActivityMock)
      .downloadAndArchiveSendLegalFact(
        sendEvent.getNotificationRequestId(),
        LegalFactCategoryDTO.valueOf(legalFactsId.getCategory()),
        legalFactsId.getKey()
      );

    //WHEN
    String actualResult =
      sendEventStreamProcessingService.processSendStreamEvent(
        SEND_STREAM_ID,
        sendEvent
      );

    //THEN
    Assertions.assertNotNull(actualResult);
    Assertions.assertEquals(sendEvent.getEventId(), actualResult);
  }

  @Test
  void givenNonMappedEventWhenProcessSendStreamEventThenOk() {
    //GIVEN
    TimelineElementV25DTO streamElement = new TimelineElementV25DTO();
    LegalFactsIdV20DTO legalFactsId = LegalFactsIdV20DTO.builder().key("id").category(LegalFactCategoryDTO.ANALOG_DELIVERY.getValue()).build();
    streamElement.setLegalFactsIds(List.of(legalFactsId));
    ProgressResponseElementV25DTO sendEvent = new ProgressResponseElementV25DTO();
    sendEvent.setNewStatus(NotificationStatusDTO.DELIVERING);
    sendEvent.setEventId(EVENT_ID);
    sendEvent.setNotificationRequestId(NOTIFICATION_REQUEST_ID);
    sendEvent.setElement(streamElement);

    SendNotificationPaymentsDTO sendNotificationPaymentsDTO = new SendNotificationPaymentsDTO();
    SendNotificationDTO sendNotificationDTO = new SendNotificationDTO();
    sendNotificationDTO.setPayments(List.of(sendNotificationPaymentsDTO));

    DebtPositionSendNotificationDTO debtPositionSendNotificationDTO = new DebtPositionSendNotificationDTO();
    debtPositionSendNotificationDTO.setNoticeCodes(new ArrayList<>());

    Mockito.doNothing()
      .when(fetchSendLegalFactActivityMock)
      .downloadAndArchiveSendLegalFact(
        sendEvent.getNotificationRequestId(),
        LegalFactCategoryDTO.valueOf(legalFactsId.getCategory()),
        legalFactsId.getKey()
      );

    //WHEN
    String actualResult =
      sendEventStreamProcessingService.processSendStreamEvent(
        SEND_STREAM_ID,
        sendEvent
      );

    //THEN
    Assertions.assertNull(actualResult);
  }

  @Test
  void givenNullEventWhenProcessSendStreamEventThenOk() {
    //GIVEN
    TimelineElementV25DTO streamElement = new TimelineElementV25DTO();
    LegalFactsIdV20DTO legalFactsId = LegalFactsIdV20DTO.builder().key("id").category(LegalFactCategoryDTO.ANALOG_DELIVERY.getValue()).build();
    streamElement.setLegalFactsIds(List.of(legalFactsId));
    ProgressResponseElementV25DTO sendEvent = new ProgressResponseElementV25DTO();
    sendEvent.setNewStatus(null);
    sendEvent.setEventId(EVENT_ID);
    sendEvent.setNotificationRequestId(NOTIFICATION_REQUEST_ID);
    sendEvent.setElement(streamElement);

    SendNotificationPaymentsDTO sendNotificationPaymentsDTO = new SendNotificationPaymentsDTO();
    SendNotificationDTO sendNotificationDTO = new SendNotificationDTO();
    sendNotificationDTO.setPayments(List.of(sendNotificationPaymentsDTO));

    DebtPositionSendNotificationDTO debtPositionSendNotificationDTO = new DebtPositionSendNotificationDTO();
    debtPositionSendNotificationDTO.setNoticeCodes(new ArrayList<>());

    Mockito.doNothing()
      .when(fetchSendLegalFactActivityMock)
      .downloadAndArchiveSendLegalFact(
        sendEvent.getNotificationRequestId(),
        LegalFactCategoryDTO.valueOf(legalFactsId.getCategory()),
        legalFactsId.getKey()
      );

    //WHEN
    String actualResult =
      sendEventStreamProcessingService.processSendStreamEvent(
        SEND_STREAM_ID,
        sendEvent
      );

    //THEN
    Assertions.assertNull(actualResult);
  }

}
