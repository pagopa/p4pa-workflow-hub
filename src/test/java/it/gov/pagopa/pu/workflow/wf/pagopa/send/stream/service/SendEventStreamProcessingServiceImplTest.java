package it.gov.pagopa.pu.workflow.wf.pagopa.send.stream.service;

import it.gov.pagopa.payhub.activities.activity.sendnotification.stream.processing.*;
import it.gov.pagopa.pu.sendnotification.dto.generated.*;
import it.gov.pagopa.pu.workflow.dto.PaymentEventRequestDTO;
import it.gov.pagopa.pu.workflow.dto.generated.PaymentEventType;
import it.gov.pagopa.pu.workflow.wf.pagopa.send.create.activity.PublishSendNotificationPaymentEventActivity;
import it.gov.pagopa.pu.workflow.wf.pagopa.send.create.dto.DebtPositionSendNotificationDTO;
import it.gov.pagopa.pu.workflow.wf.pagopa.send.stream.activity.StartDeleteSendLegalFactFileActivity;
import it.gov.pagopa.pu.workflow.wf.pagopa.send.stream.activity.StartDeleteSendNotificationFileActivity;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

@ExtendWith(MockitoExtension.class)
class SendEventStreamProcessingServiceImplTest {

  public static final String SEND_STREAM_ID = "sendStreamId";
  public static final String NOTIFICATION_REQUEST_ID = "notificationRequestId";
  public static final String EVENT_ID = "eventId";

  @Mock
  private UpdateSendNotificationStatusActivity updateSendNotificationStatusActivityMock;
  @Mock
  private ValidateSendNotificationStatusActivity validateSendNotificationStatusActivityMock;
  @Mock
  private SendNotificationDateRetrieveActivity sendNotificationDateRetrieveActivityMock;
  @Mock
  private PublishSendNotificationPaymentEventActivity publishSendNotificationPaymentEventActivityMock;
  @Mock
  private FetchSendLegalFactActivity fetchSendLegalFactActivityMock;
  @Mock
  private StartDeleteSendNotificationFileActivity startDeleteSendNotificationFileActivityMock;
  @Mock
  private StartDeleteSendLegalFactFileActivity startDeleteSendLegalFactFileActivityMock;
  @Mock
  private GetSendNotificationByNotificationRequestIdActivity getSendNotificationByNotificationRequestIdActivity;

  @InjectMocks
  private SendEventStreamProcessingServiceImpl sendEventStreamProcessingService;

  @AfterEach
  void verifyNoMoreInteractions() {
    Mockito.verifyNoMoreInteractions(
      updateSendNotificationStatusActivityMock,
      validateSendNotificationStatusActivityMock,
      sendNotificationDateRetrieveActivityMock,
      publishSendNotificationPaymentEventActivityMock,
      fetchSendLegalFactActivityMock,
      startDeleteSendNotificationFileActivityMock,
      startDeleteSendLegalFactFileActivityMock,
      getSendNotificationByNotificationRequestIdActivity
    );
  }

  @Test
  void givenAcceptedEventWhenProcessSendStreamEventThenOk() {
    //GIVEN
    ProgressResponseElementV28DTO sendEvent = buildSendEvent(
      null,
      NotificationStatusV26DTO.ACCEPTED
    );

    SendNotificationDTO sendNotificationDTO = buildSendNotification();
    sendNotificationDTO.setStatus(NotificationStatus.SENDING);

    Mockito.when(validateSendNotificationStatusActivityMock.validateSendNotificationStatus(
      NOTIFICATION_REQUEST_ID
    )).thenReturn(sendNotificationDTO);
    Mockito.when(getSendNotificationByNotificationRequestIdActivity.getSendNotificationByNotificationRequestId(
      NOTIFICATION_REQUEST_ID
    )).thenReturn(sendNotificationDTO);

    //WHEN
    String actualResult =
      sendEventStreamProcessingService.processSendStreamEvent(
        SEND_STREAM_ID,
        sendEvent
      );

    //THEN
    Assertions.assertNotNull(actualResult);
    Assertions.assertEquals(EVENT_ID, actualResult);
    Assertions.assertEquals(sendEvent.getEventId(), actualResult);
    Mockito.verify(publishSendNotificationPaymentEventActivityMock)
      .publishSendNotificationEvent(
        Mockito.isA(DebtPositionSendNotificationDTO.class),
        Mockito.eq(new PaymentEventRequestDTO(PaymentEventType.SEND_NOTIFICATION_CREATED, null))
      );
    Mockito.verify(fetchSendLegalFactActivityMock, Mockito.times(0))
      .downloadAndArchiveSendLegalFact(
        Mockito.isA(String.class),
        Mockito.isA(LegalFactCategoryDTO.class),
        Mockito.isA(String.class)
      );
  }

  @Test
  void givenRefusedEventWhenProcessSendStreamEventThenOk() {
    //GIVEN
    ProgressResponseElementV28DTO sendEvent = buildSendEvent(
      null,
      NotificationStatusV26DTO.REFUSED
    );

    SendNotificationDTO sendNotificationDTO = buildSendNotification();
    sendNotificationDTO.setStatus(NotificationStatus.IN_VALIDATION);

    Mockito.when(validateSendNotificationStatusActivityMock.validateSendNotificationStatus(
      NOTIFICATION_REQUEST_ID
    )).thenReturn(sendNotificationDTO);
    Mockito.when(getSendNotificationByNotificationRequestIdActivity.getSendNotificationByNotificationRequestId(
      NOTIFICATION_REQUEST_ID
    )).thenReturn(sendNotificationDTO);

    //WHEN
    String actualResult =
      sendEventStreamProcessingService.processSendStreamEvent(
        SEND_STREAM_ID,
        sendEvent
      );

    //THEN
    Assertions.assertNotNull(actualResult);
    Assertions.assertEquals(EVENT_ID, actualResult);
    Assertions.assertEquals(sendEvent.getEventId(), actualResult);
    Mockito.verify(publishSendNotificationPaymentEventActivityMock)
      .publishSendNotificationErrorEvent(
        Mockito.isA(DebtPositionSendNotificationDTO.class),
        Mockito.eq(new PaymentEventRequestDTO(PaymentEventType.SEND_NOTIFICATION_ERROR, null))
      );
    Mockito.verify(fetchSendLegalFactActivityMock, Mockito.times(0))
      .downloadAndArchiveSendLegalFact(
        Mockito.isA(String.class),
        Mockito.isA(LegalFactCategoryDTO.class),
        Mockito.isA(String.class)
      );
  }

  @Test
  void givenDeliveredEventWhenProcessSendStreamEventThenOk() {
    //GIVEN
    ProgressResponseElementV28DTO sendEvent = buildSendEvent(
      null,
      NotificationStatusV26DTO.DELIVERED
    );

    SendNotificationDTO sendNotificationDTO = buildSendNotification();
    sendNotificationDTO.setStatus(NotificationStatus.ACCEPTED);

    Mockito.when(sendNotificationDateRetrieveActivityMock.sendNotificationDateRetrieve(
      NOTIFICATION_REQUEST_ID
    )).thenReturn(sendNotificationDTO);
    Mockito.when(getSendNotificationByNotificationRequestIdActivity.getSendNotificationByNotificationRequestId(
      NOTIFICATION_REQUEST_ID
    )).thenReturn(sendNotificationDTO);

    Mockito.doNothing().when(startDeleteSendNotificationFileActivityMock).startDeleteSendNotificationExpiredFiles(sendNotificationDTO.getSendNotificationId());
    Mockito.doNothing().when(startDeleteSendLegalFactFileActivityMock).startDeleteSendLegalFactExpiredFiles(sendNotificationDTO.getSendNotificationId());

    //WHEN
    String actualResult =
      sendEventStreamProcessingService.processSendStreamEvent(
        SEND_STREAM_ID,
        sendEvent
      );

    //THEN
    Assertions.assertNotNull(actualResult);
    Assertions.assertEquals(EVENT_ID, actualResult);
    Assertions.assertEquals(sendEvent.getEventId(), actualResult);
    Mockito.verify(publishSendNotificationPaymentEventActivityMock)
      .publishSendNotificationEvent(
        Mockito.isA(DebtPositionSendNotificationDTO.class),
        Mockito.eq(new PaymentEventRequestDTO(PaymentEventType.SEND_NOTIFICATION_DATE, null))
      );
    Mockito.verify(fetchSendLegalFactActivityMock, Mockito.times(0))
      .downloadAndArchiveSendLegalFact(
        Mockito.isA(String.class),
        Mockito.isA(LegalFactCategoryDTO.class),
        Mockito.isA(String.class)
      );
    Mockito.verify(updateSendNotificationStatusActivityMock)
      .updateSendNotificationStatus(NOTIFICATION_REQUEST_ID, NotificationStatus.DELIVERED);
  }

  @ParameterizedTest
  @EnumSource(value = NotificationStatusV26DTO.class, names = {
    "UNREACHABLE"
  })
  void givenSimpleEventWhenProcessSendStreamEventThenUpdateStatusAndStartDeleteLegal(NotificationStatusV26DTO dtoStatus) {
    // GIVEN
    ProgressResponseElementV28DTO sendEvent = buildSendEvent(null, dtoStatus);
    NotificationStatus expectedDomainStatus = NotificationStatus.valueOf(dtoStatus.name());

    SendNotificationDTO sendNotificationDTO = buildSendNotification();
    sendNotificationDTO.setStatus(NotificationStatus.ACCEPTED);
    Mockito.when(getSendNotificationByNotificationRequestIdActivity.getSendNotificationByNotificationRequestId(
      NOTIFICATION_REQUEST_ID
    )).thenReturn(sendNotificationDTO);

    Mockito.doNothing().when(startDeleteSendNotificationFileActivityMock).startDeleteSendNotificationExpiredFiles(sendNotificationDTO.getSendNotificationId());
    Mockito.doNothing().when(startDeleteSendLegalFactFileActivityMock).startDeleteSendLegalFactExpiredFiles(sendNotificationDTO.getSendNotificationId());


    // WHEN
    String actualResult = sendEventStreamProcessingService.processSendStreamEvent(SEND_STREAM_ID, sendEvent);

    // THEN
    Assertions.assertEquals(sendEvent.getEventId(), actualResult);

    Mockito.verify(updateSendNotificationStatusActivityMock)
      .updateSendNotificationStatus(NOTIFICATION_REQUEST_ID, expectedDomainStatus);

    Mockito.verifyNoInteractions(sendNotificationDateRetrieveActivityMock);
    Mockito.verifyNoInteractions(publishSendNotificationPaymentEventActivityMock);
  }


  @ParameterizedTest
  @EnumSource(value = NotificationStatusV26DTO.class, names = {
    "DELIVERING", "VIEWED", "EFFECTIVE_DATE", "PAID",
    "CANCELLED", "RETURNED_TO_SENDER"
  })
  void givenSimpleEventWhenProcessSendStreamEventThenUpdateStatus(NotificationStatusV26DTO dtoStatus) {
    // GIVEN
    ProgressResponseElementV28DTO sendEvent = buildSendEvent(null, dtoStatus);
    NotificationStatus expectedDomainStatus = NotificationStatus.valueOf(dtoStatus.name());

    SendNotificationDTO sendNotificationDTO = buildSendNotification();
    sendNotificationDTO.setStatus(NotificationStatus.DELIVERED);
    Mockito.when(getSendNotificationByNotificationRequestIdActivity.getSendNotificationByNotificationRequestId(
      NOTIFICATION_REQUEST_ID
    )).thenReturn(sendNotificationDTO);

    // WHEN
    String actualResult = sendEventStreamProcessingService.processSendStreamEvent(SEND_STREAM_ID, sendEvent);

    // THEN
    Assertions.assertEquals(sendEvent.getEventId(), actualResult);

    Mockito.verify(updateSendNotificationStatusActivityMock)
      .updateSendNotificationStatus(NOTIFICATION_REQUEST_ID, expectedDomainStatus);

    Mockito.verifyNoInteractions(sendNotificationDateRetrieveActivityMock);
    Mockito.verifyNoInteractions(publishSendNotificationPaymentEventActivityMock);
  }

  @ParameterizedTest
  @MethodSource("nonMappedNotificationStatusScenarios")
  void givenNonMappedEventWhenProcessSendStreamEventThenOk(NotificationStatusV26DTO notificationStatusDTO) {
    //GIVEN
    ProgressResponseElementV28DTO sendEvent = buildSendEvent(
      null,
      notificationStatusDTO
    );

    SendNotificationDTO sendNotificationDTO = buildSendNotification();
    sendNotificationDTO.setStatus(NotificationStatus.IN_VALIDATION);
    Mockito.when(getSendNotificationByNotificationRequestIdActivity.getSendNotificationByNotificationRequestId(
      NOTIFICATION_REQUEST_ID
    )).thenReturn(sendNotificationDTO);

    //WHEN
    String actualResult =
      sendEventStreamProcessingService.processSendStreamEvent(
        SEND_STREAM_ID,
        sendEvent
      );

    //THEN
    Assertions.assertNotNull(actualResult);
    Assertions.assertEquals(EVENT_ID, actualResult);
    Mockito.verify(fetchSendLegalFactActivityMock, Mockito.times(0))
      .downloadAndArchiveSendLegalFact(
        Mockito.isA(String.class),
        Mockito.isA(LegalFactCategoryDTO.class),
        Mockito.isA(String.class)
      );
  }

  private static Stream<NotificationStatusV26DTO> nonMappedNotificationStatusScenarios() {
    return Stream.of(
      NotificationStatusV26DTO.IN_VALIDATION,
      null
    );
  }

  @Test
  void givenValidLegalFactsIdListEventWhenProcessSendStreamEventThenOk() {
    //GIVEN
    LegalFactsIdV20DTO legalFactsId = LegalFactsIdV20DTO.builder()
      .key("id")
      .category(LegalFactCategoryDTO.ANALOG_DELIVERY.getValue())
      .build();

    ProgressResponseElementV28DTO sendEvent = buildSendEvent(
      List.of(legalFactsId),
      NotificationStatusV26DTO.DELIVERED
    );

    SendNotificationDTO sendNotificationDTO = buildSendNotification();
    sendNotificationDTO.setStatus(NotificationStatus.DELIVERED);
    Mockito.when(getSendNotificationByNotificationRequestIdActivity.getSendNotificationByNotificationRequestId(
      NOTIFICATION_REQUEST_ID
    )).thenReturn(sendNotificationDTO);

    //WHEN
    String actualResult =
      sendEventStreamProcessingService.processSendStreamEvent(
        SEND_STREAM_ID,
        sendEvent
      );

    //THEN
    Assertions.assertNotNull(actualResult);
    Assertions.assertEquals(EVENT_ID, actualResult);
    Mockito.verify(fetchSendLegalFactActivityMock)
      .downloadAndArchiveSendLegalFact(
        sendEvent.getNotificationRequestId(),
        LegalFactCategoryDTO.valueOf(legalFactsId.getCategory()),
        legalFactsId.getKey()
      );
  }

  @Test
  void givenEmptyLegalFactsIdListEventWhenProcessSendStreamEventThenOk() {
    //GIVEN
    ProgressResponseElementV28DTO sendEvent = buildSendEvent(
      Collections.emptyList(),
      null
    );

    Mockito.when(getSendNotificationByNotificationRequestIdActivity.getSendNotificationByNotificationRequestId(
      NOTIFICATION_REQUEST_ID
    )).thenReturn(null);

    //WHEN
    String actualResult =
      sendEventStreamProcessingService.processSendStreamEvent(
        SEND_STREAM_ID,
        sendEvent
      );

    //THEN
    Assertions.assertNotNull(actualResult);
    Assertions.assertEquals(EVENT_ID, actualResult);
    Mockito.verify(fetchSendLegalFactActivityMock, Mockito.times(0))
      .downloadAndArchiveSendLegalFact(
        Mockito.isA(String.class),
        Mockito.isA(LegalFactCategoryDTO.class),
        Mockito.isA(String.class)
      );
  }

  @Test
  void givenNullLegalFactsIdListEventWhenProcessSendStreamEventThenOk() {
    //GIVEN
    ProgressResponseElementV28DTO sendEvent = buildSendEvent(
      null,
      null
    );

    Mockito.when(getSendNotificationByNotificationRequestIdActivity.getSendNotificationByNotificationRequestId(
      NOTIFICATION_REQUEST_ID
    )).thenReturn(null);

    //WHEN
    String actualResult =
      sendEventStreamProcessingService.processSendStreamEvent(
        SEND_STREAM_ID,
        sendEvent
      );

    //THEN
    Assertions.assertNotNull(actualResult);
    Assertions.assertEquals(EVENT_ID, actualResult);
    Mockito.verify(fetchSendLegalFactActivityMock, Mockito.times(0))
      .downloadAndArchiveSendLegalFact(
        Mockito.isA(String.class),
        Mockito.isA(LegalFactCategoryDTO.class),
        Mockito.isA(String.class)
      );
  }

  private static SendNotificationDTO buildSendNotification() {
    SendNotificationDTO sendNotificationDTO = new SendNotificationDTO();
    sendNotificationDTO.setPayments(List.of(new SendNotificationPaymentsDTO()));
    return sendNotificationDTO;
  }

  private static ProgressResponseElementV28DTO buildSendEvent(List<LegalFactsIdV20DTO> legalFactsIdList, NotificationStatusV26DTO notificationStatusDTO) {
    TimelineElementV27DTO streamElement = new TimelineElementV27DTO();
    streamElement.setLegalFactsIds(legalFactsIdList);

    ProgressResponseElementV28DTO sendEvent = new ProgressResponseElementV28DTO();
    sendEvent.setNewStatus(notificationStatusDTO);
    sendEvent.setEventId(EVENT_ID);
    sendEvent.setNotificationRequestId(NOTIFICATION_REQUEST_ID);
    sendEvent.setElement(streamElement);
    return sendEvent;
  }

}
