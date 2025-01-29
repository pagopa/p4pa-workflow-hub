package it.gov.pagopa.pu.workflow.event.payments.producer;

import it.gov.pagopa.pu.debtposition.dto.generated.DebtPositionDTO;
import it.gov.pagopa.pu.workflow.event.payments.dto.PaymentEventDTO;
import it.gov.pagopa.pu.workflow.event.payments.enums.PaymentEventType;
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
    void whenNotifyPaymentsEventThenSendMessage() {
        // Given
        DebtPositionDTO debtPosition = buildDebtPositionDTO();
        PaymentEventType eventType = PaymentEventType.SYNC_ERROR;

        // When
        paymentsProducerService.notifyPaymentsEvent(debtPosition, eventType, "eventDescription");

        // Then
        verify(streamBridge, times(1)).send(
                Mockito.eq("paymentsProducer-out-0"),
                Mockito.any(),
                Mockito.<Message<?>>argThat(m -> {
                    PaymentEventDTO payload = (PaymentEventDTO) m.getPayload();
                    Assertions.assertSame(debtPosition, payload.getPayload());
                    Assertions.assertSame(eventType, payload.getEventType());
                    Assertions.assertEquals(String.valueOf(debtPosition.getOrganizationId()), m.getHeaders().get(KafkaHeaders.KEY));
                    return true;
                }));
    }
}
