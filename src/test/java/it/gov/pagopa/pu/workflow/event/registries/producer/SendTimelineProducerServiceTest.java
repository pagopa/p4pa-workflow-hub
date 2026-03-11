package it.gov.pagopa.pu.workflow.event.registries.producer;

import it.gov.pagopa.pu.workflow.event.registries.dto.RegistryEventSendTimelineDTO;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;

@ExtendWith(MockitoExtension.class)
class SendTimelineProducerServiceTest {

  @Mock
  private StreamBridge streamBridge;

  @InjectMocks
  private SendTimelineProducerService sendTimelineProducerService;

  @Test
  void whenNotifyPagoPaEventThenSendMessage() {
    // Given
    RegistryEventSendTimelineDTO event = new RegistryEventSendTimelineDTO();
    String streamId = "streamId";

    // When
    sendTimelineProducerService.notifySendTimelineErrorEvent(event, streamId);

    // Then
    Mockito.verify(streamBridge, Mockito.times(1)).send(
      Mockito.eq("registryProducer-out-0"),
      Mockito.any(),
      Mockito.<Message<?>>argThat(m -> {
        Assertions.assertEquals(event, m.getPayload());
        Assertions.assertEquals(streamId, m.getHeaders().get(KafkaHeaders.KEY));
        return true;
      }));
  }

}
