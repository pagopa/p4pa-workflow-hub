package it.gov.pagopa.pu.workflow.event.payments.dto;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import it.gov.pagopa.pu.workflow.dto.generated.PaymentEventType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "eventType", defaultImpl = PaymentEventDTO.class)
@JsonSubTypes({
  @JsonSubTypes.Type(value = DebtPositionEventDTO.class, names = {
    "DP_CREATED",
    "DP_UPDATED",
    "DP_CANCELLED",
    "DPI_ADDED",
    "DPI_UPDATED",
    "DPI_CANCELLED",
    "RT_RECEIVED",
    "SYNC_ERROR",
  }),
})
public class PaymentEventDTO <T> {
  private String eventId;
  private T payload;
  private PaymentEventType eventType;
  private String eventDescription;
}
