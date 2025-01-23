package it.gov.pagopa.pu.workflow.event.consumer.payments.dto;

import it.gov.pagopa.pu.debtposition.dto.generated.DebtPositionDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentEventDTO {
  private DebtPositionDTO payload;
  private String eventType;
}
