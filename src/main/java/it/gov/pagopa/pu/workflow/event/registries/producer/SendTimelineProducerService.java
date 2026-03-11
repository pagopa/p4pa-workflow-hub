package it.gov.pagopa.pu.workflow.event.registries.producer;

import it.gov.pagopa.pu.workflow.event.registries.dto.RegistryEventSendTimelineDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;

import java.util.function.Supplier;

@Component
public class SendTimelineProducerService {

  @Value("${spring.cloud.stream.bindings.registryProducer-out-0.binder}")
  private String binder;

  private final StreamBridge streamBridge;

  public SendTimelineProducerService(StreamBridge streamBridge) {
    this.streamBridge = streamBridge;
  }

  @Configuration
  static class SendTimelineConfig {
    @Bean
    public Supplier<Message<RegistryEventSendTimelineDTO>> sendTimelineProducer() {
      return () -> null;
    }
  }

  public void notifySendTimelineEvent(RegistryEventSendTimelineDTO sendTimelineEvent, String sendStreamId) {
    notifySendTimelineEvent(sendTimelineEvent, sendStreamId, "ok");
  }

  public void notifySendTimelineErrorEvent(RegistryEventSendTimelineDTO sendTimelineEvent, String sendStreamId) {
    notifySendTimelineEvent(sendTimelineEvent, sendStreamId, "ko");
  }

  public void notifySendTimelineEvent(RegistryEventSendTimelineDTO payload, String sendStreamId,  String error) {
    streamBridge.send("registryProducer-out-0", binder,
      MessageBuilder.withPayload(payload)
        .setHeader(KafkaHeaders.KEY, String.valueOf(sendStreamId))
        .build()
    );
  }
}
