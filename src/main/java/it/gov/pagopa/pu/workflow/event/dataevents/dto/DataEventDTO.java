package it.gov.pagopa.pu.workflow.event.dataevents.dto;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import it.gov.pagopa.pu.workflow.enums.DataEventType;
import java.time.OffsetDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.EXISTING_PROPERTY, property = "eventType", defaultImpl = DataEventDTO.class, visible = true)
@JsonSubTypes({
  @JsonSubTypes.Type(value = IngestionDataEventDTO.class, name = "INGESTION"),
})
public class DataEventDTO <T> {
  private String eventId;
  private String traceId;
  private DataEventType eventType;
  private OffsetDateTime eventDateTime;
  private T payload;
  private String eventDescription;
}

