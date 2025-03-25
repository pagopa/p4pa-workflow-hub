package it.gov.pagopa.pu.workflow.dto;

import it.gov.pagopa.pu.workflow.dto.generated.PaymentEventType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;

@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@Data
public class PaymentEventRequestDTO implements Serializable {
  private PaymentEventType paymentEventType;
  private String eventDescription;
}
