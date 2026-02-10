package it.gov.pagopa.pu.workflow.service.wf.send;

import it.gov.pagopa.payhub.activities.activity.sendnotification.SendNotificationDateRetrieveActivity;
import it.gov.pagopa.payhub.activities.activity.sendnotification.UpdateSendNotificationStatusActivity;
import it.gov.pagopa.pu.sendnotification.dto.generated.NotificationStatusDTO;
import it.gov.pagopa.pu.sendnotification.dto.generated.ProgressResponseElementV25DTO;
import it.gov.pagopa.pu.sendnotification.dto.generated.SendNotificationDTO;
import it.gov.pagopa.pu.sendnotification.dto.generated.SendNotificationPaymentsDTO;
import it.gov.pagopa.pu.workflow.dto.PaymentEventRequestDTO;
import it.gov.pagopa.pu.workflow.dto.SendEventStreamProcessResult;
import it.gov.pagopa.pu.workflow.dto.generated.PaymentEventType;
import it.gov.pagopa.pu.workflow.wf.pagopa.send.activity.PublishSendNotificationPaymentEventActivity;
import it.gov.pagopa.pu.workflow.wf.pagopa.send.dto.DebtPositionSendNotificationDTO;
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

  public static final long ORGANIZATION_ID = 1L;
  public static final String SEND_STREAM_ID = "sendStreamId";
  public static final String NOTIFICATION_REQUEST_ID = "notificationRequestId";
  public static final String EVENT_ID = "eventId";

  @Mock
  private UpdateSendNotificationStatusActivity updateSendNotificationStatusActivityMock;
  @Mock
  private SendNotificationDateRetrieveActivity sendNotificationDateRetrieveActivityMock;
  @Mock
  private PublishSendNotificationPaymentEventActivity publishSendNotificationPaymentEventActivityMock;

  @InjectMocks
  private SendEventStreamProcessingServiceImpl sendEventStreamProcessingService;

  @Test
  void givenAcceptedEventWhenProcessSendStreamEventThenOk() {
    //GIVEN
    ProgressResponseElementV25DTO sendEvent = new ProgressResponseElementV25DTO();
    sendEvent.setNewStatus(NotificationStatusDTO.ACCEPTED);
    sendEvent.setEventId(EVENT_ID);
    sendEvent.setNotificationRequestId(NOTIFICATION_REQUEST_ID);

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

    //WHEN
    SendEventStreamProcessResult actualResult =
      sendEventStreamProcessingService.processSendStreamEvent(
        SEND_STREAM_ID,
        sendEvent
      );

    //THEN
    Assertions.assertNotNull(actualResult);
    Assertions.assertEquals(2L, actualResult.getActivityExecutionCount());
    Assertions.assertEquals(sendEvent.getEventId(), actualResult.getLastProcessedEventId());
  }

  @Test
  void givenRefusedEventWhenProcessSendStreamEventThenOk() {
    //GIVEN
    ProgressResponseElementV25DTO sendEvent = new ProgressResponseElementV25DTO();
    sendEvent.setNewStatus(NotificationStatusDTO.REFUSED);
    sendEvent.setEventId(EVENT_ID);
    sendEvent.setNotificationRequestId(NOTIFICATION_REQUEST_ID);

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

    //WHEN
    SendEventStreamProcessResult actualResult =
      sendEventStreamProcessingService.processSendStreamEvent(
        SEND_STREAM_ID,
        sendEvent
      );

    //THEN
    Assertions.assertNotNull(actualResult);
    Assertions.assertEquals(2L, actualResult.getActivityExecutionCount());
    Assertions.assertEquals(sendEvent.getEventId(), actualResult.getLastProcessedEventId());
  }

  @Test
  void givenViewedEventWhenProcessSendStreamEventThenOk() {
    //GIVEN
    ProgressResponseElementV25DTO sendEvent = new ProgressResponseElementV25DTO();
    sendEvent.setNewStatus(NotificationStatusDTO.VIEWED);
    sendEvent.setEventId(EVENT_ID);
    sendEvent.setNotificationRequestId(NOTIFICATION_REQUEST_ID);

    SendNotificationPaymentsDTO sendNotificationPaymentsDTO = new SendNotificationPaymentsDTO();
    SendNotificationDTO sendNotificationDTO = new SendNotificationDTO();
    sendNotificationDTO.setPayments(List.of(sendNotificationPaymentsDTO));

    DebtPositionSendNotificationDTO debtPositionSendNotificationDTO = new DebtPositionSendNotificationDTO();
    debtPositionSendNotificationDTO.setNoticeCodes(new ArrayList<>());

    Mockito.when(sendNotificationDateRetrieveActivityMock.sendNotificationDateRetrieve(
      NOTIFICATION_REQUEST_ID
    )).thenReturn(sendNotificationDTO);

    //WHEN
    SendEventStreamProcessResult actualResult =
      sendEventStreamProcessingService.processSendStreamEvent(
        SEND_STREAM_ID,
        sendEvent
      );

    //THEN
    Assertions.assertNotNull(actualResult);
    Assertions.assertEquals(ORGANIZATION_ID, actualResult.getActivityExecutionCount());
    Assertions.assertEquals(sendEvent.getEventId(), actualResult.getLastProcessedEventId());
  }

  @Test
  void givenNonMappedEventWhenProcessSendStreamEventThenOk() {
    //GIVEN
    ProgressResponseElementV25DTO sendEvent = new ProgressResponseElementV25DTO();
    sendEvent.setNewStatus(NotificationStatusDTO.DELIVERING);
    sendEvent.setEventId(EVENT_ID);
    sendEvent.setNotificationRequestId(NOTIFICATION_REQUEST_ID);

    SendNotificationPaymentsDTO sendNotificationPaymentsDTO = new SendNotificationPaymentsDTO();
    SendNotificationDTO sendNotificationDTO = new SendNotificationDTO();
    sendNotificationDTO.setPayments(List.of(sendNotificationPaymentsDTO));

    DebtPositionSendNotificationDTO debtPositionSendNotificationDTO = new DebtPositionSendNotificationDTO();
    debtPositionSendNotificationDTO.setNoticeCodes(new ArrayList<>());

    //WHEN
    SendEventStreamProcessResult actualResult =
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
    ProgressResponseElementV25DTO sendEvent = new ProgressResponseElementV25DTO();
    sendEvent.setNewStatus(null);
    sendEvent.setEventId(EVENT_ID);
    sendEvent.setNotificationRequestId(NOTIFICATION_REQUEST_ID);

    SendNotificationPaymentsDTO sendNotificationPaymentsDTO = new SendNotificationPaymentsDTO();
    SendNotificationDTO sendNotificationDTO = new SendNotificationDTO();
    sendNotificationDTO.setPayments(List.of(sendNotificationPaymentsDTO));

    DebtPositionSendNotificationDTO debtPositionSendNotificationDTO = new DebtPositionSendNotificationDTO();
    debtPositionSendNotificationDTO.setNoticeCodes(new ArrayList<>());

    //WHEN
    SendEventStreamProcessResult actualResult =
      sendEventStreamProcessingService.processSendStreamEvent(
        SEND_STREAM_ID,
        sendEvent
      );

    //THEN
    Assertions.assertNull(actualResult);
  }

}
