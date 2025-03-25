package it.gov.pagopa.pu.workflow.dto;

import it.gov.pagopa.pu.workflow.dto.generated.PaymentEventType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.experimental.SuperBuilder;

@AllArgsConstructor
@SuperBuilder
@Data
public class PaymentEventRequestDTO {
  private PaymentEventType paymentEventType;
  private String eventDescription;
}
