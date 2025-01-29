package it.gov.pagopa.pu.workflow.event.payments.dto;

import it.gov.pagopa.pu.debtposition.dto.generated.DebtPositionDTO;
import it.gov.pagopa.pu.workflow.event.payments.enums.PaymentEventType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentEventDTO {
  private DebtPositionDTO payload;
  private PaymentEventType eventType;
  private String eventDescription;
}
