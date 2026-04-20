package it.gov.pagopa.pu.workflow.wf.pagopa.send.create.activity;

import it.gov.pagopa.pu.workflow.dto.PaymentEventRequestDTO;
import it.gov.pagopa.pu.workflow.dto.generated.PaymentEventType;
import it.gov.pagopa.pu.workflow.event.payments.producer.PaymentsProducerService;
import it.gov.pagopa.pu.workflow.wf.pagopa.send.create.dto.DebtPositionSendNotificationDTO;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PublishSendNotificationPaymentEventActivityTest {

  @Mock
  private PaymentsProducerService eventProduceServiceMock;

  private PublishSendNotificationPaymentEventActivity activity;

  @BeforeEach
  void init(){
    activity = new PublishSendNotificationPaymentEventActivityImpl(eventProduceServiceMock);
  }

  @AfterEach
  void verifyNoMoreInteractions(){
    Mockito.verifyNoMoreInteractions(eventProduceServiceMock);
  }

  @Test
  void whenPublishDebtPositionEventThenInvokeProducer(){
    // Given
    DebtPositionSendNotificationDTO sendNotificationEvent = new DebtPositionSendNotificationDTO();
    PaymentEventRequestDTO paymentEventRequest = new PaymentEventRequestDTO(PaymentEventType.SEND_NOTIFICATION_CREATED, "EVENTDESCRIPTION");

    // When
    activity.publishSendNotificationEvent(sendNotificationEvent, paymentEventRequest);

    // Then
    Mockito.verify(eventProduceServiceMock)
      .notifyDebtPositionSendEvent(Mockito.same(sendNotificationEvent), Mockito.same(paymentEventRequest));
  }

  @Test
  void whenPublishDebtPositionErrorEventThenInvokeProducer(){
    // Given
    DebtPositionSendNotificationDTO sendNotificationEvent = new DebtPositionSendNotificationDTO();
    PaymentEventRequestDTO paymentEventRequest = new PaymentEventRequestDTO(PaymentEventType.SEND_NOTIFICATION_ERROR, "EVENTDESCRIPTION");

    // When
    activity.publishSendNotificationErrorEvent(sendNotificationEvent, paymentEventRequest);

    // Then
    Mockito.verify(eventProduceServiceMock)
      .notifyDebtPositionSendEvent(Mockito.same(sendNotificationEvent), Mockito.same(paymentEventRequest));
  }
}
