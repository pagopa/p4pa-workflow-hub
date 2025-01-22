package it.gov.pagopa.pu.workflow.event.consumer.payments.dto;

import it.gov.pagopa.pu.debtposition.dto.generated.DebtPositionDTO;
import lombok.Data;

@Data
public class PaymentEventDTO {
  private DebtPositionDTO payload;
  private String eventType;
}
