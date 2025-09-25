package it.gov.pagopa.pu.workflow.event.dataevents.producer;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import it.gov.pagopa.pu.workflow.dto.IngestionDataDTO;
import it.gov.pagopa.pu.workflow.enums.DataEventType;
import it.gov.pagopa.pu.workflow.event.dataevents.dto.DataEventDTO;
import it.gov.pagopa.pu.workflow.event.dataevents.dto.DataEventRequestDTO;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.MDC;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;

@ExtendWith(MockitoExtension.class)
class DataEventsProducerServiceTest {

  @Mock
  private StreamBridge streamBridge;

  private DataEventsProducerService dataEventsProducerService;

  @BeforeEach
  void setUp() {
    dataEventsProducerService = new DataEventsProducerService(streamBridge);
  }

  @AfterEach
  void clear(){
    MDC.clear();
  }

  @Test
  void whenNotifyAssessmentsEventThenSendMessage() {
    // Given
    IngestionDataDTO ingestionDataDTO = new IngestionDataDTO();
    ingestionDataDTO.setOrganizationId(1L);
    ingestionDataDTO.setIngestionFlowFileId(99L);

    DataEventRequestDTO dataEventRequestDTO = new DataEventRequestDTO(DataEventType.INGESTION, "EVENTDESCRIPTION");
    String traceId = "TRACEID";
    MDC.put("traceId", traceId);

    // When
    dataEventsProducerService.notifyIngestionEvent(ingestionDataDTO, dataEventRequestDTO);

    // Then
    verify(streamBridge, times(1)).send(
      Mockito.eq("dataEventsProducer-out-0"),
      Mockito.any(),
      Mockito.<Message<?>>argThat(m -> {
        DataEventDTO<?> payload = (DataEventDTO<?>)m.getPayload();
        String eventIdPrefix = dataEventRequestDTO.getDataEventType().name() + ingestionDataDTO.getIngestionFlowFileId();
        Assertions.assertEquals(eventIdPrefix, payload.getEventId().substring(0, eventIdPrefix.length()));
        Assertions.assertSame(ingestionDataDTO, payload.getPayload());
        Assertions.assertSame(dataEventRequestDTO.getEventDescription(), payload.getEventDescription());
        Assertions.assertSame(dataEventRequestDTO.getDataEventType(), payload.getEventType());
        Assertions.assertEquals("ingestion"+ingestionDataDTO.getOrganizationId(), m.getHeaders().get(
          KafkaHeaders.KEY));
        return true;
      }));
  }
}
