package it.gov.pagopa.pu.workflow.event.payments.producer;

import it.gov.pagopa.payhub.activities.dto.debtposition.DebtPositionIoNotificationDTO;
import it.gov.pagopa.pu.debtposition.dto.generated.DebtPositionDTO;
import it.gov.pagopa.pu.workflow.dto.PaymentEventRequestDTO;
import it.gov.pagopa.pu.workflow.event.payments.dto.PaymentEventDTO;
import it.gov.pagopa.pu.workflow.wf.pagopa.send.create.dto.DebtPositionSendNotificationDTO;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.util.UUID;
import java.util.function.Supplier;

@Component
public class PaymentsProducerService {

  @Value("${spring.cloud.stream.bindings.paymentsProducer-out-0.binder}")
  private String binder;

  private final StreamBridge streamBridge;

  public PaymentsProducerService(StreamBridge streamBridge) {
    this.streamBridge = streamBridge;
  }

  @Configuration
  static class PaymentsProducerConfig {
    @Bean
    public Supplier<Message<PaymentEventDTO<Object>>> paymentsProducer() {
      return () -> null;
    }
  }

  public void notifyDebtPositionPaymentsEvent(DebtPositionDTO debtPosition, PaymentEventRequestDTO paymentEventRequest) {
    notifyPaymentsEvent(debtPosition.getOrganizationId(), String.valueOf(debtPosition.getDebtPositionId()), debtPosition, paymentEventRequest);
  }

  public void notifyDebtPositionSendEvent(DebtPositionSendNotificationDTO dpSendNotification, PaymentEventRequestDTO paymentEventRequest) {
    notifyPaymentsEvent(dpSendNotification.getOrganizationId(), dpSendNotification.getSendNotificationId(), dpSendNotification, paymentEventRequest);
  }

  public void notifyDebtPositionIoEvent(DebtPositionIoNotificationDTO dpSendNotification, PaymentEventRequestDTO paymentEventRequest) {
    notifyPaymentsEvent(dpSendNotification.getOrganizationId(), String.valueOf(dpSendNotification.getDebtPositionId()), dpSendNotification, paymentEventRequest);
  }

  public void notifyPaymentsEvent(Long organizationId, String entityId, Object payload, PaymentEventRequestDTO paymentEventRequest) {
    String eventId = paymentEventRequest.getPaymentEventType().getValue() + entityId + UUID.randomUUID();
    streamBridge.send("paymentsProducer-out-0", binder,
      MessageBuilder.withPayload(PaymentEventDTO.builder()
          .eventId(eventId)
          .traceId(MDC.get("traceId"))
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
