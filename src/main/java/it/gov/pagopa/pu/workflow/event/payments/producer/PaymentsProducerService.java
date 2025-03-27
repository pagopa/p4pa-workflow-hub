package it.gov.pagopa.pu.workflow.event.payments.producer;

import it.gov.pagopa.pu.debtposition.dto.generated.DebtPositionDTO;
import it.gov.pagopa.pu.workflow.dto.PaymentEventRequestDTO;
import it.gov.pagopa.pu.workflow.event.payments.dto.PaymentEventDTO;
import it.gov.pagopa.pu.workflow.wf.pagopa.send.dto.DebtPositionSendNotificationDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.util.UUID;

@Component
public class PaymentsProducerService {

  @Value("${spring.cloud.stream.bindings.paymentsProducer-out-0.binder}")
  private String binder;

  private final StreamBridge streamBridge;

  public PaymentsProducerService(StreamBridge streamBridge) {
    this.streamBridge = streamBridge;
  }

    /*
    Producer not connected on startup, but just on-demand
    To connect on startup uncomment these lines and configure bean name (spring.cloud.function.definition)
    @Configuration
    static class PaymentsProducerConfig {
        @Bean
        public Supplier<Message<Object>> paymentsProducer() {
            return () -> null;
        }
    }
    */

  public void notifyDebtPositionPaymentsEvent(DebtPositionDTO debtPosition, PaymentEventRequestDTO paymentEventRequest) {
    notifyPaymentsEvent(debtPosition.getOrganizationId(), String.valueOf(debtPosition.getDebtPositionId()), debtPosition, paymentEventRequest);
  }

  public void notifyDebtPositionSendEvent(DebtPositionSendNotificationDTO dpSendNotification, PaymentEventRequestDTO paymentEventRequest) {
    notifyPaymentsEvent(dpSendNotification.getOrganizationId(), dpSendNotification.getSendNotificationId(), dpSendNotification, paymentEventRequest);
  }

  public void notifyPaymentsEvent(Long organizationId, String entityId, Object payload, PaymentEventRequestDTO paymentEventRequest) {
    String eventId = paymentEventRequest.getPaymentEventType().getValue() + entityId + UUID.randomUUID();
    streamBridge.send("paymentsProducer-out-0", binder,
      MessageBuilder.withPayload(PaymentEventDTO.builder()
          .eventId(eventId)
          .eventType(paymentEventRequest.getPaymentEventType())
          .eventDateTime(OffsetDateTime.now())
          .payload(payload)
          .eventDescription(paymentEventRequest.getEventDescription())
          .build())
        .setHeader(KafkaHeaders.KEY, String.valueOf(organizationId))
        .build()
    );
  }
}
