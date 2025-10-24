package it.gov.pagopa.pu.workflow.event.dataevents.producer;

import it.gov.pagopa.payhub.activities.dto.assessments.AssessmentEventDTO;
import it.gov.pagopa.pu.workflow.dto.ExportDataDTO;
import it.gov.pagopa.pu.workflow.dto.IngestionDataDTO;
import it.gov.pagopa.pu.workflow.enums.DataEventType;
import it.gov.pagopa.pu.workflow.event.dataevents.dto.DataEventDTO;
import it.gov.pagopa.pu.workflow.event.dataevents.dto.DataEventRequestDTO;
import it.gov.pagopa.pu.workflow.utilities.Utilities;
import java.time.OffsetDateTime;
import java.util.UUID;
import java.util.function.Supplier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;

@Component
public class DataEventsProducerService {

  @Value("${spring.cloud.stream.bindings.dataEventsProducer-out-0.binder}")
  private String binder;

  private final StreamBridge streamBridge;

  public DataEventsProducerService(StreamBridge streamBridge) {
    this.streamBridge = streamBridge;
  }

  @Configuration
  static class DataEventsProducerConfig {
    @Bean
    public Supplier<Message<DataEventType>> dataEventsProducer() {
      return () -> null;
    }
  }

  public void notifyIngestionEvent(IngestionDataDTO ingestionDataDTO, DataEventRequestDTO dataEventRequest) {
    notifyDataEvent(ingestionDataDTO.getOrganizationId(), String.valueOf(ingestionDataDTO.getIngestionFlowFileId()), ingestionDataDTO, dataEventRequest, "ingestion");
  }

  public void notifyExportEvent(ExportDataDTO exportDataDTO, DataEventRequestDTO dataEventRequest) {
    notifyDataEvent(exportDataDTO.getOrganizationId(), String.valueOf(exportDataDTO.getExportFileId()), exportDataDTO, dataEventRequest, "export");
  }

  public void notifyPaymentAssessmentsEvent(AssessmentEventDTO assessmentsEventDTO, DataEventRequestDTO dataEventRequest) {
    notifyDataEvent(
      assessmentsEventDTO.getOrganizationId(),
      String.valueOf(assessmentsEventDTO.getAssessmentId()),
      assessmentsEventDTO,
      dataEventRequest,
      "assessments"
    );
  }

  private void notifyDataEvent(Long organizationId, String entityId, Object payload, DataEventRequestDTO dataEventRequest, String partitionKey) {
    String eventId = dataEventRequest.getDataEventType().name() + entityId + UUID.randomUUID();
    streamBridge.send("dataEventsProducer-out-0", binder,
      MessageBuilder.withPayload(DataEventDTO.builder()
          .eventId(eventId)
          .traceId(Utilities.getTraceId())
          .eventType(dataEventRequest.getDataEventType())
          .eventDateTime(OffsetDateTime.now())
          .payload(payload)
          .eventDescription(dataEventRequest.getEventDescription())
          .build())
        .setHeader(KafkaHeaders.KEY, partitionKey+organizationId)
        .build()
    );
  }
}
