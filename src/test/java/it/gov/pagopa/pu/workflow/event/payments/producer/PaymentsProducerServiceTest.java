package it.gov.pagopa.pu.workflow.event.payments.producer;

import it.gov.pagopa.pu.debtposition.dto.generated.DebtPositionDTO;
import it.gov.pagopa.pu.workflow.dto.PaymentEventRequestDTO;
import it.gov.pagopa.pu.workflow.dto.generated.PaymentEventType;
import it.gov.pagopa.pu.workflow.event.payments.dto.PaymentEventDTO;
import it.gov.pagopa.pu.workflow.wf.pagopa.send.dto.DebtPositionSendNotificationDTO;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;

import java.time.Duration;
import java.time.OffsetDateTime;

import static it.gov.pagopa.pu.workflow.utils.faker.DebtPositionFaker.buildDebtPositionDTO;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class PaymentsProducerServiceTest {

  @Mock
  private StreamBridge streamBridge;

  private PaymentsProducerService paymentsProducerService;

  @BeforeEach
  public void setUp() {
    paymentsProducerService = new PaymentsProducerService(streamBridge);
  }

  @Test
  void whenNotifyDebtPositionPaymentsEventThenSendMessage() {
    // Given
    DebtPositionDTO debtPosition = buildDebtPositionDTO();
    PaymentEventRequestDTO paymentEventRequest = new PaymentEventRequestDTO(PaymentEventType.DP_CREATED, "EVENTDESCRIPTION");

    // When
    paymentsProducerService.notifyDebtPositionPaymentsEvent(debtPosition, paymentEventRequest);

    // Then
    verify(streamBridge, times(1)).send(
      Mockito.eq("paymentsProducer-out-0"),
      Mockito.any(),
      Mockito.<Message<?>>argThat(m -> {
        PaymentEventDTO<?> payload = (PaymentEventDTO<?>)m.getPayload();
        String eventIdPrefix = paymentEventRequest.getPaymentEventType().getValue() + debtPosition.getDebtPositionId();
        Assertions.assertEquals(eventIdPrefix, payload.getEventId().substring(0, eventIdPrefix.length()));
        Assertions.assertSame(debtPosition, payload.getPayload());
        Assertions.assertSame(paymentEventRequest.getEventDescription(), payload.getEventDescription());
        Assertions.assertSame(paymentEventRequest.getPaymentEventType(), payload.getEventType());
        Assertions.assertTrue(Duration.between(payload.getEventDateTime(), OffsetDateTime.now()).toSeconds() <=5);
        Assertions.assertEquals(String.valueOf(debtPosition.getOrganizationId()), m.getHeaders().get(KafkaHeaders.KEY));
        return true;
      }));
  }

  @Test
  void whenNotifyDebtPositionSendEventThenSendMessage() {
    // Given
    DebtPositionSendNotificationDTO debtPositionSendNotificationDTO = new DebtPositionSendNotificationDTO();
    PaymentEventRequestDTO paymentEventRequest = new PaymentEventRequestDTO(PaymentEventType.SEND_NOTIFICATION_CREATED, "EVENTDESCRIPTION");

    // When
    paymentsProducerService.notifyDebtPositionSendEvent(debtPositionSendNotificationDTO, paymentEventRequest);

    // Then
    verify(streamBridge, times(1)).send(
      Mockito.eq("paymentsProducer-out-0"),
      Mockito.any(),
      Mockito.<Message<?>>argThat(m -> {
        PaymentEventDTO<?> payload = (PaymentEventDTO<?>)m.getPayload();
        String eventIdPrefix = paymentEventRequest.getPaymentEventType().getValue() + debtPositionSendNotificationDTO.getDebtPositionId();
        Assertions.assertEquals(eventIdPrefix, payload.getEventId().substring(0, eventIdPrefix.length()));
        Assertions.assertSame(debtPositionSendNotificationDTO, payload.getPayload());
        Assertions.assertSame(paymentEventRequest.getEventDescription(), payload.getEventDescription());
        Assertions.assertSame(paymentEventRequest.getPaymentEventType(), payload.getEventType());
        Assertions.assertEquals(String.valueOf(debtPositionSendNotificationDTO.getOrganizationId()), m.getHeaders().get(KafkaHeaders.KEY));
        return true;
      }));
  }
}
